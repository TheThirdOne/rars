package mars.mips.instructions.instructions;

import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.RegisterFile;
import mars.mips.instructions.BasicInstruction;
import mars.mips.instructions.BasicInstructionFormat;
import mars.simulator.Exceptions;

public class ADDI extends BasicInstruction {
    public ADDI() {
        super("addi $t1,$t2,-100", "Addition immediate with overflow : set $t1 to ($t2 plus signed 16-bit immediate)",
                BasicInstructionFormat.I_FORMAT, "001000 sssss fffff tttttttttttttttt");
    }

    public void simulate(ProgramStatement statement) throws ProcessingException {
        int[] operands = statement.getOperands();
        int add1 = RegisterFile.getValue(operands[1]);
        int add2 = operands[2] << 16 >> 16;
        int sum = add1 + add2;
        // overflow on A+B detected when A and B have same sign and A+B has other sign.
        if ((add1 >= 0 && add2 >= 0 && sum < 0)
                || (add1 < 0 && add2 < 0 && sum >= 0)) {
            throw new ProcessingException(statement,
                    "arithmetic overflow", Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION);
        }
        RegisterFile.updateRegister(operands[0], sum);
    }
}
