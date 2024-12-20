package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.SimulationException;
import rars.riscv.hardware.ControlAndStatusRegisterFile;
import rars.riscv.hardware.RegisterFile;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;

public class CSRRC extends BasicInstruction {
    public CSRRC() {
        super("csrrc t0, fcsr, t1", "Atomic Read/Clear CSR: read from the CSR into t0 and clear bits of the CSR according to t1",
                BasicInstructionFormat.I_FORMAT, "ssssssssssss ttttt 011 fffff 1110011");
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        int[] operands = statement.getOperands();
        try {
            long csr = ControlAndStatusRegisterFile.getValueLong(operands[1]);
            if (operands[2] != 0) {
                if(ControlAndStatusRegisterFile.clearRegister(operands[1], RegisterFile.getValueLong(operands[2]))){
                    throw new SimulationException(statement, "Attempt to write to read-only CSR", SimulationException.ILLEGAL_INSTRUCTION);
                }
            }
            RegisterFile.updateRegister(operands[0], csr);
        } catch (NullPointerException e) {
            throw new SimulationException(statement, "Attempt to access unavailable CSR", SimulationException.ILLEGAL_INSTRUCTION);
        }
    }
}
