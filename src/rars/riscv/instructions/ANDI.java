package rars.riscv.instructions;

public class ANDI extends ImmediateInstruction {
    public ANDI() {
        super("andi t1,t2,-100", "Bitwise AND immediate : Set t1 to bitwise AND of t2 and sign-extended 12-bit immediate", "111");
    }

    public long compute(long value, long immediate) {
        return value & immediate;
    }
}
