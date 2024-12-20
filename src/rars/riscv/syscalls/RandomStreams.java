package rars.riscv.syscalls;

import rars.riscv.hardware.RegisterFile;

import java.util.HashMap;
import java.util.Random;

/**
 * This small class serves only to hold a static HashMap for storing
 * random number generators for use by all the random number generator
 * syscalls.
 */

public class RandomStreams {
    /**
     * Collection of pseudorandom number streams available for use in Rand-type syscalls.
     * The streams are by default not seeded.
     */
    static final HashMap<Integer, Random> randomStreams = new HashMap<>();

    /**
     * Just a little helper method to initialize streams on stream being empty
     *
     * @param reg The name of the register that holds the stream index
     * @return the stream a that index
     */
    static Random get(String reg) {
        int index = RegisterFile.getValue(reg);
        Random stream = randomStreams.get(index);
        if (stream == null) {
            stream = new Random(); // create a non-seeded stream
            RandomStreams.randomStreams.put(index, stream);
        }
        return stream;
    }
}