package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.riscv.hardware.RegisterFile;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;
import rars.riscv.Instruction;
import rars.riscv.InstructionSet;

public class JAL extends BasicInstruction {
    public JAL() {
        super("jal t1, target", "Jump and link : Set t1 to Program Counter (return address) then jump to statement at target address",
                BasicInstructionFormat.J_FORMAT, "s ssssssssss s ssssssss fffff 1101111 ");
    }

    public void simulate(ProgramStatement statement) {
        int[] operands = statement.getOperands();
        InstructionSet.processReturnAddress(operands[0]);
        InstructionSet.processJump(RegisterFile.getProgramCounter() - Instruction.INSTRUCTION_LENGTH + operands[1]);
    }
}