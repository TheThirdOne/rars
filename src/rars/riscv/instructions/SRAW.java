package rars.riscv.instructions;

public class SRAW extends ArithmeticW {
    public SRAW() {
        super("sraw t1,t2,t3", "Shift left logical (32 bit): Set t1 to result of shifting t2 left by number of bits specified by value in low-order 5 bits of t3",
                "0100000", "101",new SRA());
    }
}
