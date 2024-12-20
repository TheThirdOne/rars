package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.SimulationException;
import rars.riscv.hardware.ControlAndStatusRegisterFile;
import rars.riscv.hardware.RegisterFile;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;

public class CSRRWI extends BasicInstruction {
    public CSRRWI() {
        super("csrrwi t0, fcsr, 10", "Atomic Read/Write CSR Immediate: read from the CSR into t0 and write a constant into the CSR",
                BasicInstructionFormat.I_FORMAT, "ssssssssssss ttttt 101 fffff 1110011");
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        int[] operands = statement.getOperands();
        try {
            long csr = ControlAndStatusRegisterFile.getValueLong(operands[1]);
            if(ControlAndStatusRegisterFile.updateRegister(operands[1], operands[2])){
                throw new SimulationException(statement, "Attempt to write to read-only CSR", SimulationException.ILLEGAL_INSTRUCTION);
            }
            RegisterFile.updateRegister(operands[0], csr);
        } catch (NullPointerException e) {
            throw new SimulationException(statement, "Attempt to access unavailable CSR", SimulationException.ILLEGAL_INSTRUCTION);
        }
    }
}
