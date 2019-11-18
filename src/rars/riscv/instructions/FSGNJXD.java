package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;
import rars.riscv.hardware.FloatingPointRegisterFile;

public class FSGNJXD extends BasicInstruction {
    public FSGNJXD() {
        super("fsgnjx.d f1, f2, f3", "Floating point sign injection (xor 64 bit):  xor the sign bit of f2 with the sign bit of f3 and assign it to f1",
                BasicInstructionFormat.R_FORMAT, "0010001 ttttt sssss 010 fffff 1010011");
    }

    public void simulate(ProgramStatement statement) {
        int[] operands = statement.getOperands();
        long f2 = FloatingPointRegisterFile.getValueLong(operands[1]), f3 = FloatingPointRegisterFile.getValueLong(operands[2]);
        long result = (f2 & 0x7FFFFFFF_FFFFFFFFL) | ((f2 ^ f3) & 0x80000000_00000000L);
        FloatingPointRegisterFile.updateRegisterLong(operands[0], result);
    }
}