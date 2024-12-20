package rars.riscv.syscalls;


import javax.sound.midi.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////
//
//  The ToneGenerator and Tone classes were developed by Otterbein College
//  student Tony Brock in July 2007.  They simulate MIDI output through the
//  computers soundcard using classes and methods of the javax.sound.midi
//  package.
//
//  Max Hailperin <max@gustavus.edu> changed the interface of the
//  ToneGenerator class 2009-10-19 in order to
//  (1) provide a reliable way to wait for the completion of a
//       synchronous tone,
//  and while he was at it,
//  (2) improve the efficiency of asynchronous tones by using a thread
//      pool executor, and
//  (3) simplify the interface by removing all the unused versions
//       that provided default values for various parameters
/////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////


/*
 * Creates a Tone object and passes it to a thread to "play" it using MIDI.
 */
class ToneGenerator {

    /**
     * The default pitch value for the tone: 60 / middle C.
     */
    public final static byte DEFAULT_PITCH = 60;

    /**
     * The default duration of the tone: 1000 milliseconds.
     */
    public final static int DEFAULT_DURATION = 1000;

    /**
     * The default instrument of the tone: 0 / piano.
     */
    public final static byte DEFAULT_INSTRUMENT = 0;

    /**
     * The default volume of the tone: 100 (of 127).
     */
    public final static byte DEFAULT_VOLUME = 100;

    private static Executor threadPool = Executors.newCachedThreadPool();

    /**
     * Produces a Tone with the specified pitch, duration, and instrument,
     * and volume.
     *
     * @param pitch      the desired pitch in semitones - 0-127 where 60 is
     *                   middle C.
     * @param duration   the desired duration in milliseconds.
     * @param instrument the desired instrument (or patch) represented
     *                   by a positive byte value (0-127).  See the <a href=
     *                   http://www.midi.org/about-midi/gm/gm1sound.shtml#instrument>general
     *                   MIDI instrument patch map</a> for more instruments associated with
     *                   each value.
     * @param volume     the desired volume of the initial attack of the
     *                   Tone (MIDI velocity) represented by a positive byte value (0-127).
     */
    public void generateTone(byte pitch, int duration,
                             byte instrument, byte volume) {
        Runnable tone = new Tone(pitch, duration, instrument, volume);
        threadPool.execute(tone);
    }

    /**
     * Produces a Tone with the specified pitch, duration, and instrument,
     * and volume, waiting for it to finish playing.
     *
     * @param pitch      the desired pitch in semitones - 0-127 where 60 is
     *                   middle C.
     * @param duration   the desired duration in milliseconds.
     * @param instrument the desired instrument (or patch) represented
     *                   by a positive byte value (0-127).  See the <a href=
     *                   http://www.midi.org/about-midi/gm/gm1sound.shtml#instrument>general
     *                   MIDI instrument patch map</a> for more instruments associated with
     *                   each value.
     * @param volume     the desired volume of the initial attack of the
     *                   Tone (MIDI velocity) represented by a positive byte value (0-127).
     */
    public void generateToneSynchronously(byte pitch, int duration,
                                          byte instrument, byte volume) {
        Runnable tone = new Tone(pitch, duration, instrument, volume);
        tone.run();
    }

}


/**
 * Contains important variables for a MIDI Tone: pitch, duration
 * instrument (patch), and volume.  The tone can be passed to a thread
 * and will be played using MIDI.
 */
class Tone implements Runnable {

    /**
     * Tempo of the tone is in milliseconds: 1000 beats per second.
     */

    public final static int TEMPO = 1000;
    /**
     * The default MIDI channel of the tone: 0 (channel 1).
     */
    public final static int DEFAULT_CHANNEL = 0;

    private byte pitch;
    private int duration;
    private byte instrument;
    private byte volume;

    /**
     * Instantiates a new Tone object, initializing the tone's pitch,
     * duration, instrument (patch), and volume.
     *
     * @param pitch      the pitch in semitones.  Pitch is represented by
     *                   a positive byte value - 0-127 where 60 is middle C.
     * @param duration   the duration of the tone in milliseconds.
     * @param instrument a positive byte value (0-127) which represents
     *                   the instrument (or patch) of the tone.  See the <a href=
     *                   http://www.midi.org/about-midi/gm/gm1sound.shtml#instrument>general
     *                   MIDI instrument patch map</a> for more instruments associated with
     *                   each value.
     * @param volume     a positive byte value (0-127) which represents the
     *                   volume of the initial attack of the note (MIDI velocity).  127 being
     *                   loud, and 0 being silent.
     */
    public Tone(byte pitch, int duration, byte instrument, byte volume) {
        this.pitch = pitch;
        this.duration = duration;
        this.instrument = instrument;
        this.volume = volume;
    }

    /**
     * Plays the tone.
     */
    public void run() {
        playTone();
    }
      
       /* The following lock and the code which locks and unlocks it
    * around the opening of the Sequencer were added 2009-10-19 by
	* Max Hailperin <max@gustavus.edu> in order to work around a
	* bug in Sun's JDK which causes crashing if two threads race:
	* http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6888117 .
	* This routinely manifested native-code crashes when tones
	* were played asynchronously, on dual-core machines with Sun's
	* JDK (but not on one core or with OpenJDK).  Even when tones
	* were played only synchronously, crashes sometimes occurred.
	* This is likely due to the fact that Thread.sleep was used
	* for synchronization, a role it cannot reliably serve.  In
	* any case, this one lock seems to make all the crashes go
	* away, and the sleeps are being eliminated (since they can
	* cause other, less severe, problems), so that case should be
	* double covered. */

    private static Lock openLock = new ReentrantLock();

    private void playTone() {

        try {
            Sequencer player = null;
            openLock.lock();
            try {
                player = MidiSystem.getSequencer();
                player.open();
            } finally {
                openLock.unlock();
            }

            Sequence seq = new Sequence(Sequence.PPQ, 1);
            player.setTempoInMPQ(TEMPO);
            Track t = seq.createTrack();

            //select instrument
            ShortMessage inst = new ShortMessage();
            inst.setMessage(ShortMessage.PROGRAM_CHANGE, DEFAULT_CHANNEL, instrument, 0);
            MidiEvent instChange = new MidiEvent(inst, 0);
            t.add(instChange);

            ShortMessage on = new ShortMessage();
            on.setMessage(ShortMessage.NOTE_ON, DEFAULT_CHANNEL, pitch, volume);
            MidiEvent noteOn = new MidiEvent(on, 0);
            t.add(noteOn);

            ShortMessage off = new ShortMessage();
            off.setMessage(ShortMessage.NOTE_OFF, DEFAULT_CHANNEL, pitch, volume);
            MidiEvent noteOff = new MidiEvent(off, duration);
            t.add(noteOff);

            player.setSequence(seq);

	    /* The EndOfTrackListener was added 2009-10-19 by Max
         * Hailperin <max@gustavus.edu> so that its
	     * awaitEndOfTrack method could be used as a more reliable
	     * replacement for Thread.sleep.  (Given that the tone
	     * might not start playing right away, the sleep could end
	     * before the tone, clipping off the end of the tone.) */
            EndOfTrackListener eot = new EndOfTrackListener();
            player.addMetaEventListener(eot);

            player.start();

            try {
                eot.awaitEndOfTrack();
            } catch (InterruptedException ex) {
            } finally {
                player.close();
            }

        } catch (MidiUnavailableException mue) {
            mue.printStackTrace();
        } catch (InvalidMidiDataException imde) {
            imde.printStackTrace();
        }
    }
}

class EndOfTrackListener implements javax.sound.midi.MetaEventListener {

    private boolean endedYet = false;

    public synchronized void meta(javax.sound.midi.MetaMessage m) {
        if (m.getType() == 47) {
            endedYet = true;
            notifyAll();
        }
    }

    public synchronized void awaitEndOfTrack() throws InterruptedException {
        while (!endedYet) {
            wait();
        }
    }
}
