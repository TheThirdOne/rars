package rars.assembler;

import rars.RISCVprogram;

import java.util.ArrayList;

/*
Copyright (c) 2013.

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
 * Stores information of macros defined by now. <br>
 * Will be used in first pass of assembling RISCV source code. When reached
 * <code>.macro</code> directive, parser calls
 * {@link MacroPool#beginMacro(Token)} and skips source code lines until
 * reaches <code>.end_macro</code> directive. then calls
 * {@link MacroPool#commitMacro(Token)} and the macro information stored in a
 * {@link Macro} instance will be added to {@link #macroList}. <br>
 * Each {@link RISCVprogram} will have one {@link MacroPool}<br>
 * NOTE: Forward referencing macros (macro expansion before its definition in
 * source code) and Nested macro definition (defining a macro inside other macro
 * definition) are not supported.
 *
 * @author M.H.Sekhavat sekhavat17@gmail.com
 */
public class MacroPool {
    private RISCVprogram program;
    /**
     * List of macros defined by now
     */
    private ArrayList<Macro> macroList;
    /**
     * @see #beginMacro(Token)
     */
    private Macro current;
    private ArrayList<Integer> callStack;
    private ArrayList<Integer> callStackOrigLines;
    /**
     * @see #getNextCounter()
     */
    private int counter;


    /**
     * Create an empty MacroPool for given program
     *
     * @param program associated program
     */
    public MacroPool(RISCVprogram program) {
        this.program = program;
        macroList = new ArrayList<>();
        callStack = new ArrayList<>();
        callStackOrigLines = new ArrayList<>();
        current = null;
        counter = 0;
    }

    /**
     * This method will be called by parser when reached <code>.macro</code>
     * directive.<br>
     * Instantiates a new {@link Macro} object and stores it in {@link #current}
     * . {@link #current} will be added to {@link #macroList} by
     * {@link #commitMacro(Token)}
     *
     * @param nameToken Token containing name of macro after <code>.macro</code> directive
     */

    public void beginMacro(Token nameToken) {
        current = new Macro();
        current.setName(nameToken.getValue());
        current.setFromLine(nameToken.getSourceLine());
        current.setOriginalFromLine(nameToken.getOriginalSourceLine());
        current.setProgram(program);
    }

    /**
     * This method will be called by parser when reached <code>.end_macro</code>
     * directive. <br>
     * Adds/Replaces {@link #current} macro into the {@link #macroList}.
     *
     * @param endToken Token containing <code>.end_macro</code> directive in source code
     */

    public void commitMacro(Token endToken) {
        current.setToLine(endToken.getSourceLine());
        current.setOriginalToLine(endToken.getOriginalSourceLine());
        current.readyForCommit();
        macroList.add(current);
        current = null;
    }

    /**
     * Will be called by parser when reaches a macro expansion call
     *
     * @param tokens tokens passed to macro expansion call
     * @return {@link Macro} object matching the name and argument count of
     * tokens passed
     */
    public Macro getMatchingMacro(TokenList tokens, int callerLine) {
        if (tokens.size() < 1)
            return null;
        Macro ret = null;
        Token firstToken = tokens.get(0);
        for (Macro macro : macroList) {
            if (macro.getName().equals(firstToken.getValue())
                    && macro.getArgs().size() + 1 == tokens.size()
                    //&& macro.getToLine() < callerLine  // condition removed; doesn't work nicely in conjunction with .include, and does not seem necessary.  DPS 8-MAR-2013
                    && (ret == null || ret.getFromLine() < macro.getFromLine()))
                ret = macro;
        }
        return ret;
    }

    /**
     * @param value
     * @return true if any macros have been defined with name <code>value</code>
     * by now, not concerning arguments count.
     */
    public boolean matchesAnyMacroName(String value) {
        for (Macro macro : macroList)
            if (macro.getName().equals(value))
                return true;
        return false;
    }


    public Macro getCurrent() {
        return current;
    }

    public void setCurrent(Macro current) {
        this.current = current;
    }

    /**
     * {@link #counter} will be set to 0 on construction of this class and will
     * be incremented by each call. parser calls this method once for every
     * expansions. it will be a unique id for each expansion of macro in a file
     *
     * @return counter value
     */
    public int getNextCounter() {
        return counter++;
    }


    public ArrayList<Integer> getCallStack() {
        return callStack;
    }


    public boolean pushOnCallStack(Token token) { //returns true if detected expansion loop
        int sourceLine = token.getSourceLine();
        int origSourceLine = token.getOriginalSourceLine();
        if (callStack.contains(sourceLine))
            return true;
        callStack.add(sourceLine);
        callStackOrigLines.add(origSourceLine);
        return false;
    }

    public void popFromCallStack() {
        callStack.remove(callStack.size() - 1);
        callStackOrigLines.remove(callStackOrigLines.size() - 1);
    }


    public String getExpansionHistory() {
        String ret = "";
        for (int i = 0; i < callStackOrigLines.size(); i++) {
            if (i > 0)
                ret += "->";
            ret += callStackOrigLines.get(i).toString();
        }
        return ret;
    }
}
