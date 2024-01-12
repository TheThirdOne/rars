package com.github.unaimillan.rars.riscv.instructions;

import com.github.unaimillan.jsoftfloat.Environment;
import com.github.unaimillan.jsoftfloat.types.Float64;

public class FMIND extends Double {
    public FMIND() {
        super("fmin.d", "Floating MINimum (64 bit): assigns f1 to the smaller of f1 and f3", "0010101", "000");
    }

    public Float64 compute(Float64 f1, Float64 f2, Environment env) {
        return com.github.unaimillan.jsoftfloat.operations.Comparisons.minimumNumber(f1,f2,env);
    }
}
