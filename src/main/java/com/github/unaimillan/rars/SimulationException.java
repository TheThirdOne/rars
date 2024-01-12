package com.github.unaimillan.rars;

import com.github.unaimillan.rars.riscv.hardware.AddressErrorException;
import com.github.unaimillan.rars.riscv.hardware.RegisterFile;
import com.github.unaimillan.rars.riscv.Instruction;
import com.github.unaimillan.rars.util.Binary;

/**
 * For exceptions thrown during runtime
 * <p>
 * if cause is -1, the exception is not-handlable is user code.
 */
public class SimulationException extends Exception {

    // Interrupts
    public static final int SOFTWARE_INTERRUPT = 0x80000000;
    public static final int TIMER_INTERRUPT = 0x80000004;
    public static final int EXTERNAL_INTERRUPT = 0x80000008;
    // Traps
    public static final int INSTRUCTION_ADDR_MISALIGNED = 0;
    public static final int INSTRUCTION_ACCESS_FAULT = 1;
    public static final int ILLEGAL_INSTRUCTION = 2;
    public static final int LOAD_ADDRESS_MISALIGNED = 4;
    public static final int LOAD_ACCESS_FAULT = 5;
    public static final int STORE_ADDRESS_MISALIGNED = 6;
    public static final int STORE_ACCESS_FAULT = 7;
    public static final int ENVIRONMENT_CALL = 8;

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

    public SimulationException(String m) {
        message = new ErrorMessage(null, 0, 0, m);
    }

    public SimulationException(String m, int cause) {
        message = new ErrorMessage(null, 0, 0, m);
        this.cause = cause;
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
}
