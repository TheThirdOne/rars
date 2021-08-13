package rars.riscv.instructions;

import rars.Globals;
import rars.riscv.hardware.AddressErrorException;
import rars.ProgramStatement;
import rars.SimulationException;
import rars.riscv.hardware.RegisterFile;
import rars.riscv.hardware.ReservationTable.bitWidth;

public class SCD extends Atomic {
    public SCD() {
        super("sc.d t0, t1, (t2)", "Try to store t1 to contents of effective memory word address, sets t0 to store attempt result", "011", "00011", true);
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        int[] operands = statement.getOperands();
        int hart = statement.getCurrentHart();
        try {
            if (hart == -1) {
                long result = store(RegisterFile.getValue(operands[2]), RegisterFile.getValue(operands[1]), hart);
                RegisterFile.updateRegister(operands[0], result);
            } else {
                long result = store(RegisterFile.getValue(operands[2], hart), RegisterFile.getValue(operands[1], hart), hart);
                RegisterFile.updateRegister(operands[0], result, hart);
            }
        } catch (AddressErrorException e) {
            throw new SimulationException(statement, e);
        }
    }

    private long store(int address, int value, int hart) throws AddressErrorException {
        if (Globals.reservationTables.unreserveAddress(hart + 1, address, bitWidth.doubleWord)) {
            Globals.memory.setDoubleWord(address, value);
            return 0;
        }
        return 1;
    }
}
