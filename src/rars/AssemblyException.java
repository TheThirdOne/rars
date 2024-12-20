package rars;

/**
 * Exception wrapping ErrorList, used mainly in Tokenizer and Assembler; Represents errors that occurs while assembling a RISCV program.
 *
 * @author Benjamin Landers
 * @version July 2017
 */
public class AssemblyException extends Exception {
    private ErrorList errs;

    public AssemblyException(ErrorList errs) {
        this.errs = errs;
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
