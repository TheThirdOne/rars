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

package mars.tools;//.bhtsim;

import javax.swing.table.AbstractTableModel;
import java.util.Vector;

/**
 * Simulates the actual functionality of a Branch History Table (BHT).
 * <p>
 * The BHT consists of a number of BHT entries which are used to perform branch prediction.
 * The entries of the BHT are stored as a Vector of BHTEntry objects.
 * The number of entries is configurable but has to be a power of 2.
 * The history kept by each BHT entry is also configurable during run-time.
 * A change of the configuration however causes a complete reset of the BHT.
 * <p>
 * The typical interaction is as follows:
 * <ul>
 * <li>Construction of a BHT with a certain number of entries with a given history size.</li>
 * <li>When encountering a branch instruction the index of the relevant BHT entry is calculated via the {@link BHTableModel#getIdxForAddress(int)} method.</li>
 * <li>The current prediction of the BHT entry at the calculated index is obtained via the {@link BHTableModel#getPredictionAtIdx(int)} method.</li>
 * <li>After detecting if the branch was really taken or not, this feedback is provided to the BHT by the {@link BHTableModel#updatePredictionAtIdx(int, boolean)} method.</li>
 * </ul>
 * <p>
 * Additionally it serves as TableModel that can be directly used to render the state of the BHT in a JTable.
 * Feedback provided to the BHT causes a change of the internal state and a repaint of the table(s) associated to this model.
 *
 * @author ingo.kofler@itec.uni-klu.ac.at
 */

//@SuppressWarnings("serial")
public class BHTableModel extends AbstractTableModel {

    /**
     * vector holding the entries of the BHT
     */
    private Vector m_entries;

    /**
     * number of entries in the BHT
     */
    private int m_entryCnt;

    /**
     * number of past branch events to remember
     */
    private int m_historySize;

    /**
     * name of the table columns
     */
    private String m_columnNames[] = {"Index", "History", "Prediction", "Correct", "Incorrect", "Precision"};

    /**
     * type of the table columns
     */
    //@SuppressWarnings("unchecked")
    private Class m_columnClasses[] = {Integer.class, String.class, String.class, Integer.class, Integer.class, Double.class};


    /**
     * Constructs a new BHT with given number of entries and history size.
     *
     * @param numEntries  number of entries in the BHT
     * @param historySize size of the history (in bits/number of past branches)
     */
    public BHTableModel(int numEntries, int historySize, boolean initVal) {
        initBHT(numEntries, historySize, initVal);
    }


    /**
     * Returns the name of the i-th column of the table.
     * Required by the TableModel interface.
     *
     * @param i the index of the column
     * @return name of the i-th column
     */
    public String getColumnName(int i) {
        if (i < 0 || i > m_columnNames.length)
            throw new IllegalArgumentException("Illegal column index " + i + " (must be in range 0.." + (m_columnNames.length - 1) + ")");

        return m_columnNames[i];
    }


    /**
     * Returns the class/type of the i-th column of the table.
     * Required by the TableModel interface.
     *
     * @param i the index of the column
     * @return class representing the type of the i-th column
     */
    public Class getColumnClass(int i) {
        if (i < 0 || i > m_columnClasses.length)
            throw new IllegalArgumentException("Illegal column index " + i + " (must be in range 0.." + (m_columnClasses.length - 1) + ")");

        return m_columnClasses[i];
    }


    /**
     * Returns the number of columns.
     * Required by the TableModel interface.
     *
     * @return currently the constant 6
     */
    public int getColumnCount() {
        return 6;
    }


    /**
     * Returns the number of entries of the BHT.
     * Required by the TableModel interface.
     *
     * @return number of rows / entries of the BHT
     */
    public int getRowCount() {
        return m_entryCnt;
    }


    /**
     * Returns the value of the cell at the given row and column
     * Required by the TableModel interface.
     *
     * @param row the row index
     * @param col the column index
     * @return the value of the cell
     */
    public Object getValueAt(int row, int col) {

        BHTEntry e = (BHTEntry) m_entries.elementAt(row);
        if (e == null) return "";

        if (col == 0) return new Integer(row);
        if (col == 1) return e.getHistoryAsStr();
        if (col == 2) return e.getPredictionAsStr();
        if (col == 3) return new Integer(e.getStatsPredCorrect());
        if (col == 4) return new Integer(e.getStatsPredIncorrect());
        if (col == 5) return new Double(e.getStatsPredPrecision());

        return "";
    }


    /**
     * Initializes the BHT with the given size and history.
     * All previous data like the BHT entries' history and statistics will get lost.
     * A refresh of the table that use this BHT as model will be triggered.
     *
     * @param numEntries  number of entries in the BHT (has to be a power of 2)
     * @param historySize size of the history to consider
     * @param initVal     initial value for each entry (true means take branch, false do not take branch)
     */
    public void initBHT(int numEntries, int historySize, boolean initVal) {

        if (numEntries <= 0 || (numEntries & (numEntries - 1)) != 0)
            throw new IllegalArgumentException("Number of entries must be a positive power of 2.");
        if (historySize < 1 || historySize > 2)
            throw new IllegalArgumentException("Only history sizes of 1 or 2 supported.");

        m_entryCnt = numEntries;
        m_historySize = historySize;

        m_entries = new Vector();

        for (int i = 0; i < m_entryCnt; i++) {
            m_entries.add(new BHTEntry(m_historySize, initVal));
        }

        // refresh the table(s)
        fireTableStructureChanged();
    }


    /**
     * Returns the index into the BHT for a given branch instruction address.
     * A simple direct mapping is used.
     *
     * @param address the address of the branch instruction
     * @return the index into the BHT
     */
    public int getIdxForAddress(int address) {
        if (address < 0)
            throw new IllegalArgumentException("No negative addresses supported");

        return (address >> 2) % m_entryCnt;
    }


    /**
     * Retrieve the prediction for the i-th BHT entry.
     *
     * @param index the index of the entry in the BHT
     * @return the prediction to take (true) or do not take (false) the branch
     */
    public boolean getPredictionAtIdx(int index) {
        if (index < 0 || index > m_entryCnt)
            throw new IllegalArgumentException("Only indexes in the range 0 to " + (m_entryCnt - 1) + " allowed");

        return ((BHTEntry) m_entries.elementAt(index)).getPrediction();
    }


    /**
     * Updates the BHT entry with the outcome of the branch instruction.
     * This causes a change in the model and signals to update the connected table(s).
     *
     * @param index       the index of the entry in the BHT
     * @param branchTaken
     */
    public void updatePredictionAtIdx(int index, boolean branchTaken) {
        if (index < 0 || index > m_entryCnt)
            throw new IllegalArgumentException("Only indexes in the range 0 to " + (m_entryCnt - 1) + " allowed");

        ((BHTEntry) m_entries.elementAt(index)).updatePrediction(branchTaken);
        fireTableRowsUpdated(index, index);
    }

}
