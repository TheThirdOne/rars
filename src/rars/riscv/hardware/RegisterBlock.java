package rars.riscv.hardware;

import rars.util.Binary;

import java.util.Observer;

/**
 * Helper class for RegisterFile, FPRegisterFile, and CSRFile
 * <p>
 * Much of the implementation was ripped directly from RegisterFile
 *
 * @author Benjamin Landers
 * @version June 2017
 */
public class RegisterBlock {
    private final Register[] regFile;
    private final char prefix;

    protected RegisterBlock(char prefix, Register[] registers) {
        this.prefix = prefix;
        this.regFile = registers;
    }

    /**
     * Method for displaying the register values for debugging.
     **/
    public void showRegisters() {
        for (Register r : regFile) {
            System.out.println("Name: " + r.getName());
            System.out.println("Number: " + r.getNumber());
            System.out.println("Value: " + r.getValue());
            System.out.println("");
        }
    }

    /**
     * This method updates the register value to val
     *
     * @param r   Register to set the value of.
     * @param val The desired value for the register.
     **/
    public long updateRegister(Register r, long val) {
        if (r == null) return 0;
        return r.setValue(val);
    }

    public long updateRegister(int num, long val) {
        return updateRegister(getRegister(num), val);
    }

    public long updateRegister(String name, long val) {
        return updateRegister(getRegister(name), val);
    }

    /**
     * Returns the value of the register.
     *
     * @param num The register's number.
     * @return The value of the given register.
     **/
    public long getValue(int num) {
        return getRegister(num).getValue();
    }

    /**
     * Returns the value of the register. Observers are not notified.
     *
     * @param num The register's number.
     * @return value The value of the given register.
     */
    public long getValueNoNotify(int num) {
        return getRegister(num).getValueNoNotify();
    }

    /**
     * Returns the value of the register.
     *
     * @param name The register's name.
     * @return The value of the given register.
     **/
    public long getValue(String name) {
        return getRegister(name).getValue();
    }

    /**
     * Get a register from a number
     *
     * @param num the number to search for
     * @return the register for num or null if none exists
     */
    public Register getRegister(int num) {
        for (Register r : regFile) {
            if (r.getNumber() == num) {
                return r;
            }
        }
        return null;
    }

    /**
     * Get register object corresponding to given name.  If no match, return null.
     *
     * @param name The register name, either in (prefix)(number) format or a direct name.
     * @return The register object,or null if not found.
     **/
    public Register getRegister(String name) {
        if(name.length() < 2) return null;

        // Handle a direct name
        for (Register r : regFile) {
            if (r.getName().equals(name)) {
                return r;
            }
        }
        // Handle prefix case
        if (name.charAt(0) == prefix) {
            if(name.charAt(1) == 0) { // Ensure that it is a normal decimal number
                if(name.length() > 2) return null;
                return getRegister(0);
            }

            Integer num = Binary.stringToIntFast(name.substring(1));
            if(num == null) return null;
            return getRegister(num);
        }
        return null;
    }

    /**
     * For returning the set of registers.
     *
     * @return The set of registers.
     **/

    public Register[] getRegisters() {
        return regFile;
    }

    /**
     * Method to reinitialize the values of the registers.
     **/
    public void resetRegisters() {
        for (Register r : regFile) {
            r.resetValue();
        }
    }

    /**
     * Each individual register is a separate object and Observable.  This handy method
     * will add the given Observer to each one.  Currently does not apply to Program
     * Counter.
     */
    public void addRegistersObserver(Observer observer) {
        for (Register r : regFile) {
            r.addObserver(observer);
        }
    }

    /**
     * Each individual register is a separate object and Observable.  This handy method
     * will delete the given Observer from each one.  Currently does not apply to Program
     * Counter.
     */
    public void deleteRegistersObserver(Observer observer) {
        for (Register r : regFile) {
            r.deleteObserver(observer);
        }
    }
}
