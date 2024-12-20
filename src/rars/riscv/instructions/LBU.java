package rars.riscv.instructions;

import rars.Globals;
import rars.riscv.hardware.AddressErrorException;

public class LBU extends Load {
    public LBU() {
        super("lbu t1, -100(t2)", "Set t1 to zero-extended 8-bit value from effective memory byte address", "100");
    }

    public long load(int address) throws AddressErrorException {
        return Globals.memory.getByte(address) & 0x000000FF;
    }
}



