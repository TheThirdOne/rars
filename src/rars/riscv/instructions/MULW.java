package rars.riscv.instructions;

public class MULW extends ArithmeticW {
    public MULW() {
        super("mulw t1,t2,t3", "Multiplication: set t1 to the lower 32 bits of t2*t3 using only the lower 32 bits of the input",
                "0000001", "000",new MUL());
    }
}