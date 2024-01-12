package com.github.unaimillan.rars.riscv.instructions;

import com.github.unaimillan.jsoftfloat.Environment;
import com.github.unaimillan.jsoftfloat.types.Float64;

public class FMULD extends Double {
    public FMULD() {
        super("fmul.d", "Floating MULtiply (64 bit): assigns f1 to f2 * f3", "0001001");
    }

    @Override
    public Float64 compute(Float64 f1, Float64 f2, Environment e) {
        return com.github.unaimillan.jsoftfloat.operations.Arithmetic.multiplication(f1,f2,e);
    }
}
