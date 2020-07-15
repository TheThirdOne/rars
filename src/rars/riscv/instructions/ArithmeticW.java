package rars.riscv.instructions;


public abstract class ArithmeticW extends Arithmetic {
    private final Arithmetic base;
    public ArithmeticW(String usage, String description, String funct7, String funct3, Arithmetic base) {
        super(usage, description, funct7,funct3,true);
        this.base = base;
    }
    protected long compute(long value, long value2) {
        return base.computeW((int)value,(int)value2);
    }
}
