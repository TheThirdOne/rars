package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.riscv.hardware.FloatingPointRegisterFile;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;

public class FSGNJNS extends BasicInstruction {
    public FSGNJNS() {
        super("fsgnjn.s f1, f2, f3", "Floating point sign injection (inverted):  replace the sign bit of f2 with the opposite of sign bit of f3 and assign it to f1",
                BasicInstructionFormat.R_FORMAT, "0010000 ttttt sssss 001 fffff 1010011");
    }

    public void simulate(ProgramStatement statement) {
        int[] operands = statement.getOperands();
        int result = (FloatingPointRegisterFile.getValue(operands[1]) & 0x7FFFFFFF) | ((~FloatingPointRegisterFile.getValue(operands[2])) & 0x80000000);
        FloatingPointRegisterFile.updateRegister(operands[0], result);
    }
}