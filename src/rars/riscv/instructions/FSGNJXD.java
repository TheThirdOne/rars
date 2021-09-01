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
        int hart = statement.getCurrentHart();
        long f2 = (hart == -1)
                ? FloatingPointRegisterFile.getValueLong(operands[1])
                : FloatingPointRegisterFile.getValueLong(operands[1], hart);
        long f3 = (hart == -1)
                ? FloatingPointRegisterFile.getValueLong(operands[2])
                : FloatingPointRegisterFile.getValueLong(operands[2], hart);
        long result = (f2 & 0x7FFFFFFF_FFFFFFFFL) | ((f2 ^ f3) & 0x80000000_00000000L);
        if (hart == -1)
            FloatingPointRegisterFile.updateRegisterLong(operands[0], result);
        else
            FloatingPointRegisterFile.updateRegisterLong(operands[0], result, hart);
    }
}
