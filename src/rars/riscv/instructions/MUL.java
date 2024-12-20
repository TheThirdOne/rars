package rars.riscv.instructions;

public class MUL extends Arithmetic {
    public MUL() {
        super("mul t1,t2,t3", "Multiplication: set t1 to the lower 32 bits of t2*t3",
                "0000001", "000");
    }

    public long compute(long value, long value2) {
        // int*int is correct here
        // the upper 64 bits are just ignored and the result is is just the lower 32 bits
        return value * value2;
    }
}
