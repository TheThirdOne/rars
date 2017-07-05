package mars;

import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.RegisterFile;
import mars.mips.instructions.Instruction;
import mars.simulator.Exceptions;
import mars.util.Binary;

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

/**
 * Class to represent error that occurs while assembling or running a MIPS program.
 * <p>
 * This is now only used in SimulationException; it should be reafctored into that quickly
 *
 * @author Pete Sanderson
 * @version August 2003
 **/

public class ProcessingException extends Exception {
    private ErrorList errs;

    /**
     * Constructor for ProcessingException.
     *
     * @param e An ErrorList which is an ArrayList of ErrorMessage objects.  Each ErrorMessage
     *          represents one processing error.
     **/
    public ProcessingException(ErrorList e) {
        errs = e;
    }

    /**
     * Constructor for ProcessingException.
     *
     * @param e   An ErrorList which is an ArrayList of ErrorMessage objects.  Each ErrorMessage
     *            represents one processing error.
     * @param aee AddressErrorException object containing specialized error message, cause, address
     **/
    public ProcessingException(ErrorList e, AddressErrorException aee) {
        errs = e;
        Exceptions.setRegisters(aee.getType(), aee.getAddress());
    }

    /**
     * Constructor for ProcessingException to handle runtime exceptions
     *
     * @param ps a ProgramStatement of statement causing runtime exception
     * @param m  a String containing specialized error message
     **/
    public ProcessingException(ProgramStatement ps, String m) {
        errs = new ErrorList();
        errs.add(new ErrorMessage(ps, "Runtime exception at " +
                Binary.intToHexString(RegisterFile.getProgramCounter() - Instruction.INSTRUCTION_LENGTH) +
                ": " + m));
        // Stopped using ps.getAddress() because of pseudo-instructions.  All instructions in
        // the macro expansion point to the same ProgramStatement, and thus all will return the
        // same value for getAddress(). But only the first such expanded instruction will
        // be stored at that address.  So now I use the program counter (which has already
        // been incremented).
    }


    /**
     * Constructor for ProcessingException to handle runtime exceptions
     *
     * @param ps    a ProgramStatement of statement causing runtime exception
     * @param m     a String containing specialized error message
     * @param cause exception cause (see Exceptions class for list)
     **/
    public ProcessingException(ProgramStatement ps, String m, int cause) {
        this(ps, m);
        Exceptions.setRegisters(cause);
    }


    /**
     * Constructor for ProcessingException to handle address runtime exceptions
     *
     * @param ps  a ProgramStatement of statement causing runtime exception
     * @param aee AddressErrorException object containing specialized error message, cause, address
     **/

    public ProcessingException(ProgramStatement ps, AddressErrorException aee) {
        this(ps, aee.getMessage());
        Exceptions.setRegisters(aee.getType(), aee.getAddress());
    }

    /**
     * Constructor for ProcessingException.
     * <p>
     * No parameter and thus no error list.  Use this for normal MIPS
     * program termination (e.g. syscall 10 for exit).
     **/
    public ProcessingException() {
        errs = null;
    }

    /**
     * Produce the list of error messages.
     *
     * @return Returns ErrorList of error messages.
     * @see ErrorList
     * @see ErrorMessage
     **/

    public ErrorList errors() {
        return errs;
    }

}
