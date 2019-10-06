package rars.riscv.hardware;

/**
 * A register which aliases a subset of another register
 */
public class MaskedRegister extends Register {
    private int mask;

    /**
     * @param name the name to assign
     * @param num  the number to assign
     * @param val the reset value
     * @param mask the bits to use
     */
    public MaskedRegister(String name, int num, int val, int mask) {
        super(name, num, val); // reset value does not matter
        this.mask = mask;
    }

    public synchronized int setValue(int val) {
        int current = getValue();
        super.setValue((current & mask) | (val & ~mask));
        return current;
    }
}
