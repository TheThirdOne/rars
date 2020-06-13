package rars.riscv.instructions;

// TODO: update description
public class REMUW extends ArithmeticW {
    public REMUW() {
        super("remuw t1,t2,t3", "Multiplication: set t1 to the lower 32 bits of t2*t3",
                "0000001", "111",new REMU());
    }
}