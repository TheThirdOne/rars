package mars.mips.instructions.instructions;

import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.RegisterFile;
import mars.mips.instructions.BasicInstruction;
import mars.mips.instructions.BasicInstructionFormat;
import mars.simulator.Exceptions;

public class SUB extends BasicInstruction {
    public SUB() {
        super("sub $t1,$t2,$t3", "Subtraction: set $t1 to ($t2 minus $t3)",
                BasicInstructionFormat.R_FORMAT, "0100000 ttttt sssss 000 fffff 0110011");
    }

    public void simulate(ProgramStatement statement) throws ProcessingException {
        int[] operands = statement.getOperands();
        int sub1 = RegisterFile.getValue(operands[1]);
        int sub2 = RegisterFile.getValue(operands[2]);
        int dif = sub1 - sub2;
        RegisterFile.updateRegister(operands[0], dif);
    }
}
