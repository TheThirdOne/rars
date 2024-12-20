package rars.riscv.dump;

import rars.riscv.hardware.AddressErrorException;
import rars.riscv.hardware.Memory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Class that represents the "binary text" memory dump format.  The output
 * is a text file with one word of memory per line.  The word is formatted
 * using '0' and '1' characters, e.g. 01110101110000011111110101010011.
 *
 * @author Pete Sanderson
 * @version December 2007
 */


public class BinaryTextDumpFormat extends AbstractDumpFormat {

    /**
     * Constructor.  There is no standard file extension for this format.
     */
    public BinaryTextDumpFormat() {
        super("Binary Text", "BinaryText", "Written as '0' and '1' characters to text file", null);
    }


    /**
     * Write memory contents in binary text format.  Each line of
     * text contains one memory word written as 32 '0' and '1' characters.  Written
     * using PrintStream's println() method.
     * Adapted by Pete Sanderson from code written by Greg Gibeling.
     *
     * @see AbstractDumpFormat
     */
    public void dumpMemoryRange(File file, int firstAddress, int lastAddress, Memory memory)
            throws AddressErrorException, IOException {
        PrintStream out = new PrintStream(new FileOutputStream(file));
        String string = null;
        try {
            for (int address = firstAddress; address <= lastAddress; address += Memory.WORD_LENGTH_BYTES) {
                Integer temp = memory.getRawWordOrNull(address);
                if (temp == null)
                    break;
                string = Integer.toBinaryString(temp);
                while (string.length() < 32) {
                    string = '0' + string;
                }
                out.println(string);
            }
        } finally {
            out.close();
        }
    }

}