package rars.riscv.instructions;

// TODO: this is a stub from add
public class ADDW extends Arithmetic {
    public ADDW() {
        super("addw t1,t2,t3", "Addition: set t1 to (t2 plus t3)",
                "0000000", "000");
    }

    public long compute(long value, long value2) {
        return value + value2;
    }
}
