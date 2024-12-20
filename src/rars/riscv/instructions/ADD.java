package rars.riscv.instructions;

public class ADD extends Arithmetic {
    public ADD() {
        super("add t1,t2,t3", "Addition: set t1 to (t2 plus t3)",
                "0000000", "000");
    }

    public long compute(long value, long value2) {
        return value + value2;
    }
}
