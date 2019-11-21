package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;
import rars.riscv.hardware.FloatingPointRegisterFile;

public class FSGNJND extends BasicInstruction {
    public FSGNJND() {
        super("fsgnjn.d f1, f2, f3", "Floating point sign injection (inverted 64 bit):  replace the sign bit of f2 with the opposite of sign bit of f3 and assign it to f1",
                BasicInstructionFormat.R_FORMAT, "0010001 ttttt sssss 001 fffff 1010011");
    }

    public void simulate(ProgramStatement statement) {
        int[] operands = statement.getOperands();
        long result = (FloatingPointRegisterFile.getValueLong(operands[1]) & 0x7FFFFFFF_FFFFFFFFL) |
                   ((~FloatingPointRegisterFile.getValueLong(operands[2])) & 0x80000000_00000000L);
        FloatingPointRegisterFile.updateRegisterLong(operands[0], result);
    }
}