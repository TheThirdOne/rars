package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.SimulationException;
import rars.riscv.hardware.RegisterFile;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;
import rars.riscv.InstructionSet;

public class ECALL extends BasicInstruction {
    public ECALL() {
        super("ecall", "Issue a system call : Execute the system call specified by value in a7",
                BasicInstructionFormat.I_FORMAT, "000000000000 00000 000 00000 1110011");
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        InstructionSet.findAndSimulateSyscall(RegisterFile.getValue("a7"), statement);
    }
}