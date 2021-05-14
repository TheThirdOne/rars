package rars.riscv.instructions;

import rars.Globals;
import rars.riscv.hardware.AddressErrorException;
import rars.ProgramStatement;
import rars.SimulationException;
import rars.riscv.hardware.AddressErrorException;
import rars.riscv.hardware.RegisterFile;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;

public class SC_W extends Atomic {
    public SC_W() {
        super("sc.w t1, (t2)", "Try to store t1 to contents of effective memory word address, sets t0 to store attempt result", "010", "00011");
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        int[] operands = statement.getOperands();
        System.out.println(operands[0]);
        System.out.println(operands[1]);
        System.out.println(operands[2]);
        operands[1] = (operands[1] << 20) >> 20;
        try {
            store(RegisterFile.getValue(0), RegisterFile.getValue(operands[0]));
        } catch (AddressErrorException e) {
            throw new SimulationException(statement, e);
        }
    }

    private void store(int address, long value) throws AddressErrorException {
        Globals.memory.setWord(address, data);
    }
}
