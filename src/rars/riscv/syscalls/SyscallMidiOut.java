package rars.riscv.syscalls;

import rars.ProgramStatement;
import rars.riscv.AbstractSyscall;
import rars.riscv.hardware.RegisterFile;

public class SyscallMidiOut extends AbstractSyscall {
    // Endpoints of ranges for the three "byte" parameters.  The duration
    // parameter is limited at the high end only by the int range.
    private static final int rangeLowEnd = 0;
    private static final int rangeHighEnd = 127;

    public SyscallMidiOut() {
        super("MidiOut", "Outputs simulated MIDI tone to sound card (does not wait for sound to end).", "See MIDI note below", "N/A");
    }

    /**
     * Arguments:
     * a0 - pitch (note).  Integer value from 0 to 127, with 60 being middle-C on a piano.<br>
     * a1 - duration. Integer value in milliseconds.<br>
     * a2 - instrument.  Integer value from 0 to 127, with 0 being acoustic grand piano.<br>
     * a3 - volume.  Integer value from 0 to 127.<br>
     * <p>
     * Default values, in case any parameters are outside the above ranges, are a0=60, a1=1000,
     * a2=0, a3=100.<br>
     * <p>
     * See MARS/RARS documentation elsewhere or www.midi.org for more information.  Note that the pitch,
     * instrument and volume value ranges 0-127 are from javax.sound.midi; actual MIDI instruments
     * use the range 1-128.
     */
    public void simulate(ProgramStatement statement) {
        int pitch = RegisterFile.getValue("a0");
        int duration = RegisterFile.getValue("a1");
        int instrument = RegisterFile.getValue("a2");
        int volume = RegisterFile.getValue("a3");
        if (pitch < rangeLowEnd || pitch > rangeHighEnd) pitch = ToneGenerator.DEFAULT_PITCH;
        if (duration < 0) duration = ToneGenerator.DEFAULT_DURATION;
        if (instrument < rangeLowEnd || instrument > rangeHighEnd) instrument = ToneGenerator.DEFAULT_INSTRUMENT;
        if (volume < rangeLowEnd || volume > rangeHighEnd) volume = ToneGenerator.DEFAULT_VOLUME;
        new ToneGenerator().generateTone((byte) pitch, duration, (byte) instrument, (byte) volume);
    }

}

