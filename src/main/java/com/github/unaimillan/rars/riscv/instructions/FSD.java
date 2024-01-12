package com.github.unaimillan.rars.riscv.instructions;

import com.github.unaimillan.rars.Globals;
import com.github.unaimillan.rars.ProgramStatement;
import com.github.unaimillan.rars.SimulationException;
import com.github.unaimillan.rars.riscv.BasicInstruction;
import com.github.unaimillan.rars.riscv.BasicInstructionFormat;
import com.github.unaimillan.rars.riscv.hardware.AddressErrorException;
import com.github.unaimillan.rars.riscv.hardware.FloatingPointRegisterFile;
import com.github.unaimillan.rars.riscv.hardware.RegisterFile;

public class FSD extends BasicInstruction {
    public FSD() {
        super("fsd f1, -100(t1)", "Store a double to memory",
                BasicInstructionFormat.S_FORMAT, "sssssss fffff ttttt 011 sssss 0100111");
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        int[] operands = statement.getOperands();
        operands[1] = (operands[1] << 20) >> 20;
        try {
            Globals.memory.setDoubleWord(RegisterFile.getValue(operands[2]) + operands[1], FloatingPointRegisterFile.getValueLong(operands[0]));
        } catch (AddressErrorException e) {
            throw new SimulationException(statement, e);
        }
    }
}