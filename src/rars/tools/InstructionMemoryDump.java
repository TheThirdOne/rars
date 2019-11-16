/*
  Copyright (c) 2019, John Owens

  Developed by John Owens (jowens@ece.ucdavis.edu)

  Permission is hereby granted, free of charge, to any person obtaining
  a copy of this software and associated documentation files (the
  "Software"), to deal in the Software without restriction, including
  without limitation the rights to use, copy, modify, merge, publish,
  distribute, sublicense, and/or sell copies of the Software, and to
  permit persons to whom the Software is furnished to do so, subject
  to the following conditions:

  The above copyright notice and this permission notice shall be
  included in all copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
  ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
  CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

  (MIT license, http://www.opensource.org/licenses/mit-license.html)
*/
package rars.tools;

import rars.ProgramStatement;
import rars.riscv.hardware.AccessNotice;
import rars.riscv.hardware.AddressErrorException;
import rars.riscv.hardware.Memory;
import rars.riscv.hardware.MemoryAccessNotice;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.awt.*;
import java.util.Observable;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;

/**
 * Instruction/memory dump tool. Dumps every instruction run and every memory access to a file.
 *
 * <p>
 * Code based on InstructionCounter.
 *
 * @author John Owens <jowens@ece.ucdavis.edu>
 */
public class InstructionMemoryDump extends AbstractToolAndApplication {
    private static String name = "Instruction/Memory Dump";
    private static String version = "Version 1.0 (John Owens)";
    private static String heading = "Dumps every executed instruction and data memory access to a file";

    /**
     * The last address we saw. We ignore it because the only way for a
     * program to execute twice the same instruction is to enter an infinite
     * loop, which is not insteresting in the POV of counting instructions.
     */
    private int lastAddress = -1;

    /**
     * Instructions and memory accesses get logged here
     */
    private StringBuffer log = new StringBuffer("");

    /**
     * Filename when we dump the log
     */
    private JTextField dumpLogFilename;

    /**
     * Simple constructor, likely used to run a stand-alone memory reference visualizer.
     *
     * @param title   String containing title for title bar
     * @param heading String containing text for heading shown in upper part of window.
     */
    public InstructionMemoryDump(String title, String heading) {
        super(title, heading);
    }

    /**
     * Simple construction, likely used by the RARS Tools menu mechanism.
     */
    public InstructionMemoryDump() {
        super(name + ", " + version, heading);
    }

    @Override
    protected JComponent buildMainDisplayArea() {
        JPanel panel = new JPanel(new GridBagLayout());

        // Adds a "Dump Log" button, which, not surprisingly, dumps the log to a file
        JButton dumpLogButton = new JButton("Dump Log");
        dumpLogButton.setToolTipText("Dumps the log to a file");
        dumpLogButton.addActionListener(
                                        new ActionListener() {
                                            public void actionPerformed(ActionEvent e) {
                                                dumpLog();
                                            }
                                        });
        dumpLogButton.addKeyListener(new EnterKeyListener(dumpLogButton));

        dumpLogFilename = new JTextField("dumplog.txt", 20);

        panel.add(dumpLogButton);
        panel.add(dumpLogFilename);

        return panel;
    }

    @Override
    public String getName() {
        return name;
    }

    private int lowDataSegmentAddress = Memory.dataSegmentBaseAddress;
    private int highDataSegmentAddress = Memory.stackBaseAddress;

    @Override
    protected void addAsObserver() {
        // watch the text segment (the program)
        addAsObserver(Memory.textBaseAddress, Memory.textLimitAddress);
        // also watch the data segment
        addAsObserver(lowDataSegmentAddress, highDataSegmentAddress);
    }

    @Override
    protected void processRISCVUpdate(Observable resource, AccessNotice notice) {
        if (!notice.accessIsFromRISCV()) return;

        // we've got two kinds of access here: instructions and data
        MemoryAccessNotice m = (MemoryAccessNotice) notice;
        int a = m.getAddress();

        // is a in the text segment (program)?
        if ((a >= Memory.textBaseAddress) && (a < Memory.textLimitAddress)) {
            if (notice.getAccessType() != AccessNotice.READ) return;
            if (a == lastAddress) return;
            lastAddress = a;
            try {
                ProgramStatement stmt = Memory.getInstance().getStatement(a);

                // If the program is finished, getStatement() will return null,
                // A null statement will cause the simulator to stall.
                if (stmt != null) {
                    BasicInstruction instr = (BasicInstruction) stmt.getInstruction();
                    BasicInstructionFormat format = instr.getInstructionFormat();

                    // First, dump the instruction, prefixed by "I:"
                    log.append("I: 0x"
                               + Integer.toUnsignedString(stmt.getBinaryStatement(), 16)
                               + "\n");
                }
            } catch (AddressErrorException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // is a in the text segment (program)?
        if ((a >= lowDataSegmentAddress) && (a < highDataSegmentAddress)) {
            if (notice.getAccessType() == AccessNotice.READ) log.append("L: ");
            if (notice.getAccessType() == AccessNotice.WRITE) log.append("S: ");
            log.append(Integer.toUnsignedString(a, 16) + "\n");
        }

        updateDisplay();
    }

    @Override
    protected void initializePreGUI() {
    }

    public void dumpLog() {
        try {
            String filename = dumpLogFilename.getText();
            if (filename == "") return;
            File file = new File(filename);
            BufferedWriter bwr = new BufferedWriter(new FileWriter(file));
            bwr.write(log.toString());
            bwr.flush();
            bwr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void reset() {
        lastAddress = -1;
        updateDisplay();
    }

    @Override
    protected void updateDisplay() {
    }
}
