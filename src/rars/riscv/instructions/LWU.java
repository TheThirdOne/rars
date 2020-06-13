package rars.riscv.instructions;

import rars.Globals;
import rars.riscv.hardware.AddressErrorException;

// todo: update description
public class LWU extends Load {
    public LWU() {
        super("lwu t1, -100(t2)", "Set t1 to contents of effective memory word address", "110");
    }

    public long load(int address) throws AddressErrorException {
        return Globals.memory.getWord(address) & 0xFFFF_FFFFL;
    }
}
