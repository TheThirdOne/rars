package mars.venus;

import mars.Globals;
import mars.Settings;
import mars.mips.hardware.*;
import mars.simulator.Simulator;
import mars.simulator.SimulatorNotice;
import mars.util.Binary;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Observable;
import java.util.Observer;

/*
Copyright (c) 2003-2009,  Pete Sanderson and Kenneth Vollmar

Developed by Pete Sanderson (psanderson@otterbein.edu)
and Kenneth Vollmar (kenvollmar@missouristate.edu)

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

/**
 * Sets up a window to display Coprocessor 1 registers in the Registers pane of the UI.
 *
 * @author Pete Sanderson 2005
 **/

public class Coprocessor1Window extends JPanel implements ActionListener, Observer {
    private static JTable table;
    private static Register[] registers;
    private Object[][] tableData;
    private boolean highlighting;
    private int highlightRow;
    private ExecutePane executePane;
    private JCheckBox[] conditionFlagCheckBox;
    private static final int NAME_COLUMN = 0;
    private static final int FLOAT_COLUMN = 1;
    private static final int DOUBLE_COLUMN = 2;
    private static Settings settings;

    /**
     * Constructor which sets up a fresh window with a table that contains the register values.
     **/

    public Coprocessor1Window() {
        Simulator.getInstance().addObserver(this);
        settings = Globals.getSettings();
        // Display registers in table contained in scroll pane.
        this.setLayout(new BorderLayout()); // table display will occupy entire width if widened
        table = new MyTippedJTable(new RegTableModel(setupWindow()));
        table.getColumnModel().getColumn(NAME_COLUMN).setPreferredWidth(20);
        table.getColumnModel().getColumn(FLOAT_COLUMN).setPreferredWidth(70);
        table.getColumnModel().getColumn(DOUBLE_COLUMN).setPreferredWidth(130);
        // Display register values (String-ified) right-justified in mono font
        table.getColumnModel().getColumn(NAME_COLUMN).setCellRenderer(new RegisterCellRenderer(MonoRightCellRenderer.MONOSPACED_PLAIN_12POINT, SwingConstants.LEFT));
        table.getColumnModel().getColumn(FLOAT_COLUMN).setCellRenderer(new RegisterCellRenderer(MonoRightCellRenderer.MONOSPACED_PLAIN_12POINT, SwingConstants.RIGHT));
        table.getColumnModel().getColumn(DOUBLE_COLUMN).setCellRenderer(new RegisterCellRenderer(MonoRightCellRenderer.MONOSPACED_PLAIN_12POINT, SwingConstants.RIGHT));
        this.add(new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        // Display condition flags in panel below the registers
        JPanel flagsPane = new JPanel(new BorderLayout());
        flagsPane.setToolTipText(
                "flags are used by certain floating point instructions, default flag is 0");
        flagsPane.add(new JLabel("Condition Flags", JLabel.CENTER), BorderLayout.NORTH);
        int numFlags = Coprocessor1.getConditionFlagCount();
        conditionFlagCheckBox = new JCheckBox[numFlags];
        JPanel checksPane = new JPanel(new GridLayout(2, numFlags / 2));
        // Tried to get interior of checkboxes to be white while its label and
        // remaining background stays same background color.  Found example
        // like the following on the web, but does not appear to have any
        // affect.  Might be worth further study but for now I'll just set
        // background to white.  I want white so the checkbox appears
        // "responsive" to user clicking on it (it is responsive anyway but looks
        // dead when drawn in gray.
        //Object saveBG = UIManager.getColor("CheckBox.interiorBackground");
        //UIManager.put("CheckBox.interiorBackground", Color.WHITE);
        for (int i = 0; i < numFlags; i++) {
            conditionFlagCheckBox[i] = new JCheckBox(Integer.toString(i));
            conditionFlagCheckBox[i].addActionListener(this);
            conditionFlagCheckBox[i].setBackground(Color.WHITE);
            conditionFlagCheckBox[i].setToolTipText("checked == 1, unchecked == 0");
            checksPane.add(conditionFlagCheckBox[i]);
        }
        //UIManager.put("CheckBox.interiorBackground", saveBG);
        flagsPane.add(checksPane, BorderLayout.CENTER);
        this.add(flagsPane, BorderLayout.SOUTH);
    }

    /**
     * Called when user clicks on a condition flag checkbox.
     * Updates both the display and the underlying Coprocessor 1 flag.
     *
     * @param e component that triggered this call
     */
    public void actionPerformed(ActionEvent e) {
        JCheckBox checkBox = (JCheckBox) e.getSource();
        int i = Integer.parseInt(checkBox.getText());
        if (checkBox.isSelected()) {
            checkBox.setSelected(true);
            Coprocessor1.setConditionFlag(i);
        } else {
            checkBox.setSelected(false);
            Coprocessor1.clearConditionFlag(i);
        }
    }

    /**
     * Sets up the data for the window.
     *
     * @return The array object with the data for the window.
     **/

    private Object[][] setupWindow() {
        registers = Coprocessor1.getRegisters();
        this.highlighting = false;
        tableData = new Object[registers.length][3];
        for (int i = 0; i < registers.length; i++) {
            tableData[i][0] = registers[i].getName();
            tableData[i][1] = NumberDisplayBaseChooser.formatFloatNumber(registers[i].getValue(),
                    NumberDisplayBaseChooser.getBase(settings.getBooleanSetting(Settings.DISPLAY_VALUES_IN_HEX)));//formatNumber(floatValue,NumberDisplayBaseChooser.getBase(settings.getDisplayValuesInHex()));
            if (i % 2 == 0) { // even numbered double registers
                long longValue = 0;
                try {
                    longValue = Coprocessor1.getLongFromRegisterPair(registers[i].getName());
                } catch (InvalidRegisterAccessException e) {
                } // cannot happen since i must be even
                tableData[i][2] = NumberDisplayBaseChooser.formatDoubleNumber(longValue,
                        NumberDisplayBaseChooser.getBase(settings.getBooleanSetting(Settings.DISPLAY_VALUES_IN_HEX)));
            } else {
                tableData[i][2] = "";
            }
        }
        return tableData;
    }

    /**
     * Reset and redisplay registers.
     */
    public void clearWindow() {
        this.clearHighlighting();
        Coprocessor1.resetRegisters();
        this.updateRegisters(Globals.getGui().getMainPane().getExecutePane().getValueDisplayBase());
        Coprocessor1.clearConditionFlags();
        this.updateConditionFlagDisplay();
    }

    /**
     * Clear highlight background color from any row currently highlighted.
     */
    public void clearHighlighting() {
        highlighting = false;
        if (table != null) {
            table.tableChanged(new TableModelEvent(table.getModel()));
        }
        highlightRow = -1; // assure highlight will not occur upon re-assemble.
    }

    /**
     * Refresh the table, triggering re-rendering.
     */
    public void refresh() {
        if (table != null) {
            table.tableChanged(new TableModelEvent(table.getModel()));
        }
    }

    /**
     * Redisplay registers using current display number base (10 or 16)
     */
    public void updateRegisters() {
        updateRegisters(Globals.getGui().getMainPane().getExecutePane().getValueDisplayBase());
    }

    /**
     * Redisplay registers using specified display number base (10 or 16)
     *
     * @param base number base for display (10 or 16)
     */
    public void updateRegisters(int base) {
        registers = Coprocessor1.getRegisters();
        for (int i = 0; i < registers.length; i++) {
            updateFloatRegisterValue(registers[i].getNumber(), registers[i].getValue(), base);
            if (i % 2 == 0) {
                updateDoubleRegisterValue(i, base);
            }
        }
        updateConditionFlagDisplay();
    }

    private void updateConditionFlagDisplay() {
        for (int i = 0; i < conditionFlagCheckBox.length; i++) {
            conditionFlagCheckBox[i].setSelected(Coprocessor1.getConditionFlag(i) != 0);
        }
    }

    /**
     * This method handles the updating of the GUI.  Does not affect actual register.
     *
     * @param number The number of the float register whose display to update.
     * @param val    New value.
     * @param base   the number base for display (e.g. 10, 16)
     **/

    public void updateFloatRegisterValue(int number, int val, int base) {
        ((RegTableModel) table.getModel()).setDisplayAndModelValueAt(NumberDisplayBaseChooser.formatFloatNumber(val, base), number, FLOAT_COLUMN);

    }


    /**
     * This method handles the updating of the GUI.  Does not affect actual register.
     *
     * @param number The number of the double register to update.
     * @param base   the number base for display (e.g. 10, 16)
     **/
    public void updateDoubleRegisterValue(int number, int base) {
        long val = 0;
        try {
            val = Coprocessor1.getLongFromRegisterPair(registers[number].getName());
        } catch (InvalidRegisterAccessException e) {
        } // happens only if number is not even
        ((RegTableModel) table.getModel()).setDisplayAndModelValueAt(NumberDisplayBaseChooser.formatDoubleNumber(val, base), number, DOUBLE_COLUMN);
    }

    /**
     * Required by Observer interface.  Called when notified by an Observable that we are registered with.
     * Observables include:
     * The Simulator object, which lets us know when it starts and stops running
     * A register object, which lets us know of register operations
     * The Simulator keeps us informed of when simulated MIPS execution is active.
     * This is the only time we care about register operations.
     *
     * @param observable The Observable object who is notifying us
     * @param obj        Auxiliary object with additional information.
     */
    public void update(Observable observable, Object obj) {
        if (observable == mars.simulator.Simulator.getInstance()) {
            SimulatorNotice notice = (SimulatorNotice) obj;
            if (notice.getAction() == SimulatorNotice.SIMULATOR_START) {
                // Simulated MIPS execution starts.  Respond to memory changes if running in timed
                // or stepped mode.
                if (notice.getRunSpeed() != RunSpeedPanel.UNLIMITED_SPEED || notice.getMaxSteps() == 1) {
                    Coprocessor1.addRegistersObserver(this);
                    this.highlighting = true;
                }
            } else {
                // Simulated MIPS execution stops.  Stop responding.
                Coprocessor1.deleteRegistersObserver(this);
            }
        } else if (obj instanceof RegisterAccessNotice) {
            // NOTE: each register is a separate Observable
            RegisterAccessNotice access = (RegisterAccessNotice) obj;
            if (access.getAccessType() == AccessNotice.WRITE) {
                // For now, use highlighting technique used by Label Window feature to highlight
                // memory cell corresponding to a selected label.  The highlighting is not
                // as visually distinct as changing the background color, but will do for now.
                // Ideally, use the same highlighting technique as for Text Segment -- see
                // AddressCellRenderer class in DataSegmentWindow.java.
                this.highlighting = true;
                this.highlightCellForRegister((Register) observable);
                Globals.getGui().getRegistersPane().setSelectedComponent(this);
            }
        }
    }

    /**
     * Highlight the row corresponding to the given register.
     *
     * @param register Register object corresponding to row to be selected.
     */
    void highlightCellForRegister(Register register) {
        this.highlightRow = register.getNumber();
        table.tableChanged(new TableModelEvent(table.getModel()));
          /*
         int registerColumn = FLOAT_COLUMN;
         registerColumn = table.convertColumnIndexToView(registerColumn); 
         Rectangle registerCell = table.getCellRect(registerRow, registerColumn, true);
         // STEP 2:  Select the cell by generating a fake Mouse Pressed event and 
      	// explicitly invoking the table's mouse listener.
         MouseEvent fakeMouseEvent = new MouseEvent(table, MouseEvent.MOUSE_PRESSED,
                                                    new Date().getTime(), MouseEvent.BUTTON1_MASK,
            													 (int)registerCell.getX()+1,
            													 (int)registerCell.getY()+1, 1, false);
         MouseListener[] mouseListeners = table.getMouseListeners();
         for (int i=0; i<mouseListeners.length; i++) {
            mouseListeners[i].mousePressed(fakeMouseEvent);
         }
      	*/
    }

    /*
    * Cell renderer for displaying register entries.  This does highlighting, so if you
    * don't want highlighting for a given column, don't use this.  Currently we highlight
    * all columns.
    */
    private class RegisterCellRenderer extends DefaultTableCellRenderer {
        private Font font;
        private int alignment;

        public RegisterCellRenderer(Font font, int alignment) {
            super();
            this.font = font;
            this.alignment = alignment;
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel cell = (JLabel) super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);
            cell.setFont(font);
            cell.setHorizontalAlignment(alignment);
            if (settings.getBooleanSetting(Settings.REGISTERS_HIGHLIGHTING) && highlighting && row == highlightRow) {
                cell.setBackground(settings.getColorSettingByPosition(Settings.REGISTER_HIGHLIGHT_BACKGROUND));
                cell.setForeground(settings.getColorSettingByPosition(Settings.REGISTER_HIGHLIGHT_FOREGROUND));
                cell.setFont(settings.getFontByPosition(Settings.REGISTER_HIGHLIGHT_FONT));
            } else if (row % 2 == 0) {
                cell.setBackground(settings.getColorSettingByPosition(Settings.EVEN_ROW_BACKGROUND));
                cell.setForeground(settings.getColorSettingByPosition(Settings.EVEN_ROW_FOREGROUND));
                cell.setFont(settings.getFontByPosition(Settings.EVEN_ROW_FONT));
            } else {
                cell.setBackground(settings.getColorSettingByPosition(Settings.ODD_ROW_BACKGROUND));
                cell.setForeground(settings.getColorSettingByPosition(Settings.ODD_ROW_FOREGROUND));
                cell.setFont(settings.getFontByPosition(Settings.ODD_ROW_FONT));
            }
            return cell;
        }
    }


    /////////////////////////////////////////////////////////////////////////////
    //  The table model.

    class RegTableModel extends AbstractTableModel {
        final String[] columnNames = {"Name", "Float", "Double"};
        Object[][] data;

        public RegTableModel(Object[][] d) {
            data = d;
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return data.length;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            return data[row][col];
        }

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  
      	*/
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        /*
         * Float column and even-numbered rows of double column are editable. 
         */
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            return col == FLOAT_COLUMN || (col == DOUBLE_COLUMN && row % 2 == 0);
        }


        /*
         * Update cell contents in table model.  This method should be called
      	* only when user edits cell, so input validation has to be done.  If
      	* value is valid, MIPS register is updated.
         */
        public void setValueAt(Object value, int row, int col) {
            int valueBase = Globals.getGui().getMainPane().getExecutePane().getValueDisplayBase();
            float fVal;
            double dVal;
            String sVal = (String) value;
            try {
                if (col == FLOAT_COLUMN) {
                    if (Binary.isHex(sVal)) {
                        // Avoid using Float.intBitsToFloat() b/c it may not preserve NaN value.
                        int iVal = Binary.stringToInt(sVal);
                        //  Assures that if changed during MIPS program execution, the update will
                        //  occur only between MIPS instructions.
                        synchronized (Globals.memoryAndRegistersLock) {
                            Coprocessor1.updateRegister(row, iVal);
                        }
                        data[row][col] = NumberDisplayBaseChooser.formatFloatNumber(iVal, valueBase);

                    } else {
                        fVal = Float.parseFloat(sVal);
                        //  Assures that if changed during MIPS program execution, the update will
                        //  occur only between MIPS instructions.
                        synchronized (Globals.memoryAndRegistersLock) {
                            Coprocessor1.setRegisterToFloat(row, fVal);
                        }
                        data[row][col] = NumberDisplayBaseChooser.formatNumber(fVal, valueBase);
                    }
                    // have to update corresponding double display
                    int dReg = row - (row % 2);
                    setDisplayAndModelValueAt(
                            NumberDisplayBaseChooser.formatDoubleNumber(Coprocessor1.getLongFromRegisterPair(dReg), valueBase), dReg, DOUBLE_COLUMN);
                } else if (col == DOUBLE_COLUMN) {
                    if (Binary.isHex(sVal)) {
                        long lVal = Binary.stringToLong(sVal);
                        //  Assures that if changed during MIPS program execution, the update will
                        //  occur only between MIPS instructions.
                        synchronized (Globals.memoryAndRegistersLock) {
                            Coprocessor1.setRegisterPairToLong(row, lVal);
                        }
                        setDisplayAndModelValueAt(
                                NumberDisplayBaseChooser.formatDoubleNumber(lVal, valueBase), row, col);
                    } else { // is not hex, so must be decimal
                        dVal = Double.parseDouble(sVal);
                        //  Assures that if changed during MIPS program execution, the update will
                        //  occur only between MIPS instructions.
                        synchronized (Globals.memoryAndRegistersLock) {
                            Coprocessor1.setRegisterPairToDouble(row, dVal);
                        }
                        setDisplayAndModelValueAt(
                                NumberDisplayBaseChooser.formatNumber(dVal, valueBase), row, col);
                    }
                    // have to update corresponding float display
                    setDisplayAndModelValueAt(
                            NumberDisplayBaseChooser.formatNumber(Coprocessor1.getValue(row), valueBase), row, FLOAT_COLUMN);
                    setDisplayAndModelValueAt(
                            NumberDisplayBaseChooser.formatNumber(Coprocessor1.getValue(row + 1), valueBase), row + 1, FLOAT_COLUMN);
                }
            } catch (NumberFormatException nfe) {
                data[row][col] = "INVALID";
                fireTableCellUpdated(row, col);
            } catch (InvalidRegisterAccessException e) {
                // Should not occur; code below will re-display original value
                fireTableCellUpdated(row, col);
            }
        }


        /**
         * Update cell contents in table model.  Does not affect MIPS register.
         */
        private void setDisplayAndModelValueAt(Object value, int row, int col) {
            data[row][col] = value;
            fireTableCellUpdated(row, col);
        }


        // handy for debugging....
        private void printDebugData() {
            int numRows = getRowCount();
            int numCols = getColumnCount();

            for (int i = 0; i < numRows; i++) {
                System.out.print("    row " + i + ":");
                for (int j = 0; j < numCols; j++) {
                    System.out.print("  " + data[i][j]);
                }
                System.out.println();
            }
            System.out.println("--------------------------");
        }
    }

    ///////////////////////////////////////////////////////////////////
    //
    // JTable subclass to provide custom tool tips for each of the
    // register table column headers and for each register name in
    // the first column. From Sun's JTable tutorial.
    // http://java.sun.com/docs/books/tutorial/uiswing/components/table.html
    //
    private class MyTippedJTable extends JTable {
        MyTippedJTable(RegTableModel m) {
            super(m);
            this.setRowSelectionAllowed(true); // highlights background color of entire row
            this.setSelectionBackground(Color.GREEN);
        }

        private String[] regToolTips = {
            /* $f0  */  "floating point subprogram return value",  
            /* $f1  */  "should not be referenced explicitly in your program",
            /* $f2  */  "floating point subprogram return value",
            /* $f3  */  "should not be referenced explicitly in your program",
            /* $f4  */  "temporary (not preserved across call)",
            /* $f5  */  "should not be referenced explicitly in your program",
            /* $f6  */  "temporary (not preserved across call)",
            /* $f7  */  "should not be referenced explicitly in your program",
            /* $f8  */  "temporary (not preserved across call)",
            /* $f9  */  "should not be referenced explicitly in your program",
            /* $f10 */  "temporary (not preserved across call)",
            /* $f11 */  "should not be referenced explicitly in your program",
            /* $f12 */  "floating point subprogram argument 1",
            /* $f13 */  "should not be referenced explicitly in your program",
            /* $f14 */  "floating point subprogram argument 2",
            /* $f15 */  "should not be referenced explicitly in your program",
            /* $f16 */  "temporary (not preserved across call)",
            /* $f17 */  "should not be referenced explicitly in your program",
            /* $f18 */  "temporary (not preserved across call)",
            /* $f19 */  "should not be referenced explicitly in your program",
            /* $f20 */  "saved temporary (preserved across call)",  
            /* $f21 */  "should not be referenced explicitly in your program",
            /* $f22 */  "saved temporary (preserved across call)",
            /* $f23 */  "should not be referenced explicitly in your program",
            /* $f24 */  "saved temporary (preserved across call)",
            /* $f25 */  "should not be referenced explicitly in your program",
            /* $f26 */  "saved temporary (preserved across call)",
            /* $f27 */  "should not be referenced explicitly in your program",
            /* $f28 */  "saved temporary (preserved across call)",
            /* $f29 */  "should not be referenced explicitly in your program",
            /* $f30 */  "saved temporary (preserved across call)",  
            /* $f31 */  "should not be referenced explicitly in your program"
        };

        //Implement table cell tool tips.
        public String getToolTipText(MouseEvent e) {
            String tip = null;
            java.awt.Point p = e.getPoint();
            int rowIndex = rowAtPoint(p);
            int colIndex = columnAtPoint(p);
            int realColumnIndex = convertColumnIndexToModel(colIndex);
            if (realColumnIndex == NAME_COLUMN) {
                tip = regToolTips[rowIndex];
            /* You can customize each tip to encorporiate cell contents if you like:
               TableModel model = getModel();
               String regName = (String)model.getValueAt(rowIndex,0);
            	....... etc .......
            */
            } else {
                //You can omit this part if you know you don't have any
                //renderers that supply their own tool tips.
                tip = super.getToolTipText(e);
            }
            return tip;
        }

        private String[] columnToolTips = {
            /* name */   "Each register has a tool tip describing its usage convention",
            /* float */ "32-bit single precision IEEE 754 floating point register",
            /* double */  "64-bit double precision IEEE 754 floating point register (uses a pair of 32-bit registers)"
        };

        //Implement table header tool tips.
        protected JTableHeader createDefaultTableHeader() {
            return
                    new JTableHeader(columnModel) {
                        public String getToolTipText(MouseEvent e) {
                            String tip = null;
                            java.awt.Point p = e.getPoint();
                            int index = columnModel.getColumnIndexAtX(p.x);
                            int realIndex = columnModel.getColumn(index).getModelIndex();
                            return columnToolTips[realIndex];
                        }
                    };
        }
    }

}