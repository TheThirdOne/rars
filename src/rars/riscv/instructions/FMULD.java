package rars.riscv.instructions;

import jsoftfloat.Environment;
import jsoftfloat.types.Float64;

public class FMULD extends Double {
    public FMULD() {
        super("fmul.d", "Floating MULtiply (64 bit): assigns f1 to f2 * f3", "0001001");
    }

    @Override
    public Float64 compute(Float64 f1, Float64 f2, Environment e) {
        return jsoftfloat.operations.Arithmetic.multiplication(f1,f2,e);
    }
}
