package com.github.unaimillan.rars.riscv.instructions;

import com.github.unaimillan.jsoftfloat.Environment;
import com.github.unaimillan.jsoftfloat.types.Float64;
import com.github.unaimillan.rars.ProgramStatement;
import com.github.unaimillan.rars.SimulationException;
import com.github.unaimillan.rars.riscv.BasicInstruction;
import com.github.unaimillan.rars.riscv.BasicInstructionFormat;
import com.github.unaimillan.rars.riscv.hardware.FloatingPointRegisterFile;
import com.github.unaimillan.rars.riscv.hardware.RegisterFile;

import java.math.BigInteger;

public class FCVTDWU extends BasicInstruction {
    public FCVTDWU() {
        super("fcvt.d.wu f1, t1, dyn", "Convert double from unsigned integer: Assigns the value of t1 to f1",
                BasicInstructionFormat.I_FORMAT, "1101001 00001 sssss ttt fffff 1010011");
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        int[] operands = statement.getOperands();
        Environment e = new Environment();
        e.mode = Floating.getRoundingMode(operands[2],statement);
        Float64 tmp = new Float64(0);
        Float64 converted = com.github.unaimillan.jsoftfloat.operations.Conversions.convertFromInt(BigInteger.valueOf(RegisterFile.getValue(operands[1]) & 0xFFFFFFFFL),e,tmp);
        Floating.setfflags(e);
        FloatingPointRegisterFile.updateRegisterLong(operands[0],converted.bits);
    }
}

