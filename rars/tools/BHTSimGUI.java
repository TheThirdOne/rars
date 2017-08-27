/*
Copyright (c) 2009,  Ingo Kofler, ITEC, Klagenfurt University, Austria

Developed by Ingo Kofler (ingo.kofler@itec.uni-klu.ac.at)

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

package rars.tools;//.bhtsim;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.Vector;

/**
 * Represents the GUI of the BHT Simulator Tool.
 * <p>
 * <p>
 * The GUI consists of mainly four parts:
 * <ul>
 * <li>A configuration panel to select the number of entries and the history size
 * <li>A information panel that displays the most recent branch instruction including its address and BHT index
 * <li>A table representing the BHT with all entries and their internal state and statistics
 * <li>A log panel that summarizes the predictions in a textual form
 * </ul>
 *
 * @author ingo.kofler@itec.uni-klu.ac.at
 */
//@SuppressWarnings("serial")
public class BHTSimGUI extends JPanel {

    /**
     * text field presenting the most recent branch instruction
     */
    private JTextField m_tfInstruction;

    /**
     * text field representing the address of the most recent branch instruction
     */
    private JTextField m_tfAddress;

    /**
     * text field representing the resulting BHT index of the branch instruction
     */
    private JTextField m_tfIndex;

    /**
     * combo box for selecting the number of BHT entries
     */
    private JComboBox<Integer> m_cbBHTentries;

    /**
     * combo box for selecting the history size
     */
    private JComboBox<Integer> m_cbBHThistory;

    /**
     * combo box for selecting the initial value
     */
    private JComboBox<String> m_cbBHTinitVal;

    /**
     * the table representing the BHT
     */
    private JTable m_tabBHT;

    /**
     * text field for log output
     */
    private JTextArea m_taLog;

    /**
     * constant for the color that highlights the current BHT entry
     */
    public final static Color COLOR_PREPREDICTION = Color.yellow;

    /**
     * constant for the color to signal a correct prediction
     */
    public final static Color COLOR_PREDICTION_CORRECT = Color.green;

    /**
     * constant for the color to signal a misprediction
     */
    public final static Color COLOR_PREDICTION_INCORRECT = Color.red;

    /**
     * constant for the String representing "take the branch"
     */
    public final static String BHT_TAKE_BRANCH = "TAKE";

    /**
     * constant for the String representing "do not take the branch"
     */
    public final static String BHT_DO_NOT_TAKE_BRANCH = "NOT TAKE";


    /**
     * Creates the GUI components of the BHT Simulator
     * The GUI is a subclass of JPanel which is integrated in the GUI of the MARS tool
     */
    public BHTSimGUI() {
        BorderLayout layout = new BorderLayout();
        layout.setVgap(10);
        layout.setHgap(10);
        setLayout(layout);

        m_tabBHT = createAndInitTable();

        add(buildConfigPanel(), BorderLayout.NORTH);
        add(buildInfoPanel(), BorderLayout.WEST);
        add(new JScrollPane(m_tabBHT), BorderLayout.CENTER);
        add(buildLogPanel(), BorderLayout.SOUTH);
    }

    /**
     * Creates and initializes the JTable representing the BHT.
     *
     * @return the JTable representing the BHT
     */
    private JTable createAndInitTable() {
        // create the table
        JTable theTable = new JTable();

        // create a default renderer for double values (percentage)
        DefaultTableCellRenderer doubleRenderer = new DefaultTableCellRenderer() {
            private DecimalFormat formatter = new DecimalFormat("##0.00");

            public void setValue(Object value) {
                setText((value == null) ? "" : formatter.format(value));
            }
        };
        doubleRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        // create a default renderer for all other values with center alignment
        DefaultTableCellRenderer defRenderer = new DefaultTableCellRenderer();
        defRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        theTable.setDefaultRenderer(Double.class, doubleRenderer);
        theTable.setDefaultRenderer(Integer.class, defRenderer);
        theTable.setDefaultRenderer(String.class, defRenderer);

        theTable.setSelectionBackground(BHTSimGUI.COLOR_PREPREDICTION);
        theTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        return theTable;

    }


    /**
     * Creates and initializes the panel holding the instruction, address and index text fields.
     *
     * @return the info panel
     */
    private JPanel buildInfoPanel() {
        m_tfInstruction = new JTextField();
        m_tfAddress = new JTextField();
        m_tfIndex = new JTextField();

        m_tfInstruction.setColumns(10);
        m_tfInstruction.setEditable(false);
        m_tfInstruction.setHorizontalAlignment(JTextField.CENTER);
        m_tfAddress.setColumns(10);
        m_tfAddress.setEditable(false);
        m_tfAddress.setHorizontalAlignment(JTextField.CENTER);
        m_tfIndex.setColumns(10);
        m_tfIndex.setEditable(false);
        m_tfIndex.setHorizontalAlignment(JTextField.CENTER);

        JPanel panel = new JPanel();
        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new BorderLayout());

        GridBagLayout gbl = new GridBagLayout();
        panel.setLayout(gbl);

        GridBagConstraints c = new GridBagConstraints();

        c.insets = new Insets(5, 5, 2, 5);
        c.gridx = 1;
        c.gridy = 1;

        panel.add(new JLabel("Instruction"), c);
        c.gridy++;
        panel.add(m_tfInstruction, c);
        c.gridy++;
        panel.add(new JLabel("@ Address"), c);
        c.gridy++;
        panel.add(m_tfAddress, c);
        c.gridy++;
        panel.add(new JLabel("-> Index"), c);
        c.gridy++;
        panel.add(m_tfIndex, c);

        outerPanel.add(panel, BorderLayout.NORTH);
        return outerPanel;
    }


    /**
     * Creates and initializes the panel for the configuration of the tool
     * The panel contains two combo boxes for selecting the number of BHT entries and the history size.
     *
     * @return a panel for the configuration
     */
    private JPanel buildConfigPanel() {
        JPanel panel = new JPanel();

        Vector<Integer> sizes = new Vector<>();
        sizes.add(8);
        sizes.add(16);
        sizes.add(32);

        Vector<Integer> bits = new Vector<>();
        bits.add(1);
        bits.add(2);

        Vector<String> initVals = new Vector<>();
        initVals.add(BHTSimGUI.BHT_DO_NOT_TAKE_BRANCH);
        initVals.add(BHTSimGUI.BHT_TAKE_BRANCH);

        m_cbBHTentries = new JComboBox<>(sizes);
        m_cbBHThistory = new JComboBox<>(bits);
        m_cbBHTinitVal = new JComboBox<>(initVals);

        panel.add(new JLabel("# of BHT entries"));
        panel.add(m_cbBHTentries);
        panel.add(new JLabel("BHT history size"));
        panel.add(m_cbBHThistory);
        panel.add(new JLabel("Initial value"));
        panel.add(m_cbBHTinitVal);

        return panel;
    }


    /**
     * Creates and initializes the panel containing the log text area.
     *
     * @return the panel for the logging output
     */
    private JPanel buildLogPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        m_taLog = new JTextArea();
        m_taLog.setRows(6);
        m_taLog.setEditable(false);

        panel.add(new JLabel("Log"), BorderLayout.NORTH);
        panel.add(new JScrollPane(m_taLog), BorderLayout.CENTER);

        return panel;
    }


    /***
     * Returns the combo box for selecting the number of BHT entries.
     *
     * @return the reference to the combo box
     */
    public JComboBox<Integer> getCbBHTentries() {
        return m_cbBHTentries;
    }


    /***
     * Returns the combo box for selecting the size of the BHT history.
     *
     * @return the reference to the combo box
     */
    public JComboBox<Integer> getCbBHThistory() {
        return m_cbBHThistory;
    }


    /***
     * Returns the combo box for selecting the initial value of the BHT
     *
     * @return the reference to the combo box
     */
    public JComboBox<String> getCbBHTinitVal() {
        return m_cbBHTinitVal;
    }

    /***
     * Returns the table representing the BHT.
     *
     * @return the reference to the table
     */
    public JTable getTabBHT() {
        return m_tabBHT;
    }


    /***
     * Returns the text area for log purposes.
     *
     * @return the reference to the text area
     */
    public JTextArea getTaLog() {
        return m_taLog;
    }


    /***
     * Returns the text field for displaying the most recent branch instruction
     *
     * @return the reference to the text field
     */
    public JTextField getTfInstruction() {
        return m_tfInstruction;
    }


    /***
     * Returns the text field for displaying the address of the most recent branch instruction
     *
     * @return the reference to the text field
     */
    public JTextField getTfAddress() {
        return m_tfAddress;
    }


    /***
     * Returns the text field for displaying the corresponding index into the BHT
     *
     * @return the reference to the text field
     */
    public JTextField getTfIndex() {
        return m_tfIndex;
    }


}
