package rars.riscv.instructions;

import jsoftfloat.Environment;
import jsoftfloat.types.Float32;
import rars.ProgramStatement;
import rars.SimulationException;
import rars.riscv.hardware.ControlAndStatusRegisterFile;
import rars.riscv.hardware.FloatingPointRegisterFile;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;

public class FSQRTS extends BasicInstruction {
    public FSQRTS() {
        super("fsqrt.s f1, f2, dyn", "Floating SQuare RooT: Assigns f1 to the square root of f2",
                BasicInstructionFormat.I_FORMAT, "0101100 00000 sssss ttt fffff 1010011");
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        int[] operands = statement.getOperands();
        Environment e = new Environment();
        e.mode = Floating.getRoundingMode(operands[2],statement);
        Float32 result = jsoftfloat.operations.Arithmetic.squareRoot(new Float32(FloatingPointRegisterFile.getValue(operands[1])),e);
        Floating.setfflags(e);
        FloatingPointRegisterFile.updateRegister(operands[0],result.bits);
    }
}