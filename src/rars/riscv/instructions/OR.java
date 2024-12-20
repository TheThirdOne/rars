package rars.riscv.instructions;

public class OR extends Arithmetic {
    public OR() {
        super("or t1,t2,t3", "Bitwise OR : Set t1 to bitwise OR of t2 and t3",
                "0000000", "110");
    }

    public long compute(long value, long value2) {
        return value | value2;
    }
}
