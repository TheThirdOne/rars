package rars.riscv.instructions;

import jsoftfloat.Environment;
import jsoftfloat.types.Float32;

public class FNMSUBS extends FusedFloat {
    public FNMSUBS() {
        super("fnmsub.s f1, f2, f3, f4", "Fused Negated Multiply Subatract: Assigns -(f2*f3-f4) to f1", "10");
    }

    public Float32 compute(Float32 f1, Float32 f2, Float32 f3, Environment e){
        flipRounding(e);
        return jsoftfloat.operations.Arithmetic.fusedMultiplyAdd(f1,f2,f3.negate(),e).negate();
    }
}
