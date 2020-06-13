package rars.riscv.instructions;

// TODO: update description
public class SLLW extends ArithmeticW {
    public SLLW() {
        super("sllw t1,t2,t3", "Shift left logical: Set t1 to result of shifting t2 left by number of bits specified by value in low-order 5 bits of t3",
                "0000000", "001",new SLL());
    }
}
