package rars.assembler;

import java.util.ArrayList;
import java.util.Iterator;

/*
Copyright (c) 2003-2013,  Pete Sanderson and Kenneth Vollmar

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
 * Represents the list of tokens in a single line of code.  It uses, but is not
 * a subclass of, ArrayList.
 *
 * @author Pete Sanderson
 * @version August 2003
 */

public class TokenList implements Cloneable, Iterable<Token> {

    private ArrayList<Token> tokenList;
    private String processedLine;// DPS 03-Jan-2013

    /**
     * Constructor for objects of class TokenList
     */
    public TokenList() {
        tokenList = new ArrayList<>();
        processedLine = ""; // DPS 03-Jan-2013
    }

    public Iterator<Token> iterator() {
        return tokenList.iterator();
    }

    /**
     * Use this to record the source line String for this token list
     * after possible modification (textual substitution) during
     * assembly preprocessing.  The modified source will be displayed in
     * the Text Segment Display.
     *
     * @param line The source line, possibly modified (possibly not)
     */
    // DPS 03-Jan-2013
    public void setProcessedLine(String line) {
        processedLine = line;
    }

    /**
     * Retrieve the source line String associated with this
     * token list.  It may or may not have been modified during
     * assembly preprocessing.
     *
     * @return The source line for this token list.
     */   // DPS 03-Jan-2013/
    public String getProcessedLine() {
        return processedLine;
    }

    /**
     * Returns requested token given position number (starting at 0).
     *
     * @param pos Position in token list.
     * @return the requested token, or ArrayIndexOutOfBounds exception
     */
    public Token get(int pos) {
        return tokenList.get(pos);
    }

    /**
     * Replaces token at position with different one.  Will throw
     * ArrayIndexOutOfBounds exception if position does not exist.
     *
     * @param pos         Position in token list.
     * @param replacement Replacement token
     */
    public void set(int pos, Token replacement) {
        tokenList.set(pos, replacement);
    }

    /**
     * Returns number of tokens in list.
     *
     * @return token count.
     */
    public int size() {
        return tokenList.size();
    }

    /**
     * Adds a Token object to the end of the list.
     *
     * @param token Token object to be added.
     */
    public void add(Token token) {
        tokenList.add(token);
    }

    /**
     * Removes Token object at specified list position. Uses ArrayList remove method.
     *
     * @param pos Position in token list.  Subsequent Tokens are shifted one position left.
     * @throws IndexOutOfBoundsException if <tt>pos</tt> is < 0 or >= <tt>size()</tt>
     */
    public void remove(int pos) {
        tokenList.remove(pos);
    }

    /**
     * Returns empty/non-empty status of list.
     *
     * @return <tt>true</tt> if list has no tokens, else <tt>false</tt>.
     */
    public boolean isEmpty() {
        return tokenList.isEmpty();
    }

    /**
     * Get a String representing the token list.
     *
     * @return String version of the token list
     * (a blank is inserted after each token).
     */

    public String toString() {
        String stringified = "";
        for (Token token : tokenList) {
            stringified += token.toString() + " ";
        }
        return stringified;
    }


    /**
     * Get a String representing the sequence of token types for this list.
     *
     * @return String version of the token types for this list
     * (a blank is inserted after each token type).
     */

    public String toTypeString() {
        String stringified = "";
        for (Token t : tokenList) {
            stringified += t.getType().toString() + " ";
        }
        return stringified;
    }

    /**
     * Makes clone (shallow copy) of this token list object.
     *
     * @return the cloned list.
     */
    // Clones are a bit tricky.  super.clone() handles primitives (e.g. values) correctly
    // but the ArrayList itself has to be cloned separately -- otherwise clone will have
    // alias to original token list!!
    public Object clone() {
        try {
            TokenList t = (TokenList) super.clone();
            t.tokenList = new ArrayList<>(tokenList);
            return t;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
