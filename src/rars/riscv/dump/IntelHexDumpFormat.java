package rars.riscv.dump;

import rars.riscv.hardware.AddressErrorException;
import rars.riscv.hardware.Memory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Intel's Hex memory initialization format
 *
 * @author Leo Alterman
 * @version July 2011
 */

public class IntelHexDumpFormat extends AbstractDumpFormat {

    /**
     * Constructor.  File extension is "hex".
     */
    public IntelHexDumpFormat() {
        super("Intel hex format", "HEX", "Written as Intel Hex Memory File", "hex");
    }

    /**
     * Write memory contents according to the Memory Initialization File
     * (MIF) specification.
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
                string = Integer.toHexString(temp);
                while (string.length() < 8) {
                    string = '0' + string;
                }
                String addr = Integer.toHexString(address - firstAddress);
                while (addr.length() < 4) {
                    addr = '0' + addr;
                }
                String chksum;
                int tmp_chksum = 0;
                tmp_chksum += 4;
                tmp_chksum += 0xFF & (address - firstAddress);
                tmp_chksum += 0xFF & ((address - firstAddress) >> 8);
                tmp_chksum += 0xFF & temp;
                tmp_chksum += 0xFF & (temp >> 8);
                tmp_chksum += 0xFF & (temp >> 16);
                tmp_chksum += 0xFF & (temp >> 24);
                tmp_chksum = tmp_chksum % 256;
                tmp_chksum = ~tmp_chksum + 1;
                chksum = Integer.toHexString(0xFF & tmp_chksum);
                if (chksum.length() == 1) chksum = '0' + chksum;
                String finalstr = ":04" + addr + "00" + string + chksum;
                out.println(finalstr.toUpperCase());
            }
            out.println(":00000001FF");
        } finally {
            out.close();
        }

    }
}
