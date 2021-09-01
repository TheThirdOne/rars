package rars.riscv.instructions;

import jsoftfloat.Environment;
import jsoftfloat.operations.Arithmetic;
import jsoftfloat.types.Float64;
import rars.ProgramStatement;
import rars.SimulationException;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;
import rars.riscv.hardware.FloatingPointRegisterFile;

public class FSQRTD extends BasicInstruction {
    public FSQRTD() {
        super("fsqrt.d f1, f2, dyn", "Floating SQuare RooT (64 bit): Assigns f1 to the square root of f2",
                BasicInstructionFormat.I_FORMAT, "0101101 00000 sssss ttt fffff 1010011");
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        int[] operands = statement.getOperands();
        int hart = statement.getCurrentHart();
        Environment e = new Environment();
        e.mode = Floating.getRoundingMode(operands[2],statement);
        Float64 result = Arithmetic.squareRoot(new Float64((hart == -1)
                ? FloatingPointRegisterFile.getValueLong(operands[1])
                : FloatingPointRegisterFile.getValueLong(operands[1], hart)),
                e);
        Floating.setfflags(e, hart);
        if (hart == -1)
            FloatingPointRegisterFile.updateRegisterLong(operands[0], result.bits);
        else
            FloatingPointRegisterFile.updateRegisterLong(operands[0], result.bits, hart);
    }
}
