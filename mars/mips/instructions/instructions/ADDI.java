package mars.mips.instructions.instructions;

import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.RegisterFile;
import mars.mips.instructions.BasicInstruction;
import mars.mips.instructions.BasicInstructionFormat;
import mars.simulator.Exceptions;

public class ADDI extends BasicInstruction {
    public ADDI() {
        super("addi $t1,$t2,-100", "Addition immediate: set $t1 to ($t2 plus signed 12-bit immediate)",
                BasicInstructionFormat.I_FORMAT, "tttttttttttt sssss 000 fffff 0010011");
    }

    public void simulate(ProgramStatement statement) throws ProcessingException {
        int[] operands = statement.getOperands();
        int add1 = RegisterFile.getValue(operands[1]);
        int add2 = operands[2] << 12 >> 12;
        int sum = add1 + add2;
        RegisterFile.updateRegister(operands[0], sum);
    }
}
