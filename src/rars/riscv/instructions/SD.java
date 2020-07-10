package rars.riscv.instructions;

import rars.Globals;
import rars.riscv.hardware.AddressErrorException;

public class SD extends Store {
    public SD() {
        super("sd t1, -100(t2)", "Store double word : Store contents of t1 into effective memory double word address", "011",true);
    }

    public void store(int address, long data) throws AddressErrorException {
        Globals.memory.setDoubleWord(address, data);
    }
}



