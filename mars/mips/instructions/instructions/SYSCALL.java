package mars.mips.instructions.instructions;

import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.RegisterFile;
import mars.mips.instructions.BasicInstruction;
import mars.mips.instructions.BasicInstructionFormat;
import mars.mips.instructions.InstructionSet;

public class SYSCALL extends BasicInstruction {
    public SYSCALL() {
        super("ecall", "Issue a system call : Execute the system call specified by value in $v0",
                BasicInstructionFormat.I_FORMAT, "000000000000 00000 000 00000 1110011");
    }

    public void simulate(ProgramStatement statement) throws ProcessingException {
        InstructionSet.findAndSimulateSyscall(RegisterFile.getValue(2), statement);
    }
}