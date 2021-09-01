package rars.riscv.instructions;

import rars.Globals;
import rars.ProgramStatement;
import rars.SimulationException;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;
import rars.riscv.hardware.AddressErrorException;
import rars.riscv.hardware.FloatingPointRegisterFile;
import rars.riscv.hardware.RegisterFile;

public class FSD extends BasicInstruction {
    public FSD() {
        super("fsd f1, -100(t1)", "Store a double to memory",
                BasicInstructionFormat.S_FORMAT, "sssssss fffff ttttt 011 sssss 0100111");
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        int[] operands = statement.getOperands();
        int hart = statement.getCurrentHart();
        operands[1] = (operands[1] << 20) >> 20;
        try {
            Globals.memory.setDoubleWord(((hart == -1)
                    ? RegisterFile.getValue(operands[2])
                    : RegisterFile.getValue(operands[2], hart)) + operands[1],
                    (hart == -1)
                        ? FloatingPointRegisterFile.getValueLong(operands[0])
                        : FloatingPointRegisterFile.getValueLong(operands[0], hart));
        } catch (AddressErrorException e) {
            throw new SimulationException(statement, e);
        }
    }
}
