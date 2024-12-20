package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.riscv.hardware.RegisterFile;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;
import rars.riscv.InstructionSet;

public class JALR extends BasicInstruction {
    public JALR() {
        super("jalr t1, t2, -100", "Jump and link register: Set t1 to Program Counter (return address) then jump to statement at t2 + immediate",
                BasicInstructionFormat.I_FORMAT, "tttttttttttt sssss 000 fffff 1100111");
    }

    public void simulate(ProgramStatement statement) {
        int[] operands = statement.getOperands();
        int target = RegisterFile.getValue(operands[1]);
        InstructionSet.processReturnAddress(operands[0]);
        // Set PC = $t2 + immediate with the last bit set to 0
        InstructionSet.processJump((target + ((operands[2]<<20)>>20)) & 0xFFFFFFFE);
    }
}