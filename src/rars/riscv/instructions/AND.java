package rars.riscv.instructions;

public class AND extends Arithmetic {
    public AND() {
        super("and t1,t2,t3", "Bitwise AND : Set t1 to bitwise AND of t2 and t3",
                "0000000", "111");
    }

    public long compute(long value, long value2) {
        return value & value2;
    }
}
