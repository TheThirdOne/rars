package com.github.unaimillan.rars.riscv.instructions;

import com.github.unaimillan.rars.Globals;
import com.github.unaimillan.rars.riscv.hardware.AddressErrorException;

public class LD extends Load {
    public LD() {
        super("ld t1, -100(t2)", "Set t1 to contents of effective memory double word address", "011",true);
    }

    public long load(int address) throws AddressErrorException {
        return Globals.memory.getDoubleWord(address);
    }
}
