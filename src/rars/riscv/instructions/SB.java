package rars.riscv.instructions;

import rars.Globals;
import rars.riscv.hardware.AddressErrorException;

public class SB extends Store {
    public SB() {
        super("sb t1, -100(t2)", "Store byte : Store the low-order 8 bits of t1 into the effective memory byte address", "000");
    }

    public void store(int address, long data) throws AddressErrorException {
        Globals.memory.setByte(address, (int)data & 0x000000FF);
    }
}



