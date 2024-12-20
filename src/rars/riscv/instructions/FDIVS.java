package rars.riscv.instructions;

import jsoftfloat.Environment;
import jsoftfloat.types.Float32;
import rars.riscv.hardware.ControlAndStatusRegisterFile;

public class FDIVS extends Floating {
    public FDIVS() {
        super("fdiv.s", "Floating DIVide: assigns f1 to f2 / f3", "0001100");
    }

    @Override
    public Float32 compute(Float32 f1, Float32 f2, Environment e) {
        return jsoftfloat.operations.Arithmetic.division(f1,f2,e);
    }
}
