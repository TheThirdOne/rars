package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.SimulationException;
import rars.riscv.hardware.ControlAndStatusRegisterFile;
import rars.riscv.hardware.RegisterFile;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;

public class CSRRS extends BasicInstruction {
    public CSRRS() {
        super("csrrs t0, fcsr, t1", "Atomic Read/Set CSR: read from the CSR into t0 and logical or t1 into the CSR",
                BasicInstructionFormat.I_FORMAT, "ssssssssssss ttttt 010 fffff 1110011");
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        int[] operands = statement.getOperands();
        try {
            long csr = ControlAndStatusRegisterFile.getValueLong(operands[1]);
            if (operands[2] != 0) {
                if(ControlAndStatusRegisterFile.orRegister(operands[1], RegisterFile.getValueLong(operands[2]))) {
                    throw new SimulationException(statement, "Attempt to write to read-only CSR", SimulationException.ILLEGAL_INSTRUCTION);
                }
            }
            RegisterFile.updateRegister(operands[0], csr);
        } catch (NullPointerException e) {
            throw new SimulationException(statement, "Attempt to access unavailable CSR", SimulationException.ILLEGAL_INSTRUCTION);
        }
    }
}
