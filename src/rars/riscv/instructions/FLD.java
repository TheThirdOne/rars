package rars.riscv.instructions;

import rars.Globals;
import rars.ProgramStatement;
import rars.SimulationException;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;
import rars.riscv.hardware.AddressErrorException;
import rars.riscv.hardware.FloatingPointRegisterFile;
import rars.riscv.hardware.RegisterFile;

public class FLD extends BasicInstruction {
    public FLD() {
        super("fld f1, -100(t1)", "Load a double from memory",
                BasicInstructionFormat.I_FORMAT, "ssssssssssss ttttt 011 fffff 0000111");
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        int[] operands = statement.getOperands();
        operands[1] = (operands[1] << 20) >> 20;
        try {
            long low = Globals.memory.getWord(RegisterFile.getValue(operands[2]) + operands[1]);
            long high = Globals.memory.getWord(RegisterFile.getValue(operands[2]) + operands[1]+4);
            FloatingPointRegisterFile.updateRegisterLong(operands[0], (high << 32) | (low & 0xFFFFFFFFL));
        } catch (AddressErrorException e) {
            throw new SimulationException(statement, e);
        }
    }
}