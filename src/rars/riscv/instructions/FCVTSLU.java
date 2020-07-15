package rars.riscv.instructions;

import jsoftfloat.Environment;
import jsoftfloat.types.Float32;
import rars.ProgramStatement;
import rars.SimulationException;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;
import rars.riscv.hardware.FloatingPointRegisterFile;
import rars.riscv.hardware.RegisterFile;

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
        Float32 converted = jsoftfloat.operations.Conversions.convertFromInt(unsigned,e,tmp);
        Floating.setfflags(e);
        FloatingPointRegisterFile.updateRegister(operands[0],converted.bits);
    }
}

