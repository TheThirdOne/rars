package mars.mips.instructions.instructions;

import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.RegisterFile;
import mars.mips.instructions.BasicInstruction;
import mars.mips.instructions.BasicInstructionFormat;
import mars.mips.instructions.InstructionSet;

public class JAL extends BasicInstruction {
    public JAL() {
        super("jal $t1, target", "Jump and link : Set $ra to Program Counter (return address) then jump to statement at target address",
                BasicInstructionFormat.U_JUMP_FORMAT, "s ssssssssss s ssssssss fffff 1101111 ");
    }

    public void simulate(ProgramStatement statement) throws ProcessingException {
        int[] operands = statement.getOperands();
        InstructionSet.processReturnAddress(operands[0]);// RegisterFile.updateRegister(31, RegisterFile.getProgramCounter());
        InstructionSet.processJump(RegisterFile.getProgramCounter() + (operands[1] << 1));
    }
}