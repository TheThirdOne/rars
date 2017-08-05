package mars.venus.registers;

import mars.riscv.hardware.Register;
import mars.riscv.hardware.RegisterFile;
import mars.venus.NumberDisplayBaseChooser;

import java.util.Arrays;

public class RegistersWindow extends RegisterBlockWindow {
    /*
     * The tips to show when hovering over the names of the registers
     */
    private static final String[] regToolTips = {
                /* zero */  "constant 0",
                /* ra   */  "return address (used by function call)",
                /* sp   */  "stack pointer",
                /* gp   */  "pointer to global area",
                /* tp   */  "pointer to thread local data (not given a value)",
                /* t0   */  "temporary (not preserved across call)",
                /* t1   */  "temporary (not preserved across call)",
                /* t2   */  "temporary (not preserved across call)",
                /* s0   */  "saved temporary (preserved across call)",
                /* s1   */  "saved temporary (preserved across call)",
                /* a0   */  "argument 1 / return 1",
                /* a1   */  "argument 2 / return 2",
                /* a2   */  "argument 3",
                /* a3   */  "argument 4",
                /* a4   */  "argument 5",
                /* a5   */  "argument 6",
                /* a6   */  "argument 7",
                /* a7   */  "argument 8",
                /* s2   */  "saved temporary (preserved across call)",
                /* s3   */  "saved temporary (preserved across call)",
                /* s4   */  "saved temporary (preserved across call)",
                /* s5   */  "saved temporary (preserved across call)",
                /* s6   */  "saved temporary (preserved across call)",
                /* s7   */  "saved temporary (preserved across call)",
                /* s8   */  "saved temporary (preserved across call)",
                /* s9   */  "saved temporary (preserved across call)",
                /* s10  */  "saved temporary (preserved across call)",
                /* s11  */  "saved temporary (preserved across call)",
                /* t3   */  "temporary (not preserved across call)",
                /* t4   */  "temporary (not preserved across call)",
                /* t5   */  "temporary (not preserved across call)",
                /* t6   */  "temporary (not preserved across call)",
                /* pc   */  "program counter",
    };

    public RegistersWindow() {
        super(getRegisters(), regToolTips, "Current 32 bit value");
    }

    /*
     * A simple wrapper to add pc into the Registers array
     */
    private static Register[] getRegisters() {
        Register[] base = RegisterFile.getRegisters();
        Register[] out = Arrays.copyOf(base, base.length + 1);
        out[base.length] = RegisterFile.getProgramCounterRegister();
        return out;
    }

    protected String formatRegister(Register value, int base) {
        return NumberDisplayBaseChooser.formatNumber(value.getValue(), base);
    }

    protected void beginObserving() {
        RegisterFile.addRegistersObserver(this);
    }

    protected void endObserving() {
        RegisterFile.deleteRegistersObserver(this);
    }

    protected void resetRegisters() {
        RegisterFile.resetRegisters();
    }
}