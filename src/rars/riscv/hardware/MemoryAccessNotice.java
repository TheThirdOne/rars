package rars.riscv.hardware;

/**
 * Object provided to Observers of runtime access to memory.
 * Observer can get the access type (R/W), address and length in bytes (4,2,1).
 *
 * @author Pete Sanderson
 * @version July 2005
 */

public class MemoryAccessNotice extends AccessNotice {
    private int address;
    private int length;
    private int value;

    /**
     * Constructor will be called only within this package, so assume
     * address and length are in valid ranges.
     */
    MemoryAccessNotice(int type, int address, int length, int value) {
        super(type);
        this.address = address;
        this.length = length;
        this.value = value;
    }

    /**
     * Constructor will be called only within this package, so assume
     * address is in valid range.
     */
    public MemoryAccessNotice(int type, int address, int value) {
        super(type);
        this.address = address;
        this.length = Memory.WORD_LENGTH_BYTES;
        this.value = value;
    }

    /**
     * Fetch the memory address that was accessed.
     */
    public int getAddress() {
        return address;
    }

    /**
     * Fetch the length in bytes of the access operation (4,2,1).
     */
    public int getLength() {
        return length;
    }

    /**
     * Fetch the value of the access operation (the value read or written).
     */
    public int getValue() {
        return value;
    }

    /**
     * String representation indicates access type, address and length in bytes
     */
    public String toString() {
        return ((this.getAccessType() == AccessNotice.READ) ? "R " : "W ") +
                "Mem " + address + " " + length + "B = " + value;
    }
}