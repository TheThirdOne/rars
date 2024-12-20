package rars.riscv.instructions;

import rars.Globals;
import rars.riscv.hardware.AddressErrorException;

public class SH extends Store {
    public SH() {
        super("sh t1, -100(t2)", "Store halfword : Store the low-order 16 bits of t1 into the effective memory halfword address", "001");
    }

    public void store(int address, long data) throws AddressErrorException {
        Globals.memory.setHalf(address, (int)data & 0x0000FFFF);
    }
}



