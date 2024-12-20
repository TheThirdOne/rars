package rars.riscv.instructions;

import java.math.BigInteger;

public class MULHU extends Arithmetic {
    public MULHU() {
        super("mulhu t1,t2,t3", "Multiplication: set t1 to the upper 32 bits of t2*t3 using unsigned multiplication",
                "0000001", "011");
    }
    public long compute(long value, long value2) {
        BigInteger unsigned = BigInteger.valueOf(value);
        if (value < 0) {
            unsigned = unsigned.add(BigInteger.ONE.shiftLeft(64));
        }
        BigInteger unsigned2 = BigInteger.valueOf(value2);
        if (value2 < 0) {
            unsigned2 = unsigned2.add(BigInteger.ONE.shiftLeft(64));
        }
        return unsigned.multiply(unsigned2).shiftRight(64).longValue();
    }
    public int computeW(int value, int value2) {
        // Don't sign extend both arguments
        long ext = ((long) value) & 0xFFFFFFFFL, ext2 = ((long) value2) & 0xFFFFFFFFL;
        // Return the top 32 bits of the mutliplication
        return (int) ((ext * ext2) >> 32);
    }
}
