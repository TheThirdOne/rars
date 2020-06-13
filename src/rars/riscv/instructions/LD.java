package rars.riscv.instructions;

import rars.Globals;
import rars.riscv.hardware.AddressErrorException;

// TODO: this is a stub from LW
public class LD extends Load {
    public LD() {
        super("lw t1, -100(t2)", "Set t1 to contents of effective memory word address", "010");
    }

    public int load(int address) throws AddressErrorException {
        return Globals.memory.getWord(address);
    }
}
