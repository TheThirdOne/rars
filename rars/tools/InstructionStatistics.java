/*
Copyright (c) 2009,  Ingo Kofler, ITEC, Klagenfurt University, Austria

Developed by Ingo Kofler (ingo.kofler@itec.uni-klu.ac.at)
Based on the Instruction Counter tool by Felipe Lessa (felipe.lessa@gmail.com)

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

import javax.swing.*;
import java.awt.*;
import java.util.Observable;


/**
 * A RARS tool for obtaining instruction statistics by instruction category.
 * <p>
 * The code of this tools is initially based on the Instruction counter tool by Felipe Lassa.
 *
 * @author Ingo Kofler <ingo.kofler@itec.uni-klu.ac.at>
 */
public class InstructionStatistics extends AbstractToolAndApplication {

    /**
     * name of the tool
     */
    private static String NAME = "Instruction Statistics";

    /**
     * version and author information of the tool
     */
    private static String VERSION = "Version 1.0 (Ingo Kofler)";

    /**
     * heading of the tool
     */
    private static String HEADING = "";


    /**
     * number of instruction categories used by this tool
     */
    private static final int MAX_CATEGORY = 5;

    /**
     * constant for ALU instructions category
     */
    private static final int CATEGORY_ALU = 0;

    /**
     * constant for jump instructions category
     */
    private static final int CATEGORY_JUMP = 1;

    /**
     * constant for branch instructions category
     */
    private static final int CATEGORY_BRANCH = 2;

    /**
     * constant for memory instructions category
     */
    private static final int CATEGORY_MEM = 3;

    /**
     * constant for any other instruction category
     */
    private static final int CATEGORY_OTHER = 4;


    /**
     * text field for visualizing the total number of instructions processed
     */
    private JTextField m_tfTotalCounter;

    /**
     * array of text field - one for each instruction category
     */
    private JTextField[] m_tfCounters;

    /**
     * array of progress pars - one for each instruction category
     */
    private JProgressBar[] m_pbCounters;


    /**
     * counter for the total number of instructions processed
     */
    private int m_totalCounter = 0;

    /**
     * array of counter variables - one for each instruction category
     */
    private int[] m_counters = new int[MAX_CATEGORY];

    /**
     * names of the instruction categories as array
     */
    private String[] m_categoryLabels = {"ALU", "Jump", "Branch", "Memory", "Other"};


    // From Felipe Lessa's instruction counter.  Prevent double-counting of instructions 
    // which happens because 2 read events are generated.   
    /**
     * The last address we saw. We ignore it because the only way for a
     * program to execute twice the same instruction is to enter an infinite
     * loop, which is not insteresting in the POV of counting instructions.
     */
    protected int lastAddress = -1;

    /**
     * Simple constructor, likely used to run a stand-alone enhanced instruction counter.
     *
     * @param title   String containing title for title bar
     * @param heading String containing text for heading shown in upper part of window.
     */
    public InstructionStatistics(String title, String heading) {
        super(title, heading);
    }


    /**
     * Simple construction, likely used by the RARS Tools menu mechanism.
     */
    public InstructionStatistics() {
        super(InstructionStatistics.NAME + ", " + InstructionStatistics.VERSION, InstructionStatistics.HEADING);
    }


    @Override
    public String getName() {
        return NAME;
    }


    /**
     * creates the display area for the tool as required by the API
     *
     * @return a panel that holds the GUI of the tool
     */
    protected JComponent buildMainDisplayArea() {

        // Create GUI elements for the tool
        JPanel panel = new JPanel(new GridBagLayout());

        m_tfTotalCounter = new JTextField("0", 10);
        m_tfTotalCounter.setEditable(false);

        m_tfCounters = new JTextField[MAX_CATEGORY];
        m_pbCounters = new JProgressBar[MAX_CATEGORY];

        // for each category a text field and a progress bar is created
        for (int i = 0; i < InstructionStatistics.MAX_CATEGORY; i++) {
            m_tfCounters[i] = new JTextField("0", 10);
            m_tfCounters[i].setEditable(false);
            m_pbCounters[i] = new JProgressBar(JProgressBar.HORIZONTAL);
            m_pbCounters[i].setStringPainted(true);
        }

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.LINE_START;
        c.gridheight = c.gridwidth = 1;

        // create the label and text field for the total instruction counter
        c.gridx = 2;
        c.gridy = 1;
        c.insets = new Insets(0, 0, 17, 0);
        panel.add(new JLabel("Total: "), c);
        c.gridx = 3;
        panel.add(m_tfTotalCounter, c);

        c.insets = new Insets(3, 3, 3, 3);

        // create label, text field and progress bar for each category
        for (int i = 0; i < InstructionStatistics.MAX_CATEGORY; i++) {
            c.gridy++;
            c.gridx = 2;
            panel.add(new JLabel(m_categoryLabels[i] + ":   "), c);
            c.gridx = 3;
            panel.add(m_tfCounters[i], c);
            c.gridx = 4;
            panel.add(m_pbCounters[i], c);
        }

        return panel;
    }


    /**
     * registers the tool as observer for the text segment of the program
     */
    protected void addAsObserver() {
        addAsObserver(Memory.textBaseAddress, Memory.textLimitAddress);
    }

    // TODO: Port this to work with RISCV rather than MIPS
    /**
     * decodes the instruction and determines the category of the instruction.
     * <p>
     * The instruction is decoded by extracting the operation and function code of the 32-bit instruction.
     * Only the most relevant instructions are decoded and categorized.
     *
     * @param stmt the instruction to decode
     * @return the category of the instruction
     * @see InstructionStatistics#CATEGORY_ALU
     * @see InstructionStatistics#CATEGORY_JUMP
     * @see InstructionStatistics#CATEGORY_BRANCH
     * @see InstructionStatistics#CATEGORY_MEM
     * @see InstructionStatistics#CATEGORY_OTHER
     */
    protected int getInstructionCategory(ProgramStatement stmt) {
        int opCode = stmt.getBinaryStatement() >>> (32 - 6);
        int funct = stmt.getBinaryStatement() & 0x1F;

        if (opCode == 0x00) {
            if (funct == 0x00)
                return InstructionStatistics.CATEGORY_ALU; // sll
            if (0x02 <= funct && funct <= 0x07)
                return InstructionStatistics.CATEGORY_ALU; // srl, sra, sllv, srlv, srav
            if (funct == 0x08 || funct == 0x09)
                return InstructionStatistics.CATEGORY_JUMP; // jr, jalr
            if (0x10 <= funct && funct <= 0x2F)
                return InstructionStatistics.CATEGORY_ALU; // mfhi, mthi, mflo, mtlo, mult, multu, div, divu, add, addu, sub, subu, and, or, xor, nor, slt, sltu
            return InstructionStatistics.CATEGORY_OTHER;
        }
        if (opCode == 0x01) {
            if (0x00 <= funct && funct <= 0x07)
                return InstructionStatistics.CATEGORY_BRANCH; // bltz, bgez, bltzl, bgezl
            if (0x10 <= funct && funct <= 0x13)
                return InstructionStatistics.CATEGORY_BRANCH; // bltzal, bgezal, bltzall, bgczall
            return InstructionStatistics.CATEGORY_OTHER;
        }
        if (opCode == 0x02 || opCode == 0x03)
            return InstructionStatistics.CATEGORY_JUMP; // j, jal
        if (0x04 <= opCode && opCode <= 0x07)
            return InstructionStatistics.CATEGORY_BRANCH; // beq, bne, blez, bgtz
        if (0x08 <= opCode && opCode <= 0x0F)
            return InstructionStatistics.CATEGORY_ALU; // addi, addiu, slti, sltiu, andi, ori, xori, lui
        if (0x14 <= opCode && opCode <= 0x17)
            return InstructionStatistics.CATEGORY_BRANCH; // beql, bnel, blezl, bgtzl
        if (0x20 <= opCode && opCode <= 0x26)
            return InstructionStatistics.CATEGORY_MEM; // lb, lh, lwl, lw, lbu, lhu, lwr
        if (0x28 <= opCode && opCode <= 0x2E)
            return InstructionStatistics.CATEGORY_MEM; // sb, sh, swl, sw, swr

        return InstructionStatistics.CATEGORY_OTHER;
    }


    /**
     * method that is called each time the simulator accesses the text segment.
     * Before an instruction is executed by the simulator, the instruction is fetched from the program memory.
     * This memory access is observed and the corresponding instruction is decoded and categorized by the tool.
     * According to the category the counter values are increased and the display gets updated.
     *
     * @param resource the observed resource
     * @param notice   signals the type of access (memory, register etc.)
     */
    protected void processRISCVUpdate(Observable resource, AccessNotice notice) {

        if (!notice.accessIsFromRISCV())
            return;

        // check for a read access in the text segment
        if (notice.getAccessType() == AccessNotice.READ && notice instanceof MemoryAccessNotice) {

            // now it is safe to make a cast of the notice
            MemoryAccessNotice memAccNotice = (MemoryAccessNotice) notice;

            // The next three statments are from Felipe Lessa's instruction counter.  Prevents double-counting.
            int a = memAccNotice.getAddress();
            if (a == lastAddress)
                return;
            lastAddress = a;

            try {

                // access the statement in the text segment without notifying other tools etc.
                ProgramStatement stmt = Memory.getInstance().getStatementNoNotify(memAccNotice.getAddress());

                // necessary to handle possible null pointers at the end of the program
                // (e.g., if the simulator tries to execute the next instruction after the last instruction in the text segment)
                if (stmt != null) {
                    int category = getInstructionCategory(stmt);

                    m_totalCounter++;
                    m_counters[category]++;
                    updateDisplay();
                }
            } catch (AddressErrorException e) {
                // silently ignore these exceptions
            }
        }
    }


    /**
     * performs initialization tasks of the counters before the GUI is created.
     */
    protected void initializePreGUI() {
        m_totalCounter = 0;
        lastAddress = -1; // from Felipe Lessa's instruction counter tool
        for (int i = 0; i < InstructionStatistics.MAX_CATEGORY; i++)
            m_counters[i] = 0;
    }


    /**
     * resets the counter values of the tool and updates the display.
     */
    protected void reset() {
        m_totalCounter = 0;
        lastAddress = -1; // from Felipe Lessa's instruction counter tool
        for (int i = 0; i < InstructionStatistics.MAX_CATEGORY; i++)
            m_counters[i] = 0;
        updateDisplay();
    }


    /**
     * updates the text fields and progress bars according to the current counter values.
     */
    protected void updateDisplay() {
        m_tfTotalCounter.setText(String.valueOf(m_totalCounter));

        for (int i = 0; i < InstructionStatistics.MAX_CATEGORY; i++) {
            m_tfCounters[i].setText(String.valueOf(m_counters[i]));
            m_pbCounters[i].setMaximum(m_totalCounter);
            m_pbCounters[i].setValue(m_counters[i]);
        }
    }
}
