package mars;

import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.RegisterFile;
import mars.mips.instructions.Instruction;
import mars.util.Binary;

/**
 * For exceptions thrown during runtime
 * <p>
 * if cause is -1, the exception is not-handlable is user code.
 */
public class SimulationException extends Exception {
    private int cause = -1, value = 0;
    private ErrorMessage message = null;

    public SimulationException() {
    }

    public SimulationException(ProgramStatement ps, String m, int cause) {
        this(ps, m);
        this.cause = cause;
    }

    /**
     * Constructor for ProcessingException to handle runtime exceptions
     *
     * @param ps a ProgramStatement of statement causing runtime exception
     * @param m  a String containing specialized error message
     **/
    public SimulationException(ProgramStatement ps, String m) {
        message = new ErrorMessage(ps, "Runtime exception at " +
                Binary.intToHexString(RegisterFile.getProgramCounter() - Instruction.INSTRUCTION_LENGTH) +
                ": " + m);
        // Stopped using ps.getAddress() because of pseudo-instructions.  All instructions in
        // the macro expansion point to the same ProgramStatement, and thus all will return the
        // same value for getAddress(). But only the first such expanded instruction will
        // be stored at that address.  So now I use the program counter (which has already
        // been incremented).
    }

    public SimulationException(ProgramStatement ps, AddressErrorException aee) {
        this(ps, aee.getMessage());
        cause = aee.getType();
        value = aee.getAddress();
    }

    public SimulationException(ErrorMessage el, AddressErrorException aee) {
        message = el;
        cause = aee.getType();
        value = aee.getAddress();
    }

    /**
     * Produce the list of error messages.
     *
     * @return Returns the Message associated with the exception
     * @see ErrorMessage
     **/
    public ErrorMessage error() {
        return message;
    }

    public int cause() {
        return cause;
    }

    public int value() {
        return value;
    }

    ;
}
