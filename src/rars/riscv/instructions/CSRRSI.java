package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.SimulationException;
import rars.riscv.hardware.ControlAndStatusRegisterFile;
import rars.riscv.hardware.RegisterFile;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;

public class CSRRSI extends BasicInstruction {
    public CSRRSI() {
        super("csrrsi t0, fcsr, 10", "Atomic Read/Set CSR Immediate: read from the CSR into t0 and logical or a constant into the CSR",
                BasicInstructionFormat.I_FORMAT, "ssssssssssss ttttt 110 fffff 1110011");
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        int[] operands = statement.getOperands();
        try {
            long csr = ControlAndStatusRegisterFile.getValueLong(operands[1]);
            if (operands[2] != 0){
                if(ControlAndStatusRegisterFile.orRegister(operands[1], operands[2])){
                    throw new SimulationException(statement, "Attempt to write to read-only CSR", SimulationException.ILLEGAL_INSTRUCTION);
                }
            }
            RegisterFile.updateRegister(operands[0], csr);
        } catch (NullPointerException e) {
            throw new SimulationException(statement, "Attempt to access unavailable CSR", SimulationException.ILLEGAL_INSTRUCTION);
        }
    }
}
