package rars.riscv.instructions;

import jsoftfloat.Environment;
import jsoftfloat.types.Float32;

public class FADDS extends Floating {
    public FADDS() {
        super("fadd.s", "Floating ADD: assigns f1 to f2 + f3", "0000000");
    }

    @Override
    public Float32 compute(Float32 f1, Float32 f2, Environment e) {
        return jsoftfloat.operations.Arithmetic.add(f1,f2,e);
    }
}
