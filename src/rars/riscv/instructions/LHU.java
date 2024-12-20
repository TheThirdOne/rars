package rars.riscv.instructions;

import rars.Globals;
import rars.riscv.hardware.AddressErrorException;

public class LHU extends Load {
    public LHU() {
        super("lhu t1, -100(t2)", "Set t1 to zero-extended 16-bit value from effective memory halfword address", "101");
    }

    public long load(int address) throws AddressErrorException {
        return Globals.memory.getHalf(address) & 0x0000FFFF;
    }
}



