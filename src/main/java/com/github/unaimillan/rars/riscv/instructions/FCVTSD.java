package com.github.unaimillan.rars.riscv.instructions;

import com.github.unaimillan.jsoftfloat.Environment;
import com.github.unaimillan.jsoftfloat.types.Float32;
import com.github.unaimillan.jsoftfloat.types.Float64;
import com.github.unaimillan.rars.ProgramStatement;
import com.github.unaimillan.rars.SimulationException;
import com.github.unaimillan.rars.riscv.BasicInstruction;
import com.github.unaimillan.rars.riscv.BasicInstructionFormat;
import com.github.unaimillan.rars.riscv.hardware.FloatingPointRegisterFile;

public class FCVTSD extends BasicInstruction {
    public FCVTSD() {
        super("fcvt.s.d f1, f2, dyn", "Convert a double to a float: Assigned the value of f2 to f1",
                BasicInstructionFormat.R4_FORMAT,"0100000 00001 sssss ttt fffff 1010011");
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        int[] operands = statement.getOperands();
        Environment e = new Environment();
        e.mode = Floating.getRoundingMode(operands[2],statement);
        Float64 in = new Float64(FloatingPointRegisterFile.getValueLong(operands[1]));
        Float32 out = new Float32(0);
        out = convert(in,out,e);
        Floating.setfflags(e);
        FloatingPointRegisterFile.updateRegister(operands[0],out.bits);
    }
    // Kindof a long type, but removes duplicate code and would make it easy for quads to be implemented.
    public static <S extends com.github.unaimillan.jsoftfloat.types.Floating<S>,D extends com.github.unaimillan.jsoftfloat.types.Floating<D>>
        S convert(D toconvert, S constructor, Environment e){
        if(toconvert.isInfinite()){
            return toconvert.isSignMinus() ? constructor.NegativeInfinity() : constructor.Infinity();
        }
        if(toconvert.isZero()){
            return toconvert.isSignMinus() ? constructor.NegativeZero() : constructor.Zero();
        }
        if(toconvert.isNaN()){
            return constructor.NaN();
        }
        return constructor.fromExactFloat(toconvert.toExactFloat(),e);
    }
}