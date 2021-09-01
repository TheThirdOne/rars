package rars.riscv.instructions;

import rars.Globals;
import rars.riscv.hardware.AddressErrorException;
import rars.ProgramStatement;
import rars.SimulationException;
import rars.riscv.hardware.RegisterFile;
import rars.riscv.hardware.ReservationTable.bitWidth;

public class LRD extends Atomic {
    public LRD() {
        super("lr.d t1, (t2)", "Set t1 to contents of effective memory word address and reserve", "011", "00010", true);
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        int[] operands = statement.getOperands();
        try {
            int hart = statement.getCurrentHart();
            if (hart == -1)
                RegisterFile.updateRegister(operands[0], load(RegisterFile.getValue(operands[1]), hart));
            else
                RegisterFile.updateRegister(operands[0], load(RegisterFile.getValue(operands[1], hart), hart), hart);
        } catch (AddressErrorException e) {
            throw new SimulationException(statement, e);
        }
    }

    private long load(int address, int hart) throws AddressErrorException {
        Globals.reservationTables.reserveAddress(hart + 1, address, bitWidth.doubleWord);
        return Globals.memory.getDoubleWord(address);
    }
}
