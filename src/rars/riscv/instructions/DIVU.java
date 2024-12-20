package rars.riscv.instructions;

import rars.riscv.hardware.ControlAndStatusRegisterFile;

public class DIVU extends Arithmetic {
    public DIVU() {
        super("divu t1,t2,t3", "Division: set t1 to the result of t2/t3 using unsigned division",
                "0000001", "101");
    }
    public long compute(long value, long value2) {
        // Signal illegal division with -1
        if (value2 == 0) {
            return -1;
        }
        return Long.divideUnsigned(value, value2);
    }
    public int computeW(int value, int value2) {
        return (int)compute(value & 0xFFFFFFFFL, value2 & 0xFFFFFFFFL);
    }
}