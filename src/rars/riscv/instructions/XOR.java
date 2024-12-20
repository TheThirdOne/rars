package rars.riscv.instructions;

public class XOR extends Arithmetic {
    public XOR() {
        super("xor t1,t2,t3", "Bitwise XOR : Set t1 to bitwise XOR of t2 and t3",
                "0000000", "100");
    }

    public long compute(long value, long value2) {
        return value ^ value2;
    }
}
