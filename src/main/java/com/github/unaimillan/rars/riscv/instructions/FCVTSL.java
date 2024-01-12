package com.github.unaimillan.rars.riscv.instructions;

import com.github.unaimillan.jsoftfloat.Environment;
import com.github.unaimillan.jsoftfloat.types.Float32;
import com.github.unaimillan.rars.ProgramStatement;
import com.github.unaimillan.rars.SimulationException;
import com.github.unaimillan.rars.riscv.BasicInstruction;
import com.github.unaimillan.rars.riscv.BasicInstructionFormat;
import com.github.unaimillan.rars.riscv.hardware.FloatingPointRegisterFile;
import com.github.unaimillan.rars.riscv.hardware.RegisterFile;

import java.math.BigInteger;

public class FCVTSL extends BasicInstruction {
    public FCVTSL() {
        super("fcvt.s.l f1, t1, dyn", "Convert float from long: Assigns the value of t1 to f1",
                BasicInstructionFormat.I_FORMAT, "1101000 00010 sssss ttt fffff 1010011",true);
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        int[] operands = statement.getOperands();
        Environment e = new Environment();
        e.mode = Floating.getRoundingMode(operands[2],statement);
        Float32 tmp = new Float32(0);
        Float32 converted = com.github.unaimillan.jsoftfloat.operations.Conversions.convertFromInt(BigInteger.valueOf(RegisterFile.getValueLong(operands[1])),e,tmp);
        Floating.setfflags(e);
        FloatingPointRegisterFile.updateRegister(operands[0],converted.bits);
    }
}

