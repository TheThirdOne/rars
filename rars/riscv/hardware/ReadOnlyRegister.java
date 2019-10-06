package rars.riscv.hardware;

public class ReadOnlyRegister extends Register {
    public ReadOnlyRegister(String name, int num, int val) {
        super(name, num, val); // reset value does not matter
    }
}
