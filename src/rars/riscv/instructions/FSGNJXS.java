package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.riscv.hardware.FloatingPointRegisterFile;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;

public class FSGNJXS extends BasicInstruction {
    public FSGNJXS() {
        super("fsgnjx.s f1, f2, f3", "Floating point sign injection (xor):  xor the sign bit of f2 with the sign bit of f3 and assign it to f1",
                BasicInstructionFormat.R_FORMAT, "0010000 ttttt sssss 010 fffff 1010011");
    }

    public void simulate(ProgramStatement statement) {
        int[] operands = statement.getOperands();
        int f2 = FloatingPointRegisterFile.getValue(operands[1]), f3 = FloatingPointRegisterFile.getValue(operands[2]);
        int result = (f2 & 0x7FFFFFFF) | ((f2 ^ f3) & 0x80000000);
        FloatingPointRegisterFile.updateRegister(operands[0], result);
    }
}