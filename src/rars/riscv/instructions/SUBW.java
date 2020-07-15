package rars.riscv.instructions;

public class SUBW extends ArithmeticW {
    public SUBW() {
        super("subw t1,t2,t3", "Subtraction: set t1 to (t2 minus t3) using only the lower 32 bits",
                "0100000", "000",new SUB());
    }
}
