package rars.riscv.instructions;

public class ADDW extends ArithmeticW {
    public ADDW() {
        super("addw t1,t2,t3", "Addition: set t1 to (t2 plus t3) using only the lower 32 bits",
                "0000000", "000",new ADD());
    }
}
