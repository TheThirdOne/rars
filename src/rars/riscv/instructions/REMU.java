package rars.riscv.instructions;

import rars.riscv.hardware.ControlAndStatusRegisterFile;

public class REMU extends Arithmetic {
    public REMU() {
        super("remu t1,t2,t3", "Remainder: set t1 to the remainder of t2/t3 using unsigned division",
                "0000001", "111");
    }
    public long compute(long value, long value2) {
        if (value2 == 0) {
            return value;
        }
        return Long.remainderUnsigned(value, value2);
    }
    public int computeW(int value, int value2) {
        if (value2 == 0) {
            return value;
        }
        return Integer.remainderUnsigned(value, value2);
    }
}
