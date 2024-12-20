package rars.riscv.instructions;

import jsoftfloat.Environment;
import jsoftfloat.types.Float32;

public class FMINS extends Floating {
    public FMINS() {
        super("fmin.s", "Floating MINimum: assigns f1 to the smaller of f1 and f3", "0010100", "000");
    }

    public Float32 compute(Float32 f1, Float32 f2, Environment env) {
        return jsoftfloat.operations.Comparisons.minimumNumber(f1,f2,env);
    }
}
