package mars.venus;

import mars.Globals;
import mars.Settings;
import mars.riscv.hardware.AccessNotice;
import mars.riscv.hardware.Register;
import mars.riscv.hardware.RegisterAccessNotice;
import mars.riscv.hardware.RegisterFile;
import mars.simulator.Simulator;
import mars.simulator.SimulatorNotice;
import mars.util.Binary;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
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
 * Sets up a window to display registers in the UI.
 *
 * @author Sanderson, Bumgarner
 **/

public class RegistersWindow extends JPanel implements Observer {
    private static JTable table;
    private static Register[] registers;
    private Object[][] tableData;
    private boolean highlighting;
    private int highlightRow;
    private ExecutePane executePane;
    private static final int NAME_COLUMN = 0;
    private static final int NUMBER_COLUMN = 1;
    private static final int VALUE_COLUMN = 2;
    private static Settings settings;

    /**
     * Constructor which sets up a fresh window with a table that contains the register values.
     **/

    public RegistersWindow() {
        Simulator.getInstance().addObserver(this);
        settings = Globals.getSettings();
        this.highlighting = false;
        table = new MyTippedJTable(new RegTableModel(setupWindow()));
        table.getColumnModel().getColumn(NAME_COLUMN).setPreferredWidth(25);
        table.getColumnModel().getColumn(NUMBER_COLUMN).setPreferredWidth(25);
        table.getColumnModel().getColumn(VALUE_COLUMN).setPreferredWidth(60);
        // Display register values (String-ified) right-justified in mono font
        table.getColumnModel().getColumn(NAME_COLUMN).setCellRenderer(new RegisterCellRenderer(MonoRightCellRenderer.MONOSPACED_PLAIN_12POINT, SwingConstants.LEFT));
        table.getColumnModel().getColumn(NUMBER_COLUMN).setCellRenderer(new RegisterCellRenderer(MonoRightCellRenderer.MONOSPACED_PLAIN_12POINT, SwingConstants.RIGHT));
        table.getColumnModel().getColumn(VALUE_COLUMN).setCellRenderer(new RegisterCellRenderer(MonoRightCellRenderer.MONOSPACED_PLAIN_12POINT, SwingConstants.RIGHT));
        table.setPreferredScrollableViewportSize(new Dimension(200, 700));
        this.setLayout(new BorderLayout()); // table display will occupy entire width if widened
        this.add(new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
    }

    /**
     * Sets up the data for the window.
     *
     * @return The array object with the data for the window.
     **/

    public Object[][] setupWindow() {
        int valueBase = NumberDisplayBaseChooser.getBase(settings.getBooleanSetting(Settings.DISPLAY_VALUES_IN_HEX));
        tableData = new Object[33][3];
        registers = RegisterFile.getRegisters();
        for (int i = 0; i < registers.length; i++) {
            tableData[i][0] = registers[i].getName();
            tableData[i][1] = registers[i].getNumber();
            tableData[i][2] = NumberDisplayBaseChooser.formatNumber(registers[i].getValue(), valueBase);
        }
        tableData[32][0] = "pc";
        tableData[32][1] = "";//new Integer(32);
        tableData[32][2] = NumberDisplayBaseChooser.formatUnsignedInteger(RegisterFile.getProgramCounter(), valueBase);

        return tableData;
    }

    /**
     * clear and redisplay registers
     */
    public void clearWindow() {
        this.clearHighlighting();
        RegisterFile.resetRegisters();
        this.updateRegisters(Globals.getGui().getMainPane().getExecutePane().getValueDisplayBase());
    }

    /**
     * Clear highlight background color from any cell currently highlighted.
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
     * update register display using current number base (10 or 16)
     */
    public void updateRegisters() {
        updateRegisters(Globals.getGui().getMainPane().getExecutePane().getValueDisplayBase());
    }

    /**
     * update register display using specified number base (10 or 16)
     *
     * @param base desired number base
     */
    public void updateRegisters(int base) {
        registers = RegisterFile.getRegisters();
        for (Register register : registers) {
            updateRegisterValue(register.getNumber(), register.getValue(), base);
        }
        updateRegisterUnsignedValue(32, RegisterFile.getProgramCounter(), base);
    }

    /**
     * This method handles the updating of the GUI.
     *
     * @param number The number of the register to update.
     * @param val    New value.
     **/

    public void updateRegisterValue(int number, int val, int base) {
        ((RegTableModel) table.getModel()).setDisplayAndModelValueAt(NumberDisplayBaseChooser.formatNumber(val, base), number, 2);
    }


    private void updateRegisterUnsignedValue(int number, int val, int base) {
        ((RegTableModel) table.getModel()).setDisplayAndModelValueAt(NumberDisplayBaseChooser.formatUnsignedInteger(val, base), number, 2);
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
                    RegisterFile.addRegistersObserver(this);
                    this.highlighting = true;
                }
            } else {
                // Simulated MIPS execution stops.  Stop responding.
                RegisterFile.deleteRegistersObserver(this);
            }
        } else if (obj instanceof RegisterAccessNotice) {
            // NOTE: each register is a separate Observable
            RegisterAccessNotice access = (RegisterAccessNotice) obj;
            if (access.getAccessType() == AccessNotice.WRITE) {
                // Uses the same highlighting technique as for Text Segment -- see
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
        // Tell the system that table contents have changed.  This will trigger re-rendering
        // during which cell renderers are obtained.  The row of interest (identified by
        // instance variabls this.registerRow) will get a renderer
        // with highlight background color and all others get renderer with default background.
        table.tableChanged(new TableModelEvent(table.getModel()));
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


    ////////////////////////////////////////////////////////////////////////////

    class RegTableModel extends AbstractTableModel {
        final String[] columnNames = {"Name", "Number", "Value"};
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
   * Don't need to implement this method unless your table's
   * editable.
   */
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            // these registers are not editable: $zero (0), $pc (32), $ra (31)
            return col == VALUE_COLUMN && row != 0 && row != 32 && row != 31;
        }


        /*
         * Update cell contents in table model.  This method should be called
      	* only when user edits cell, so input validation has to be done.  If
      	* value is valid, MIPS register is updated.
         */
        public void setValueAt(Object value, int row, int col) {
            int val = 0;
            try {
                val = Binary.stringToInt((String) value);
            } catch (NumberFormatException nfe) {
                data[row][col] = "INVALID";
                fireTableCellUpdated(row, col);
                return;
            }
            //  Assures that if changed during MIPS program execution, the update will
            //  occur only between instructions.
            synchronized (Globals.memoryAndRegistersLock) {
                RegisterFile.updateRegister(row, val);
            }
            int valueBase = Globals.getGui().getMainPane().getExecutePane().getValueDisplayBase();
            data[row][col] = NumberDisplayBaseChooser.formatNumber(val, valueBase);
            fireTableCellUpdated(row, col);
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
                /* zero */  "constant 0",
                /* ra   */  "return address (used by function call)",
                /* sp   */  "stack pointer",
                /* gp   */  "pointer to global area",
                /* tp   */  "pointer to thread local data (not given a value)",
                /* t0   */  "temporary (not preserved across call)",
                /* t1   */  "temporary (not preserved across call)",
                /* t2   */  "temporary (not preserved across call)",
                /* s0   */  "saved temporary (preserved across call)",
                /* s1   */  "saved temporary (preserved across call)",
                /* a0   */  "argument 1 / return 1",
                /* a1   */  "argument 2 / return 2",
                /* a2   */  "argument 3",
                /* a3   */  "argument 4",
                /* a4   */  "argument 5",
                /* a5   */  "argument 6",
                /* a6   */  "argument 7",
                /* a7   */  "argument 8",
                /* s2   */  "saved temporary (preserved across call)",
                /* s3   */  "saved temporary (preserved across call)",
                /* s4   */  "saved temporary (preserved across call)",
                /* s5   */  "saved temporary (preserved across call)",
                /* s6   */  "saved temporary (preserved across call)",
                /* s7   */  "saved temporary (preserved across call)",
                /* s8   */  "saved temporary (preserved across call)",
                /* s9   */  "saved temporary (preserved across call)",
                /* s10  */  "saved temporary (preserved across call)",
                /* s11  */  "saved temporary (preserved across call)",
                /* t3   */  "temporary (not preserved across call)",
                /* t4   */  "temporary (not preserved across call)",
                /* t5   */  "temporary (not preserved across call)",
                /* t6   */  "temporary (not preserved across call)",
                /* pc   */  "program counter",
        };

        //Implement table cell tool tips.
        public String getToolTipText(MouseEvent e) {
            String tip = null;
            java.awt.Point p = e.getPoint();
            int rowIndex = rowAtPoint(p);
            int colIndex = columnAtPoint(p);
            int realColumnIndex = convertColumnIndexToModel(colIndex);
            if (realColumnIndex == NAME_COLUMN) { //Register name column
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
            /* number */ "Corresponding register number",
            /* value */  "Current 32 bit value"
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