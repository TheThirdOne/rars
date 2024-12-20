package rars.riscv.hardware;

/**
 * Object provided to Observers of runtime access to register.
 * Observer can get the access type (R/W) and register number.
 *
 * @author Pete Sanderson
 * @version July 2005
 */

public class RegisterAccessNotice extends AccessNotice {
    private String registerName;

    /**
     * Constructor will be called only within this package, so assume
     * register number is in valid range.
     */
    RegisterAccessNotice(int type, String registerName) {
        super(type);
        this.registerName = registerName;
    }

    /**
     * Fetch the register number of register accessed.
     */
    public String getRegisterName() {
        return registerName;
    }

    /**
     * String representation indicates access type and which register
     */
    public String toString() {
        return ((this.getAccessType() == AccessNotice.READ) ? "R " : "W ") +
                "Reg " + registerName;
    }

}