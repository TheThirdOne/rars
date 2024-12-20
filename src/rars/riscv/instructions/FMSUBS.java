package rars.riscv.instructions;

import jsoftfloat.Environment;
import jsoftfloat.types.Float32;

public class FMSUBS extends FusedFloat {
    public FMSUBS() {
        super("fmsub.s f1, f2, f3, f4", "Fused Multiply Subatract: Assigns f2*f3-f4 to f1", "01");
    }

    public Float32 compute(Float32 f1, Float32 f2, Float32 f3, Environment e){
        return jsoftfloat.operations.Arithmetic.fusedMultiplyAdd(f1,f2,f3.negate(),e);
    }
}
