package com.github.unaimillan.rars.riscv.instructions;

import com.github.unaimillan.jsoftfloat.Environment;
import com.github.unaimillan.jsoftfloat.types.Float64;
import com.github.unaimillan.rars.ProgramStatement;
import com.github.unaimillan.rars.SimulationException;
import com.github.unaimillan.rars.riscv.BasicInstruction;
import com.github.unaimillan.rars.riscv.BasicInstructionFormat;
import com.github.unaimillan.rars.riscv.hardware.FloatingPointRegisterFile;
import com.github.unaimillan.rars.riscv.hardware.RegisterFile;

public class FCVTWUD extends BasicInstruction {
    public FCVTWUD() {
        super("fcvt.wu.d t1, f1, dyn", "Convert unsinged integer from double: Assigns the value of f1 (rounded) to t1",
                BasicInstructionFormat.I_FORMAT, "1100001 00001 sssss ttt fffff 1010011");
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        int[] operands = statement.getOperands();
        Environment e = new Environment();
        e.mode = Floating.getRoundingMode(operands[2],statement);
        Float64 in = new Float64(FloatingPointRegisterFile.getValueLong(operands[1]));
        int out = com.github.unaimillan.jsoftfloat.operations.Conversions.convertToUnsignedInt(in,e,false);
        Floating.setfflags(e);
        RegisterFile.updateRegister(operands[0],out);
    }
}