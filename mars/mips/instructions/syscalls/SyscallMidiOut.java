package mars.mips.instructions.syscalls;

import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.RegisterFile;
import mars.mips.instructions.AbstractSyscall;


/*
Copyright (c) 2003-2009,  Pete Sanderson and Kenneth Vollmar

Developed by Pete Sanderson (psanderson@otterbein.edu)
and Kenneth Vollmar (kenvollmar@missouristate.edu)

Permission is hereby granted, free of charge, to any person obtaining 
a copy of this software and associated documentation files (the 
"Software"), to deal in the Software without restriction, including 
without limitation the rights to use, copy, modify, merge, publish, 
distribute, sublicense, and/or sell copies of the Software, and to 
permit persons to whom the Software is furnished to do so, subject 
to the following conditions:

The above copyright notice and this permission notice shall be 
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION 
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

(MIT license, http://www.opensource.org/licenses/mit-license.html)
 */

/**
 * Service to output simulated MIDI tone to sound card.  The call returns
 * immediately upon generating the tone.  By contrast, syscall 33
 * (MidiOutSync) does not return until tone duration has elapsed.
 */
public class SyscallMidiOut extends AbstractSyscall {

    // Endpoints of ranges for the three "byte" parameters.  The duration
    // parameter is limited at the high end only by the int range.
    static final int rangeLowEnd = 0;
    static final int rangeHighEnd = 127;

    /**
     * Build an instance of the MIDI (simulated) out syscall.  Default service number
     * is 31 and name is "MidiOut".
     */
    public SyscallMidiOut() {
        super(31, "MidiOut");
    }

    /**
     * Performs syscall function to send MIDI output to sound card.  This requires
     * four arguments in registers $a0 through $a3.<br>
     * $a0 - pitch (note).  Integer value from 0 to 127, with 60 being middle-C on a piano.<br>
     * $a1 - duration. Integer value in milliseconds.<br>
     * $a2 - instrument.  Integer value from 0 to 127, with 0 being acoustic grand piano.<br>
     * $a3 - volume.  Integer value from 0 to 127.<br>
     * Default values, in case any parameters are outside the above ranges, are $a0=60, $a1=1000,
     * $a2=0, $a3=100.<br>
     * See MARS documentation elsewhere or www.midi.org for more information.  Note that the pitch,
     * instrument and volume value ranges 0-127 are from javax.sound.midi; actual MIDI instruments
     * use the range 1-128.
     */
    public void simulate(ProgramStatement statement) throws ProcessingException {
        int pitch = RegisterFile.getValue(4); // $a0
        int duration = RegisterFile.getValue(5); // $a1
        int instrument = RegisterFile.getValue(6); // $a2
        int volume = RegisterFile.getValue(7); // $a3
        if (pitch < rangeLowEnd || pitch > rangeHighEnd) pitch = ToneGenerator.DEFAULT_PITCH;
        if (duration < 0) duration = ToneGenerator.DEFAULT_DURATION;
        if (instrument < rangeLowEnd || instrument > rangeHighEnd) instrument = ToneGenerator.DEFAULT_INSTRUMENT;
        if (volume < rangeLowEnd || volume > rangeHighEnd) volume = ToneGenerator.DEFAULT_VOLUME;
        new ToneGenerator().generateTone((byte) pitch, duration, (byte) instrument, (byte) volume);
    }

}

