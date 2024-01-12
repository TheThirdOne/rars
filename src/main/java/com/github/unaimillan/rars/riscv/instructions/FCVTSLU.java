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

public class FCVTSLU extends BasicInstruction {
    public FCVTSLU() {
        super("fcvt.s.lu f1, t1, dyn", "Convert float from unsigned long: Assigns the value of t1 to f1",
                BasicInstructionFormat.I_FORMAT, "1101000 00011 sssss ttt fffff 1010011",true);
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        int[] operands = statement.getOperands();
        Environment e = new Environment();
        e.mode = Floating.getRoundingMode(operands[2],statement);
        Float32 tmp = new Float32(0);
        long value = RegisterFile.getValueLong(operands[1]);
        BigInteger unsigned = BigInteger.valueOf(value);
        if (value < 0) {
            unsigned = unsigned.add(BigInteger.ONE.shiftLeft(64));
        }
        Float32 converted = com.github.unaimillan.jsoftfloat.operations.Conversions.convertFromInt(unsigned,e,tmp);
        Floating.setfflags(e);
        FloatingPointRegisterFile.updateRegister(operands[0],converted.bits);
    }
}

