package rars.riscv.hardware;

/**
 * A register which aliases a subset of another register
 */
public class LinkedRegister extends Register {
    private Register base;
    private long mask;
    private int shift;

    /**
     * @param name the name to assign
     * @param num  the number to assign
     * @param base the register to alias
     * @param mask the bits to use
     */
    public LinkedRegister(String name, int num, Register base, long mask) {
        super(name, num, 0); // reset value does not matter
        this.base = base;
        this.mask = mask;

        // Find the lowest 1 bit
        shift = 0;
        while (mask != 0 && (mask & 1) == 0) {
            shift++;
            mask >>>= 1;
        }
    }

    public synchronized long getValue() {
        super.getValue(); // to notify observers
        return getValueNoNotify();
    }

    public synchronized long getValueNoNotify() {
        return (base.getValueNoNotify() & mask) >>> shift;
    }

    public synchronized long setValue(long val) {
        long old = base.getValueNoNotify();
        base.setValue(((val << shift) & mask) | (old & ~mask));
        super.setValue(0); //value doesn't matter just notify
        return (old & mask) >>> shift;
    }

    public synchronized void resetValue() {
        base.resetValue(); // not completely correct, but registers are only reset all together, so it doesn't matter that the other subsets are reset too
    }
}
