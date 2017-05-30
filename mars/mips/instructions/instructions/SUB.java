package mars.mips.instructions.instructions;

import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.RegisterFile;
import mars.mips.instructions.BasicInstruction;
import mars.mips.instructions.BasicInstructionFormat;
import mars.simulator.Exceptions;

public class SUB extends BasicInstruction {
    public SUB() {
        super("sub $t1,$t2,$t3", "Subtraction with overflow : set $t1 to ($t2 minus $t3)",
                BasicInstructionFormat.R_FORMAT, "000000 sssss ttttt fffff 00000 100010");
    }

    public void simulate(ProgramStatement statement) throws ProcessingException {
        int[] operands = statement.getOperands();
        int sub1 = RegisterFile.getValue(operands[1]);
        int sub2 = RegisterFile.getValue(operands[2]);
        int dif = sub1 - sub2;
        // overflow on A-B detected when A and B have opposite signs and A-B has B's sign
        if ((sub1 >= 0 && sub2 < 0 && dif < 0)
                || (sub1 < 0 && sub2 >= 0 && dif >= 0)) {
            throw new ProcessingException(statement,
                    "arithmetic overflow", Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION);
        }
        RegisterFile.updateRegister(operands[0], dif);
    }
}
