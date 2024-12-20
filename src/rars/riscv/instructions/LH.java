package rars.riscv.instructions;

import rars.Globals;
import rars.riscv.hardware.AddressErrorException;

public class LH extends Load {
    public LH() {
        super("lh t1, -100(t2)", "Set t1 to sign-extended 16-bit value from effective memory halfword address", "001");
    }

    public long load(int address) throws AddressErrorException {
        return (Globals.memory.getHalf(address) << 16) >> 16; // Shifting sign extends
    }
}



