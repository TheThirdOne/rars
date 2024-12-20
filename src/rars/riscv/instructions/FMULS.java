package rars.riscv.instructions;

import jsoftfloat.Environment;
import jsoftfloat.types.Float32;

public class FMULS extends Floating {
    public FMULS() {
        super("fmul.s", "Floating MULtiply: assigns f1 to f2 * f3", "0001000");
    }

    @Override
    public Float32 compute(Float32 f1, Float32 f2, Environment e) {
        return jsoftfloat.operations.Arithmetic.multiplication(f1,f2,e);
    }
}
