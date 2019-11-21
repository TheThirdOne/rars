package rars.riscv.instructions;

import jsoftfloat.Environment;
import jsoftfloat.types.Float64;

public class FMIND extends Double {
    public FMIND() {
        super("fmin.d", "Floating MINimum (64 bit): assigns f1 to the smaller of f1 and f3", "0010101", "000");
    }

    public Float64 compute(Float64 f1, Float64 f2, Environment env) {
        return jsoftfloat.operations.Comparisons.minimumNumber(f1,f2,env);
    }
}
