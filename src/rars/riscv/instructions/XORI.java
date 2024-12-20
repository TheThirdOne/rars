package rars.riscv.instructions;

public class XORI extends ImmediateInstruction {
    public XORI() {
        super("xori t1,t2,-100", "Bitwise XOR immediate : Set t1 to bitwise XOR of t2 and sign-extended 12-bit immediate", "100");
    }

    public long compute(long value, long immediate) {
        return value ^ immediate;
    }
}
