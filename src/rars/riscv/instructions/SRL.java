package rars.riscv.instructions;

public class SRL extends Arithmetic {
    public SRL() {
        super("srl t1,t2,t3", "Shift right logical: Set t1 to result of shifting t2 right by number of bits specified by value in low-order 5 bits of t3",
                "0000000", "101");
    }
    public long compute(long value, long value2) {
        return value >>> (value2 & 0x0000003F); // Use the bottom 6 bits
    }
    public int computeW(int value, int value2) {
        /// Use >>> to zero-fill
        return value >>> (value2 & 0x0000001F); // Only use the bottom 5 bits
    }
}
