package mars.mips.instructions.instructions;

import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.RegisterFile;
import mars.mips.instructions.BasicInstruction;
import mars.mips.instructions.BasicInstructionFormat;
import mars.simulator.Exceptions;

public class ADD extends BasicInstruction {
    public ADD() {
        super("add $t1,$t2,$t3", "Addition with overflow : set $t1 to ($t2 plus $t3)",
                BasicInstructionFormat.R_FORMAT, "000000 sssss ttttt fffff 00000 100000");
    }

    public void simulate(ProgramStatement statement) throws ProcessingException {
        int[] operands = statement.getOperands();
        int add1 = RegisterFile.getValue(operands[1]);
        int add2 = RegisterFile.getValue(operands[2]);
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
