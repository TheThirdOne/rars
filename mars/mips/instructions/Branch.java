package mars.mips.instructions;

import mars.ProcessingException;
import mars.ProgramStatement;


/**
 * Base class for all branching instructions
 * <p>
 * Created mainly for making the branch simulator code simpler, but also does help with code reuse
 *
 * @author Benjamin Landers
 * @version June 2017
 */
public abstract class Branch extends BasicInstruction {
    public Branch(String usage, String description, String funct) {
        super(usage, description, BasicInstructionFormat.S_BRANCH_FORMAT,
                "ttttttt sssss fffff " + funct + " ttttt 1100011 ");
    }

    public void simulate(ProgramStatement statement) throws ProcessingException {
        if (willBranch(statement)) {
            InstructionSet.processBranch(statement.getOperands()[2]);
        }
    }

    /**
     * @param statement the program statement that carries the operands for this instruction
     * @return true if the Branch instruction will branch
     */
    public abstract boolean willBranch(ProgramStatement statement);
}