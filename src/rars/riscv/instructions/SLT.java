package rars.riscv.instructions;

public class SLT extends Arithmetic {
    public SLT() {
        super("slt t1,t2,t3", "Set less than : If t2 is less than t3, then set t1 to 1 else set t1 to 0",
                "0000000", "010");
    }

    public long compute(long value, long value2) {
        return (value < value2) ? 1 : 0;
    }
}
