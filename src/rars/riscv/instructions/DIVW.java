package rars.riscv.instructions;

// TODO: this is a stub from mul
public class DIVW extends Arithmetic {
    public DIVW() {
        super("divw t1,t2,t3", "Multiplication: set t1 to the lower 32 bits of t2*t3",
                "0000001", "000");
    }

    public long compute(long value, long value2) {
        return value * value2;
    }
}