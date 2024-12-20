package rars.riscv.syscalls;

import rars.ProgramStatement;
import rars.riscv.AbstractSyscall;
import rars.riscv.hardware.RegisterFile;

public class SyscallSleep extends AbstractSyscall {
    public SyscallSleep() {
        super("Sleep", "Set the current thread to sleep for a time (not precise)", "a0 = time to sleep in milliseconds", "N/A");
    }

    public void simulate(ProgramStatement statement) {
        try {
            Thread.sleep(RegisterFile.getValue("a0")); // units of milliseconds  1000 millisec = 1 sec.
        } catch (InterruptedException e) {
        }
    }

}
