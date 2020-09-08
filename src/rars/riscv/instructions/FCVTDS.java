package rars.riscv.instructions;

import jsoftfloat.Environment;
import jsoftfloat.types.Float32;
import jsoftfloat.types.Float64;
import rars.ProgramStatement;
import rars.SimulationException;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;
import rars.riscv.hardware.FloatingPointRegisterFile;

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