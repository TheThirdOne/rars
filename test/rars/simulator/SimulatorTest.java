package rars.simulator;

import org.junit.jupiter.api.Test;
import rars.api.Program;
import rars.riscv.hardware.Memory;

import static org.junit.jupiter.api.Assertions.*;

class SimulatorTest {

    @Test
    public void testSimulateRegister() throws Exception {
        Program p = new Program();
        p.assembleString("li a0, 42");
        p.setup(null, null);
        p.setRegisterValue("a0", 123);

        assertEquals(123, p.getRegisterValue("a0"));

        Simulator.Reason reason = p.simulate();

        assertEquals(Simulator.Reason.CLIFF_TERMINATION, reason);
        assertEquals(42, p.getRegisterValue("a0"));
        assertEquals(0, p.getExitCode());
    }

    @Test
    public void testSimulateMemory() throws Exception {
        Program p = new Program();
        p.assembleString("sw a0, 0(a1)");
        p.setup(null, null);
        int dataSegment = Memory.dataBaseAddress;
        p.setRegisterValue("a0", 42);
        p.setRegisterValue("a1", dataSegment);
        p.getMemory().setWord(dataSegment, 123);

        assertEquals(123, p.getMemory().getWord(dataSegment));

        Simulator.Reason reason = p.simulate();

        assertEquals(Simulator.Reason.CLIFF_TERMINATION, reason);
        assertEquals(42, p.getMemory().getWord(dataSegment));
        assertEquals(0, p.getExitCode());
    }
}