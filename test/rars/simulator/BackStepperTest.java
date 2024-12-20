package rars.simulator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import rars.Globals;
import rars.api.Program;
import rars.riscv.hardware.Memory;

import static org.junit.jupiter.api.Assertions.*;

class BackStepperTest {
    @AfterEach
    void tearDown() {
        Globals.program = null;
        Globals.memory = null;
    }

    @Test
    public void testSimulateRegister() throws Exception {
        Program p = new Program();
        p.assembleString("li a0, 42");
        p.setup(null, null);
        p.setRegisterValue("a0", 123);
        Globals.program = p.getCode(); // Shouldn't have to do this...

        assertEquals(123, p.getRegisterValue("a0"));
        assertTrue(p.getCode().getBackStepper().empty());

        Simulator.Reason reason = p.simulate();

        assertFalse(p.getCode().getBackStepper().empty());
        assertEquals(42, p.getRegisterValue("a0"));

        p.getCode().getBackStepper().backStep();

        assertTrue(p.getCode().getBackStepper().empty());
        assertEquals(123, p.getRegisterValue("a0"));
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
        Globals.program = p.getCode(); // Shouldn't have to do this...

        assertEquals(123, p.getMemory().getWord(dataSegment));
        assertTrue(p.getCode().getBackStepper().empty());

        Simulator.Reason reason = p.simulate();
        Globals.memory = p.getMemory(); // Neither this...

        assertFalse(p.getCode().getBackStepper().empty());
        assertEquals(42, p.getMemory().getWord(dataSegment));

        p.getCode().getBackStepper().backStep();

        assertTrue(p.getCode().getBackStepper().empty());
        assertEquals(123, p.getMemory().getWord(dataSegment));
    }
}