package rars.riscv.instructions;

// TODO: this is a stub from sll
public class SRAW extends Arithmetic {
    public SRAW() {
        super("sraw t1,t2,t3", "Shift left logical: Set t1 to result of shifting t2 left by number of bits specified by value in low-order 5 bits of t3",
                "0000000", "001");
    }
    public long compute(long value, long value2) {
        return value << (value2 & 0x0000003F); // Use the bottom 6 bits
    }

    public int computeW(int value, int value2) {
        return value << (value2 & 0x0000001F); // Only use the bottom 5 bits
    }
}
