package com.github.unaimillan.rars.riscv.instructions;

import com.github.unaimillan.jsoftfloat.Environment;
import com.github.unaimillan.jsoftfloat.types.Float64;

public class FMAXD extends Double {
    public FMAXD() {
        super("fmax.d", "Floating MAXimum (64 bit): assigns f1 to the larger of f1 and f3", "0010101", "001");
    }

    public Float64 compute(Float64 f1, Float64 f2, Environment env) {
        return com.github.unaimillan.jsoftfloat.operations.Comparisons.maximumNumber(f1,f2,env);
    }
}
