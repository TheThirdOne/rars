package mars.mips.instructions.instructions;

import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.RegisterFile;
import mars.mips.instructions.BasicInstruction;
import mars.mips.instructions.BasicInstructionFormat;
import mars.mips.instructions.InstructionSet;

public class JAL extends BasicInstruction {
    public JAL() {
        super("jal target", "Jump and link : Set $ra to Program Counter (return address) then jump to statement at target address",
                BasicInstructionFormat.J_FORMAT, "000011 ffffffffffffffffffffffffff");
    }

    public void simulate(ProgramStatement statement) throws ProcessingException {
        int[] operands = statement.getOperands();
        InstructionSet.processReturnAddress(31);// RegisterFile.updateRegister(31, RegisterFile.getProgramCounter());
        InstructionSet.processJump((RegisterFile.getProgramCounter() & 0xF0000000) | (operands[0] << 2));
    }
}