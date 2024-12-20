package rars.riscv.instructions;

public class SLTIU extends ImmediateInstruction {
    public SLTIU() {
        super("sltiu t1,t2,-100", "Set less than immediate unsigned : If t2 is less than  sign-extended 16-bit immediate using unsigned comparison, then set t1 to 1 else set t1 to 0",
                "011");
    }
    public long compute(long value, long immediate) {
        return (Long.compareUnsigned(value, immediate) < 0) ? 1 : 0;
    }
}
