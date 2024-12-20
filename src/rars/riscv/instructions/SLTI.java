package rars.riscv.instructions;

public class SLTI extends ImmediateInstruction {
    public SLTI() {
        super("slti t1,t2,-100", "Set less than immediate : If t2 is less than sign-extended 12-bit immediate, then set t1 to 1 else set t1 to 0",
                "010");
    }

    public long compute(long value, long immediate) {
        return (value < immediate) ? 1 : 0;
    }
}
