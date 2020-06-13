package rars.riscv.instructions;

// TODO: this is a stub from add
public class ADDIW extends ImmediateInstruction {
    public ADDIW() {
        super("addiw t1,t2,-100", "Addition immediate: set t1 to (t2 plus signed 12-bit immediate)", "000");
    }

    public long compute(long value, long immediate) {
        return value + immediate;
    }
}
