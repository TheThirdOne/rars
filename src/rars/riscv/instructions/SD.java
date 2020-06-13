package rars.riscv.instructions;

import rars.Globals;
import rars.riscv.hardware.AddressErrorException;
// todo: this is a stub from sw
public class SD extends Store {
    public SD() {
        super("sd t1, -100(t2)", "Store word : Store contents of t1 into effective memory word address", "010");
    }

    public void store(int address, int data) throws AddressErrorException {
        Globals.memory.setWord(address, data);
    }
}



