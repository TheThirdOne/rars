package mars.mips.instructions.instructions;

import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.RegisterFile;
import mars.mips.instructions.BasicInstruction;
import mars.mips.instructions.BasicInstructionFormat;
import mars.mips.instructions.InstructionSet;

public class BEQ extends BasicInstruction {
    public BEQ() {
        super("beq $t1,$t2,label", "Branch if equal : Branch to statement at label's address if $t1 and $t2 are equal",
                BasicInstructionFormat.S_BRANCH_FORMAT, "ttttttt sssss fffff 000 ttttt 1100011 ");
    }

    public void simulate(ProgramStatement statement) throws ProcessingException {
        int[] operands = statement.getOperands();

        if (RegisterFile.getValue(operands[0]) == RegisterFile.getValue(operands[1])) {
            InstructionSet.processBranch(operands[2]);
        }
    }
}