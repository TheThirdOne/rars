package rars.venus.registers;

import rars.Globals;
import rars.Settings;
import rars.riscv.hardware.AccessNotice;
import rars.riscv.hardware.Register;
import rars.riscv.hardware.RegisterAccessNotice;
import rars.simulator.Simulator;
import rars.simulator.SimulatorNotice;
import rars.util.Binary;
import rars.venus.MonoRightCellRenderer;
import rars.venus.NumberDisplayBaseChooser;
import rars.venus.run.RunSpeedPanel;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

/**
 * Sets up a window to display registers in the UI.
 *
 * @author Sanderson, Bumgarner
 **/

public abstract class RegisterBlockWindow extends JPanel implements Observer {
    private JTable table;
    private boolean highlighting;
    private RegistersAccessNotice stepAccessNotices;    //contains the register access notices for the current step
    private Register[] registers;

    private static final int NAME_COLUMN = 0;
    private static final int NUMBER_COLUMN = 1;
    private static final int VALUE_COLUMN = 2;

    private Settings settings;


    /**
     * Constructor which sets up a fresh window with a table that contains the register values.
     **/
    public RegisterBlockWindow(Register[] registers, String[] registerDescriptions, String valueTip) {
        stepAccessNotices = new RegistersAccessNotice();
        Simulator.getInstance().addObserver(this);
        settings = Globals.getSettings();
        settings.addObserver(this);
        this.registers = registers;
        clearHighlighting();
        table = new MyTippedJTable(new RegTableModel(setupWindow()), registerDescriptions,
                new String[]{"Each register has a tool tip describing its usage convention", "Corresponding register number", valueTip}) {
        };
        updateRowHeight();
        table.getColumnModel().getColumn(NAME_COLUMN).setPreferredWidth(20);
        table.getColumnModel().getColumn(NUMBER_COLUMN).setPreferredWidth(10);
        table.getColumnModel().getColumn(VALUE_COLUMN).setPreferredWidth(150);

        // Display register values (String-ified) right-justified in mono font
        table.getColumnModel().getColumn(NAME_COLUMN).setCellRenderer(new RegisterCellRenderer(MonoRightCellRenderer.MONOSPACED_PLAIN_12POINT, SwingConstants.LEFT));
        table.getColumnModel().getColumn(NUMBER_COLUMN).setCellRenderer(new RegisterCellRenderer(MonoRightCellRenderer.MONOSPACED_PLAIN_12POINT, SwingConstants.RIGHT));
        table.getColumnModel().getColumn(VALUE_COLUMN).setCellRenderer(new RegisterCellRenderer(MonoRightCellRenderer.MONOSPACED_PLAIN_12POINT, SwingConstants.RIGHT));
        table.setPreferredScrollableViewportSize(new Dimension(200, 700));
        this.setLayout(new BorderLayout());  // table display will occupy entire width if widened
        this.add(new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
    }

    protected abstract String formatRegister(Register value, int base);

    protected abstract void beginObserving();

    protected abstract void endObserving();

    protected abstract void resetRegisters();
    /**
     * Sets up the data for the window.
     *
     * @return The array object with the data for the window.
     **/

    private Object[][] setupWindow() {
        Object[][] tableData = new Object[registers.length][3];
        for (int i = 0; i < registers.length; i++) {
            tableData[i][0] = registers[i].getName();
            int temp = registers[i].getNumber();
            tableData[i][1] = temp == -1 ? "" : temp;
            tableData[i][2] = formatRegister(registers[i],
                    NumberDisplayBaseChooser.getBase(settings.getBooleanSetting(Settings.Bool.DISPLAY_VALUES_IN_HEX)));
        }
        return tableData;
    }
    /**
     * Reset and redisplay registers
     */
    public void clearWindow() {
        clearHighlighting();
        resetRegisters();
        updateRegisters();
    }
    /**
     * Clear highlight background color from any row currently highlighted.
     */
    public void clearHighlighting() {
        highlighting = false;
        if (table != null) {
            table.tableChanged(new TableModelEvent(table.getModel()));
        }
        stepAccessNotices.clear();
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
     * Update register display using specified display base
     */
    public void updateRegisters() {
        for (int i = 0; i < registers.length; i++) {
            ((RegTableModel) table.getModel()).setDisplayAndModelValueAt(formatRegister(registers[i],
                    Globals.getGui().getMainPane().getExecutePane().getValueDisplayBase()), i, 2);
        }
    }

    /**
     * Adds the given register to the registers to highlight and updates highlighting
     * for the current step
     *
     * @param register Register object corresponding to row to be selected.
     * @param accessType the type of access: Write is 0, Read is 1 as in the AccessNotice
     */
    private void addRegisterHighlighting(Register register, int accessType) {
        stepAccessNotices.add(getRow(register), accessType);
        table.tableChanged(new TableModelEvent(table.getModel()));
    }

    /**
     * Finds the row that contains the given register
     *
     * @param register Register object corresponding to a certain row
     * @return  Index of row that contains the given register,
     *          or -1 if register not found
     */
    private int getRow(Register register) {
        for (int i = 0; i < registers.length; i++) {
            if (registers[i] == register) {
                return i;
            }
        }
        return -1;
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
        if (observable == rars.simulator.Simulator.getInstance()) {
            SimulatorNotice notice = (SimulatorNotice) obj;
            if (notice.getAction() == SimulatorNotice.SIMULATOR_START) {
                // Simulated MIPS execution starts.  Respond to memory changes if running in timed
                // or stepped mode.
                if (notice.getRunSpeed() != RunSpeedPanel.UNLIMITED_SPEED || notice.getMaxSteps() == 1) {
                    beginObserving();
                    this.highlighting = true;
                }
            } else {
                // Simulated MIPS execution stops.  Stop responding.
                endObserving();
            }
        } else if (observable == settings) {
            updateRowHeight();
        } else if (obj instanceof RegisterAccessNotice) {
            // NOTE: each register is a separate Observable
            RegisterAccessNotice access = (RegisterAccessNotice) obj;
                // Uses the same highlighting technique as for Text Segment -- see
                // AddressCellRenderer class in DataSegmentWindow.java.
            this.highlighting = true;
            addRegisterHighlighting((Register) observable, access.getAccessType());
            Globals.getGui().getRegistersPane().setSelectedComponent(this);
        }
    }

    private void updateRowHeight() {
        Font possibleFonts[] = {
            settings.getFontByPosition(Settings.EXPLICIT_WRITE_HIGHLIGHT_FONT),
                settings.getFontByPosition(Settings.EXPLICIT_READ_HIGHLIGHT_FONT),
            settings.getFontByPosition(Settings.EVEN_ROW_FONT),
            settings.getFontByPosition(Settings.ODD_ROW_FONT),
        };
        int maxHeight = 0;
        for (int i = 0; i < possibleFonts.length; i++) {
            int height = getFontMetrics(possibleFonts[i]).getHeight();
            if (height > maxHeight) {
                maxHeight = height;
            }
        }
        table.setRowHeight(maxHeight);
    }


    /*
    * Cell renderer for displaying register entries.  This does highlighting, so if you
    * don't want highlighting for a given column, don't use this.  Currently we highlight
    * all columns.
    */
    private class RegisterCellRenderer extends MonoRightCellRenderer {
        private Font font;
        private int alignment;

        private RegisterCellRenderer(Font font, int alignment) {
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
            if (highlighting && stepAccessNotices.contains(row) && settings.getBooleanSetting(Settings.Bool.EXPLICIT_WRITE_HIGHLIGHTING) && stepAccessNotices.getAccessNoticeType(row) == AccessNotice.WRITE) {
                    cell.setBackground(settings.getColorSettingByPosition(Settings.EXPLICIT_WRITE_HIGHLIGHT_BACKGROUND));
                    cell.setForeground(settings.getColorSettingByPosition(Settings.EXPLICIT_WRITE_HIGHLIGHT_FOREGROUND));
                    cell.setFont(settings.getFontByPosition(Settings.EXPLICIT_WRITE_HIGHLIGHT_FONT));
            } else if (highlighting && stepAccessNotices.contains(row) && settings.getBooleanSetting(Settings.Bool.EXPLICIT_READ_HIGHLIGHTING) && stepAccessNotices.getAccessNoticeType(row) == AccessNotice.READ) {
                    cell.setBackground(settings.getColorSettingByPosition(Settings.EXPLICIT_READ_HIGHLIGHT_BACKGROUND));
                    cell.setForeground(settings.getColorSettingByPosition(Settings.EXPLICIT_READ_HIGHLIGHT_FOREGROUND));
                    cell.setFont(settings.getFontByPosition(Settings.EXPLICIT_READ_HIGHLIGHT_FONT));
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

    private class RegTableModel extends AbstractTableModel {
        final String[] columnNames = {"Name", "Number", "Value"};
        private Object[][] data;

        private RegTableModel(Object[][] d) {
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
            return col == VALUE_COLUMN;
        }


        /*
         * Update cell contents in table model.  This method should be called
      	 * only when user edits cell, so input validation has to be done.  If
       	 * value is valid, the register is updated.
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
            //  Assures that if changed during program execution, the update will
            //  occur only between instructions.
            Globals.memoryAndRegistersLock.lock();
            try {
                registers[row].setValue(val);
            } finally {
                Globals.memoryAndRegistersLock.unlock();
            }
            int valueBase = Globals.getGui().getMainPane().getExecutePane().getValueDisplayBase();
            data[row][col] = NumberDisplayBaseChooser.formatNumber(val, valueBase);
            fireTableCellUpdated(row, col);
        }


        /**
         * Update cell contents in table model.
         */
        private void setDisplayAndModelValueAt(Object value, int row, int col) {
            data[row][col] = value;
            fireTableCellUpdated(row, col);
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
        private MyTippedJTable(RegTableModel m, String[] row, String[] col) {
            super(m);
            regToolTips = row;
            columnToolTips = col;
            this.setRowSelectionAllowed(true); // highlights background color of entire row
            this.setSelectionBackground(Color.GREEN);
        }

        private String[] regToolTips;

        //Implement table cell tool tips.
        public String getToolTipText(MouseEvent e) {
            java.awt.Point p = e.getPoint();
            int rowIndex = rowAtPoint(p);
            int colIndex = columnAtPoint(p);
            int realColumnIndex = convertColumnIndexToModel(colIndex);
            if (realColumnIndex == NAME_COLUMN) { //Register name column
                return regToolTips[rowIndex];
            } else {
                //You can omit this part if you know you don't have any
                //renderers that supply their own tool tips.
                return super.getToolTipText(e);
            }
        }

        private String[] columnToolTips;

        //Implement table header tool tips.
        protected JTableHeader createDefaultTableHeader() {
            return new JTableHeader(columnModel) {
                        public String getToolTipText(MouseEvent e) {
                            java.awt.Point p = e.getPoint();
                            int index = columnModel.getColumnIndexAtX(p.x);
                            int realIndex = columnModel.getColumn(index).getModelIndex();
                            return columnToolTips[realIndex];
                        }
                    };
        }
    }

    //////////////////////////////////////////////////////////////
    //
    // Contains the access notice linked to each register.
    // Each register is represented by the row in which it appears in the register window
    //
    private class RegistersAccessNotice {
        private int[] accessNoticeTypes;

        //Constructor which sets all access notices to none
        public RegistersAccessNotice() {
            accessNoticeTypes = new int[32];
            Arrays.fill(accessNoticeTypes, -1);
        }

        //Adds an access notice linked to a register. Overwrites with a "write" type if there is a reading
        //and a writing access notice for the same register, and that both highlighting are enabled
        public void add(int row, int type) {
            if (0 <= row && row < accessNoticeTypes.length)
                if (accessNoticeTypes[row] == -1 ||
                        (accessNoticeTypes[row] == AccessNotice.READ && settings.getBooleanSetting(Settings.Bool.EXPLICIT_WRITE_HIGHLIGHTING)))
                    accessNoticeTypes[row] = type;
        }

        //Clears all register access notice
        public void clear() {
            Arrays.fill(accessNoticeTypes, -1);
        }

        //Returns the access notice type of a register
        public int getAccessNoticeType(int row) {
            if (0 <= row && row < accessNoticeTypes.length)
                return accessNoticeTypes[row];
            else
                return -1;
        }

        // Returns true iff there is an access notice for a certain register
        public boolean contains(int row) {
            if (0 <= row && row < accessNoticeTypes.length) {
                return accessNoticeTypes[row] != -1;
            } else
                return false;
        }
    }
}
