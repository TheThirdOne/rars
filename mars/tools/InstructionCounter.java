/*
Copyright (c) 2008,  Felipe Lessa

Developed by Felipe Lessa (felipe.lessa@gmail.com)

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
package mars.tools;

import mars.ProgramStatement;
import mars.mips.hardware.AccessNotice;
import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.Memory;
import mars.mips.hardware.MemoryAccessNotice;
import mars.mips.instructions.BasicInstruction;
import mars.mips.instructions.BasicInstructionFormat;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;

/**
 * Instruction counter tool. Can be used to know how many instructions
 * were executed to complete a given program.
 * <p>
 * Code slightly based on MemoryReferenceVisualization.
 *
 * @author Felipe Lessa <felipe.lessa@gmail.com>
 */
//@SuppressWarnings("serial")
public class InstructionCounter extends AbstractMarsToolAndApplication {
    private static String name = "Instruction Counter";
    private static String version = "Version 1.0 (Felipe Lessa)";
    private static String heading = "Counting the number of instructions executed";

    /**
     * Number of instructions executed until now.
     */
    private int counter = 0;
    private JTextField counterField;

    /**
     * Number of instructions of type R.
     */
    private int counterR = 0;
    private JTextField counterRField;
    private JProgressBar progressbarR;

    /**
     * Number of instructions of type I.
     */
    private int counterI = 0;
    private JTextField counterIField;
    private JProgressBar progressbarI;

    /**
     * Number of instructions of type S.
     */
    private int counterS = 0;
    private JTextField counterSField;
    private JProgressBar progressbarS;

    /**
     * Number of instructions of type U.
     */
    private int counterU = 0;
    private JTextField counterUField;
    private JProgressBar progressbarU;


    /**
     * The last address we saw. We ignore it because the only way for a
     * program to execute twice the same instruction is to enter an infinite
     * loop, which is not insteresting in the POV of counting instructions.
     */
    private int lastAddress = -1;

    /**
     * Simple constructor, likely used to run a stand-alone memory reference visualizer.
     *
     * @param title   String containing title for title bar
     * @param heading String containing text for heading shown in upper part of window.
     */
    public InstructionCounter(String title, String heading) {
        super(title, heading);
    }

    /**
     * Simple construction, likely used by the MARS Tools menu mechanism.
     */
    public InstructionCounter() {
        super(name + ", " + version, heading);
    }

    //	@Override
    public String getName() {
        return name;
    }

    //	@Override
    protected JComponent buildMainDisplayArea() {
        // Create everything
        JPanel panel = new JPanel(new GridBagLayout());

        counterField = new JTextField("0", 10);
        counterField.setEditable(false);

        counterRField = new JTextField("0", 10);
        counterRField.setEditable(false);
        progressbarR = new JProgressBar(JProgressBar.HORIZONTAL);
        progressbarR.setStringPainted(true);

        counterIField = new JTextField("0", 10);
        counterIField.setEditable(false);
        progressbarI = new JProgressBar(JProgressBar.HORIZONTAL);
        progressbarI.setStringPainted(true);

        counterSField = new JTextField("0", 10);
        counterSField.setEditable(false);
        progressbarS = new JProgressBar(JProgressBar.HORIZONTAL);
        progressbarS.setStringPainted(true);

        counterUField = new JTextField("0", 10);
        counterUField.setEditable(false);
        progressbarU = new JProgressBar(JProgressBar.HORIZONTAL);
        progressbarU.setStringPainted(true);

        // Add them to the panel

        // Fields
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.LINE_START;
        c.gridheight = c.gridwidth = 1;
        c.gridx = 3;
        c.gridy = 1;
        c.insets = new Insets(0, 0, 17, 0);
        panel.add(counterField, c);

        c.insets = new Insets(0, 0, 0, 0);
        c.gridy++;
        panel.add(counterRField, c);

        c.gridy++;
        panel.add(counterIField, c);

        c.gridy++;
        panel.add(counterSField, c);

        c.gridy++;
        panel.add(counterUField, c);

        // Labels
        c.anchor = GridBagConstraints.LINE_END;
        c.gridx = 1;
        c.gridwidth = 2;
        c.gridy = 1;
        c.insets = new Insets(0, 0, 17, 0);
        panel.add(new JLabel("Instructions so far: "), c);

        c.insets = new Insets(0, 0, 0, 0);
        c.gridx = 2;
        c.gridwidth = 1;
        c.gridy++;
        panel.add(new JLabel("R-type: "), c);

        c.gridy++;
        panel.add(new JLabel("I-type: "), c);

        c.gridy++;
        panel.add(new JLabel("S-type: "), c);

        c.gridy++;
        panel.add(new JLabel("U-type: "), c);

        // Progress bars
        c.insets = new Insets(3, 3, 3, 3);
        c.gridx = 4;
        c.gridy = 2;
        panel.add(progressbarR, c);

        c.gridy++;
        panel.add(progressbarI, c);

        c.gridy++;
        panel.add(progressbarS, c);

        c.gridy++;
        panel.add(progressbarU, c);

        return panel;
    }

    //	@Override
    protected void addAsObserver() {
        addAsObserver(Memory.textBaseAddress, Memory.textLimitAddress);
    }

    //	@Override
    protected void processMIPSUpdate(Observable resource, AccessNotice notice) {
        if (!notice.accessIsFromMIPS()) return;
        if (notice.getAccessType() != AccessNotice.READ) return;
        MemoryAccessNotice m = (MemoryAccessNotice) notice;
        int a = m.getAddress();
        if (a == lastAddress) return;
        lastAddress = a;
        counter++;
        try {
            ProgramStatement stmt = Memory.getInstance().getStatement(a);
            BasicInstruction instr = (BasicInstruction) stmt.getInstruction();
            BasicInstructionFormat format = instr.getInstructionFormat();
            if (format == BasicInstructionFormat.R_FORMAT)
                counterR++;
            else if (format == BasicInstructionFormat.I_FORMAT)
                counterI++;
            else if (format == BasicInstructionFormat.S_FORMAT || format == BasicInstructionFormat.S_BRANCH_FORMAT)
                counterS++;
            else if (format == BasicInstructionFormat.U_FORMAT || format == BasicInstructionFormat.U_JUMP_FORMAT)
                counterU++;
        } catch (AddressErrorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        updateDisplay();
    }

    //	@Override
    protected void initializePreGUI() {
        counter = counterR = counterI = counterS = counterU = 0;
        lastAddress = -1;
    }

    // @Override
    protected void reset() {
        counter = counterR = counterI = counterS = 0;
        lastAddress = -1;
        updateDisplay();
    }

    //	@Override
    protected void updateDisplay() {
        counterField.setText(String.valueOf(counter));

        counterRField.setText(String.valueOf(counterR));
        progressbarR.setMaximum(counter);
        progressbarR.setValue(counterR);

        counterIField.setText(String.valueOf(counterI));
        progressbarI.setMaximum(counter);
        progressbarI.setValue(counterI);

        counterSField.setText(String.valueOf(counterS));
        progressbarS.setMaximum(counter);
        progressbarS.setValue(counterS);

        counterUField.setText(String.valueOf(counterU));
        progressbarU.setMaximum(counter);
        progressbarU.setValue(counterU);

        if (counter == 0) {
            progressbarR.setString("0%");
            progressbarI.setString("0%");
            progressbarS.setString("0%");
            progressbarU.setString("0%");
        } else {
            progressbarR.setString((counterR * 100) / counter + "%");
            progressbarI.setString((counterI * 100) / counter + "%");
            progressbarS.setString((counterS * 100) / counter + "%");
            progressbarU.setString((counterU * 100) / counter + "%");
        }
    }
}
