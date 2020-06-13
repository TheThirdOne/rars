package rars.riscv.instructions;

// TODO: update description
public class DIVW extends ArithmeticW {
    public DIVW() {
        super("divw t1,t2,t3", "Multiplication: set t1 to the lower 32 bits of t2*t3",
                "0000001", "100",new DIV());
    }
}