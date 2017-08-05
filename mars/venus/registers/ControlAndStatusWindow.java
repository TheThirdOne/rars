package mars.venus.registers;

import mars.riscv.hardware.ControlAndStatusRegisterFile;
import mars.riscv.hardware.Register;
import mars.venus.NumberDisplayBaseChooser;

public class ControlAndStatusWindow extends RegisterBlockWindow {
    /*
     * The tips to show when hovering over the names of the registers
     * TODO: Maintain order if any new CSRs are added
     */
    private static final String[] regToolTips = {
            /*ustatus"*/ "Interrupt status information (set the lowest bit to enable exceptions)",
            /*fflags*/ "The accumulated floating point flags",
            /*frm*/    "Rounding mode for floating point operatations (currently ignored)",
            /*fcsr*/   "Both frm and fflags",
            /*uie*/    "Finer control for which interrupts are enabled",
            /*utvec*/  "The base address of the interrupt handler",
            /*uscratch*/"Scratch for processing inside the interrupt handler",
            /*uepc*/   "PC at the time the interrupt was triggered",
            /*ucause*/ "Cause of the interrupt (top bit is interrupt vs trap)",
            /*utval*/  "Value associated with the cause",
            /*uip*/    "Shows if any interrupt is pending and what type"
    };

    public ControlAndStatusWindow() {
        super(ControlAndStatusRegisterFile.getRegisters(), regToolTips, "Current 32 bit value");
    }

    protected String formatRegister(Register value, int base) {
        return NumberDisplayBaseChooser.formatNumber(value.getValue(), base); // Perhaps make this always in hex
    }

    protected void beginObserving() {
        ControlAndStatusRegisterFile.addRegistersObserver(this);
    }

    protected void endObserving() {
        ControlAndStatusRegisterFile.deleteRegistersObserver(this);
    }

    public void resetRegisters() {
        ControlAndStatusRegisterFile.resetRegisters();
    }
}