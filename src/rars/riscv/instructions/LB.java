package rars.riscv.instructions;

import rars.Globals;
import rars.riscv.hardware.AddressErrorException;

public class LB extends Load {
    public LB() {
        super("lb t1, -100(t2)", "Set t1 to sign-extended 8-bit value from effective memory byte address", "000");
    }

    public long load(int address) throws AddressErrorException {
        return (Globals.memory.getByte(address) << 24) >> 24; // Shifting sign extends
    }
}



