package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.riscv.hardware.ControlAndStatusRegisterFile;
import rars.riscv.hardware.RegisterFile;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;

public class URET extends BasicInstruction {
    public URET() {
        super("uret", "Return from handling an interrupt or exception (to uepc)",
                BasicInstructionFormat.I_FORMAT, "000000000010 00000 000 00000 1110011");
    }

    public void simulate(ProgramStatement statement) {
        boolean upie = (ControlAndStatusRegisterFile.getValue("ustatus") & 0x10) == 0x10;
        ControlAndStatusRegisterFile.clearRegister("ustatus", 0x10); // Clear UPIE
        if (upie) { // Set UIE to UPIE
            ControlAndStatusRegisterFile.orRegister("ustatus", 0x1);
        } else {
            ControlAndStatusRegisterFile.clearRegister("ustatus", 0x1);
        }
        RegisterFile.setProgramCounter(ControlAndStatusRegisterFile.getValue("uepc"));
    }
}
