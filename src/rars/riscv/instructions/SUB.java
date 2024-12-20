package rars.riscv.instructions;

public class SUB extends Arithmetic {
    public SUB() {
        super("sub t1,t2,t3", "Subtraction: set t1 to (t2 minus t3)",
                "0100000", "000");
    }

    public long compute(long value, long value2) {
        return value - value2;
    }
}
