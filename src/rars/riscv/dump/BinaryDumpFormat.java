package rars.riscv.dump;

import rars.riscv.hardware.AddressErrorException;
import rars.riscv.hardware.Memory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Class that represents the "binary" memory dump format.  The output
 * is a binary file containing the memory words as a byte stream.  Output
 * is produced using PrintStream's write() method.
 *
 * @author Pete Sanderson
 * @version December 2007
 */


public class BinaryDumpFormat extends AbstractDumpFormat {

    /**
     * Constructor.  There is no standard file extension for this format.
     */
    public BinaryDumpFormat() {
        super("Binary", "Binary", "Written as byte stream to binary file", null);
    }


    /**
     * Write memory contents in pure binary format.  One byte at a time
     * using PrintStream's write() method.  Adapted by Pete Sanderson from
     * code written by Greg Gibeling.
     *
     * @see AbstractDumpFormat
     */
    public void dumpMemoryRange(File file, int firstAddress, int lastAddress, Memory memory)
            throws AddressErrorException, IOException {
        PrintStream out = new PrintStream(new FileOutputStream(file));
        try {
            for (int address = firstAddress; address <= lastAddress; address += Memory.WORD_LENGTH_BYTES) {
                Integer temp = memory.getRawWordOrNull(address);
                if (temp == null)
                    break;
                int word = temp;
                for (int i = 0; i < 4; i++)
                    out.write((word >>> (i << 3)) & 0xFF);
            }
        } finally {
            out.close();
        }
    }

}