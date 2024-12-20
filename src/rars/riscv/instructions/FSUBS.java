package rars.riscv.instructions;

import jsoftfloat.Environment;
import jsoftfloat.types.Float32;

public class FSUBS extends Floating {
    public FSUBS() {
        super("fsub.s", "Floating SUBtract: assigns f1 to f2 - f3", "0000100");
    }

    @Override
    public Float32 compute(Float32 f1, Float32 f2, Environment e) {
        return jsoftfloat.operations.Arithmetic.subtraction(f1,f2,e);
    }
}
