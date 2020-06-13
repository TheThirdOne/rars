package rars.riscv.instructions;

// TODO: update description
public class DIVUW extends ArithmeticW {
    public DIVUW() {
        super("divuw t1,t2,t3", "Multiplication: set t1 to the lower 32 bits of t2*t3",
                "0000001", "101",new DIVU());
    }

}