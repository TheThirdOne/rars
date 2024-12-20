package rars.riscv.instructions;

import jsoftfloat.Environment;
import jsoftfloat.types.Float32;

public class FNMADDS extends FusedFloat {
    public FNMADDS() {
        super("fnmadd.s f1, f2, f3, f4", "Fused Negate Multiply Add: Assigns -(f2*f3+f4) to f1", "11");
    }

    public Float32 compute(Float32 f1, Float32 f2, Float32 f3, Environment e){
        // TODO: test if this is the right behaviour
        flipRounding(e);
        return jsoftfloat.operations.Arithmetic.fusedMultiplyAdd(f1,f2,f3,e).negate();
    }
}
