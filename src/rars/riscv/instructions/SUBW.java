package rars.riscv.instructions;

// TODO: update description
public class SUBW extends ArithmeticW {
    public SUBW() {
        super("subw t1,t2,t3", "Addition: set t1 to (t2 plus t3)",
                "0100000", "000",new SUB());
    }
}
