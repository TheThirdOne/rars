package com.github.unaimillan.rars.riscv.instructions;

import com.github.unaimillan.jsoftfloat.Environment;
import com.github.unaimillan.jsoftfloat.types.Float32;
import com.github.unaimillan.jsoftfloat.types.Float64;
import com.github.unaimillan.rars.ProgramStatement;
import com.github.unaimillan.rars.SimulationException;
import com.github.unaimillan.rars.riscv.BasicInstruction;
import com.github.unaimillan.rars.riscv.BasicInstructionFormat;
import com.github.unaimillan.rars.riscv.hardware.FloatingPointRegisterFile;

public class FCVTDS extends BasicInstruction {
    public FCVTDS() {
        super("fcvt.d.s f1, f2, dyn", "Convert a float to a double: Assigned the value of f2 to f1",
                BasicInstructionFormat.R4_FORMAT,"0100001 00000 sssss ttt fffff 1010011");
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        int[] operands = statement.getOperands();
        Environment e = new Environment();
        e.mode = Floating.getRoundingMode(operands[2],statement);
        Float32 in = new Float32(FloatingPointRegisterFile.getValue(operands[1]));
        Float64 out = new Float64(0);
        out = FCVTSD.convert(in,out,e);
        Floating.setfflags(e);
        FloatingPointRegisterFile.updateRegisterLong(operands[0],out.bits);
    }
}