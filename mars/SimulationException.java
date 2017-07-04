package mars;

import mars.mips.hardware.AddressErrorException;

/**
 * For exceptions thrown during runtime
 * <p>
 * if cause is -1, the exception is not-handlable is user code.
 */
public class SimulationException extends ProcessingException {
    private int cause = -1, value;

    public SimulationException() {
        super();
    }

    public SimulationException(ProgramStatement ps, String m, int cause) {
        super(ps, m, cause);
        this.cause = cause;
    }

    public SimulationException(ProgramStatement ps, String m) {
        super(ps, m);
    }

    public SimulationException(ProgramStatement ps, AddressErrorException aee) {
        super(ps, aee);
        cause = aee.getType();
        value = aee.getAddress();
    }

    public SimulationException(ErrorList el, AddressErrorException aee) {
        super(el, aee);
        cause = aee.getType();
        value = aee.getAddress();
    }
}
