package com.github.unaimillan.rars.riscv.instructions;

import com.github.unaimillan.jsoftfloat.Environment;
import com.github.unaimillan.jsoftfloat.types.Float64;

public class FNMADDD extends FusedDouble {
    public FNMADDD() {
        super("fnmadd.d f1, f2, f3, f4", "Fused Negate Multiply Add (64 bit): Assigns -(f2*f3+f4) to f1", "11");
    }

    public Float64 compute(Float64 f1, Float64 f2, Float64 f3, Environment e){
        // TODO: test if this is the right behaviour
        FusedFloat.flipRounding(e);
        return com.github.unaimillan.jsoftfloat.operations.Arithmetic.fusedMultiplyAdd(f1,f2,f3,e).negate();
    }
}
