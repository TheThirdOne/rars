package rars.riscv.instructions;

import rars.Globals;
import rars.riscv.hardware.AddressErrorException;

public class SW extends Store {
    public SW() {
        super("sw t1, -100(t2)", "Store word : Store contents of t1 into effective memory word address", "010");
    }

    public void store(int address, long data) throws AddressErrorException {
        Globals.memory.setWord(address, (int) data);
    }
}



