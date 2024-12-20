package rars.riscv.instructions;

public class ORI extends ImmediateInstruction {
    public ORI() {
        super("ori t1,t2,-100", "Bitwise OR immediate : Set t1 to bitwise OR of t2 and sign-extended 12-bit immediate", "110");
    }

    public long compute(long value, long immediate) {
        return value | immediate;
    }
}
