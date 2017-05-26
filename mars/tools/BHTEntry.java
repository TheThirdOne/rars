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

/**
 * Represents a single entry of the Branch History Table.
 * <p>
 * The entry holds the information about former branch predictions and outcomes.
 * The number of past branch outcomes can be configured and is called the history.
 * The semantics of the history of size <i>n</i> is as follows.
 * The entry will change its prediction, if it mispredicts the branch <i>n</i> times in series.
 * The prediction of the entry can be obtained by the {@link BHTEntry#getPrediction()} method.
 * Feedback of taken or not taken branches is provided to the entry via the {@link BHTEntry#updatePrediction(boolean)} method.
 * This causes the history and the prediction to be updated.
 * <p>
 * Additionally the entry keeps track about how many times the prediction was correct or incorrect.
 * The statistics can be obtained by the methods {@link BHTEntry#getStatsPredCorrect()}, {@link BHTEntry#getStatsPredIncorrect()} and {@link BHTEntry#getStatsPredPrecision()}.
 *
 * @author ingo.kofler@itec.uni-klu.ac.at
 */

public class BHTEntry {

    /**
     * the history of the BHT entry. Each boolean value signals if the branch was taken or not. The value at index n-1 represents the most recent branch outcome.
     */
    private boolean m_history[];

    /**
     * the current prediction
     */
    private boolean m_prediction;

    /**
     * absolute number of incorrect predictions
     */
    private int m_incorrect;

    /**
     * absolute number of correct predictions
     */
    private int m_correct;


    /**
     * Constructs a BHT entry with a given history size.
     * <p>
     * The size of the history can only be set via the constructor and cannot be changed afterwards.
     *
     * @param historySize number of past branch outcomes to remember
     * @param initVal     the initial value of the entry (take or do not take)
     */
    public BHTEntry(int historySize, boolean initVal) {
        m_prediction = initVal;
        m_history = new boolean[historySize];

        for (int i = 0; i < historySize; i++) {
            m_history[i] = initVal;
        }
        m_correct = m_incorrect = 0;
    }


    /**
     * Returns the branch prediction based on the history.
     *
     * @return true if prediction is to take the branch, false otherwise
     */
    public boolean getPrediction() {
        return m_prediction;
    }


    /**
     * Updates the entry's history and prediction.
     * This method provides feedback for a prediction.
     * The history and the statistics are updated accordingly.
     * Based on the updated history a new prediction is calculated
     *
     * @param branchTaken signals if the branch was taken (true) or not (false)
     */
    public void updatePrediction(boolean branchTaken) {

        // update history
        for (int i = 0; i < m_history.length - 1; i++) {
            m_history[i] = m_history[i + 1];
        }
        m_history[m_history.length - 1] = branchTaken;


        // if the prediction was correct, update stats and keep prediction
        if (branchTaken == m_prediction) {
            m_correct++;
        } else {
            m_incorrect++;

            // check if the prediction should change
            boolean changePrediction = true;

            for (int i = 0; i < m_history.length; i++) {
                if (m_history[i] != branchTaken)
                    changePrediction = false;
            }

            if (changePrediction)
                m_prediction = !m_prediction;

        }
    }


    /**
     * Get the absolute number of mispredictions.
     *
     * @return number of incorrect predictions (mispredictions)
     */
    public int getStatsPredIncorrect() {
        return m_incorrect;
    }


    /**
     * Get the absolute number of correct predictions.
     *
     * @return number of correct predictions
     */
    public int getStatsPredCorrect() {
        return m_correct;
    }


    /**
     * Get the percentage of correct predictions.
     *
     * @return the percentage of correct predictions
     */
    public double getStatsPredPrecision() {
        int sum = m_incorrect + m_correct;
        return (sum == 0) ? 0 : m_correct * 100.0 / sum;
    }


    /***
     * Builds a string representation of the BHT entry's history.
     * The history is a sequence of flags that signal if the branch was taken (T) or not taken (NT).
     *
     * @return a string representation of the BHT entry's history
     */
    public String getHistoryAsStr() {
        String result = "";

        for (int i = 0; i < m_history.length; i++) {
            if (i > 0) result = result + ", ";
            result += m_history[i] ? "T" : "NT";
        }
        return result;
    }


    /***
     * Returns a string representation of the BHT entry's current prediction.
     * The prediction can be either to TAKE or do NOT TAKE the branch.
     *
     * @return a string representation of the BHT entry's current prediction
     */
    public String getPredictionAsStr() {
        return m_prediction ? "TAKE" : "NOT TAKE";
    }
}
