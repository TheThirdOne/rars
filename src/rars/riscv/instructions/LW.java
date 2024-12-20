package rars.riscv.instructions;

import rars.Globals;
import rars.riscv.hardware.AddressErrorException;

public class LW extends Load {
    public LW() {
        super("lw t1, -100(t2)", "Set t1 to contents of effective memory word address", "010");
    }

    public long load(int address) throws AddressErrorException {
        return Globals.memory.getWord(address);
    }
}



