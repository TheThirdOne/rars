package rars.riscv.instructions;

// TODO: update description
public class ADDIW extends ImmediateInstruction {
    public ADDIW() {
        super("addiw t1,t2,-100", "Addition immediate: set t1 to (t2 plus signed 12-bit immediate)", "000",true);
    }

    public long compute(long value, long immediate) {
        return (int)value + (int)immediate;
    }
}
