package rars.program;

import java.util.ArrayList;

import rars.AssemblyException;
import rars.ErrorList;
import rars.ProgramStatement;
import rars.assembler.SymbolTable;
import rars.simulator.BackStepper;

/*
Copyright (c) 2003-2006,  Pete Sanderson and Kenneth Vollmar

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

public abstract class RISCVprogram {
	
    private String filename;
    private ArrayList<ProgramStatement> machineList;
    private BackStepper backStepper;
    private SymbolTable localSymbolTable;
	private ArrayList<String> sourceList;
    
    /**
     * Produces name of associated source code file.
     *
     * @return File name as String.
     **/
    public String getFilename() {
        return filename;
    }
    
    /**
     * Sets the filename of this program
     * 
     * @param filename the new filename of this program
     */
    private void setFilename(String filename) {
    	this.filename = filename;
    }
    
    /**
     * Produces list of machine statements that are assembled from the program.
     *
     * @return ArrayList of ProgramStatement.  Each ProgramStatement represents an assembled
     * basic RISCV instruction.
     * @see ProgramStatement
     **/
    public ArrayList<ProgramStatement> getMachineList() {
        return machineList;
    }
    
    /**
     * Sets the machine code list of this program
     * 
     * @param machineList
     */
    protected void setMachineList(ArrayList<ProgramStatement> machineList) {
    	this.machineList = machineList;
    }
    
    /**
     * Returns BackStepper associated with this program.  It is created upon successful assembly.
     *
     * @return BackStepper object, null if there is none.
     **/
    public BackStepper getBackStepper() {
        return backStepper;
    }
	
    /**
     * Returns SymbolTable associated with this program.  It is created at assembly time,
     * and stores local labels (those not declared using .globl directive).
     **/
    public SymbolTable getLocalSymbolTable() {
        return localSymbolTable;
    }
    
    /**
     * Sets the local symbol table
     * 
     * @param localSymbolTable - the new local symbol table
     */
    protected void setLocalSymbolTable(SymbolTable localSymbolTable) {
    	this.localSymbolTable = localSymbolTable;
    }
    
    /**
     * Produces list of source statements that comprise the program.
     *
     * @return ArrayList of String.  Each String is one line of RISCV source code.
     **/

    public ArrayList<String> getSourceList() {
        return sourceList;
    }
    
    /**
     * Sets the source code list of this program
     * 
     * @param sourceList The source code list
     */
    protected void setSourceList(ArrayList<String> sourceList) {
    	this.sourceList = sourceList;
    }
    
    /**
     * Produces specified line of RISCV source program.
     *
     * @param i Line number of RISCV source program to get.  Line 1 is first line.
     * @return Returns specified line of RISCV source.  If outside the line range,
     * it returns null.  Line 1 is first line.
     **/

    public String getSourceLine(int i) {
        if ((i >= 1) && (i <= sourceList.size()))
            return sourceList.get(i - 1);
        else
            return null;
    }
    
    /**
     * Returns status of BackStepper associated with this program.
     *
     * @return true if enabled, false if disabled or non-existant.
     **/
    public boolean backSteppingEnabled() {
        return (backStepper != null && backStepper.enabled());
    }
    
    /**
     * Assembles the RISCV source program. All files comprising the program must have
     * already been tokenized.
     *
     * @param programsToAssemble       ArrayList of RISCVprogram objects, each representing a tokenized source file.
     * @param warningsAreErrors        A boolean value - true means assembler warnings will be considered errors and terminate
     *                                 the assemble; false means the assembler will produce warning message but otherwise ignore warnings.
     * @return ErrorList containing nothing or only warnings (otherwise would have thrown exception).
     * @throws AssemblyException Will throw exception if errors occured while assembling.
     **/
    public final ErrorList assemble(ArrayList<? extends RISCVprogram> programsToAssemble, boolean warningsAreErrors) 
    		throws AssemblyException {
        this.backStepper = null;
        ErrorList result = assembleHelper(programsToAssemble, warningsAreErrors);
        this.backStepper = new BackStepper();
        return result;
    }
    
    /**
     * Reads RISCV source code from file into structure.  Will always read from file.
     * It is GUI responsibility to assure that source edits are written to file
     * when user selects compile or run/step options.
     *
     * @throws AssemblyException Will throw exception if there is any problem reading the file.
     **/
    public final void readSource(String filename) throws AssemblyException {
    	this.setFilename(filename);
    	this.sourceList = readSourceHelper();
    }
    
    /**
     * Overridden by subclasses to implement assembly functionality. This method is
     * automatically called by the assemble() method and should not be called directly.
     * 
     * Assembles the RISCV source program. All files comprising the program must have
     * already been tokenized.
     *
     * @param programsToAssemble       ArrayList of RISCVprogram objects, each representing a tokenized source file.
     * @param warningsAreErrors        A boolean value - true means assembler warnings will be considered errors and terminate
     *                                 the assemble; false means the assembler will produce warning message but otherwise ignore warnings.
     * @return ErrorList containing nothing or only warnings (otherwise would have thrown exception).
     * @throws AssemblyException Will throw exception if errors occured while assembling.
     **/
    protected abstract ErrorList assembleHelper(ArrayList<? extends RISCVprogram> programsToAssemble, 
    		boolean warningsAreErrors) throws AssemblyException;
    
    /**
     * Overriden by subclasses to implement file reading functionality. This method is
     * automatically called by the readSource() method and should not be called directly.
     * 
     * Reads RISCV source code from file into structure.  Will always read from file.
     * It is GUI responsibility to assure that source edits are written to file
     * when user selects compile or run/step options.
     *
     * @throws AssemblyException Will throw exception if there is any problem reading the file.
     **/
    public abstract ArrayList<String> readSourceHelper() throws AssemblyException;
}
