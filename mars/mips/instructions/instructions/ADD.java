package mars.mips.instructions.instructions;

import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.RegisterFile;
import mars.mips.instructions.BasicInstruction;
import mars.mips.instructions.BasicInstructionFormat;
import mars.simulator.Exceptions;

public class ADD extends BasicInstruction {
    public ADD() {
        super("add $t1,$t2,$t3", "Addition: set $t1 to ($t2 plus $t3)",
                BasicInstructionFormat.R_FORMAT, "0000000 ttttt sssss 000 fffff 0110011");
    }

    public void simulate(ProgramStatement statement) throws ProcessingException {
        int[] operands = statement.getOperands();
        int add1 = RegisterFile.getValue(operands[1]);
        int add2 = RegisterFile.getValue(operands[2]);
        int sum = add1 + add2;

        RegisterFile.updateRegister(operands[0], sum);
    }
}
