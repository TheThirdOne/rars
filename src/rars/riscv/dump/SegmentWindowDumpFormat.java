package rars.riscv.dump;

import rars.Globals;
import rars.ProgramStatement;
import rars.Settings;
import rars.riscv.hardware.AddressErrorException;
import rars.riscv.hardware.Memory;
import rars.util.Binary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
/*
Copyright (c) 2003-2008,  Pete Sanderson and Kenneth Vollmar

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
 * Dump memory contents in Segment Window format.  Each line of
 * text output resembles the Text Segment Window or Data Segment Window
 * depending on which segment is selected for the dump.  Written
 * using PrintStream's println() method.  Each line of Text Segment
 * Window represents one word of text segment memory.  The line
 * includes (1) address, (2) machine code in hex, (3) basic instruction,
 * (4) source line.  Each line of Data Segment Window represents 8
 * words of data segment memory.  The line includes address of first
 * word for that line followed by 8 32-bit values.
 * <p>
 * In either case, addresses and values are displayed in decimal or
 * hexadecimal representation according to the corresponding settings.
 *
 * @author Pete Sanderson
 * @version January 2008
 */


public class SegmentWindowDumpFormat extends AbstractDumpFormat {

    /**
     * Constructor.  There is no standard file extension for this format.
     */
    public SegmentWindowDumpFormat() {
        super("Text/Data Segment Window", "SegmentWindow", " Text Segment Window or Data Segment Window format to text file", null);
    }


    /**
     * Write memory contents in Segment Window format.  Each line of
     * text output resembles the Text Segment Window or Data Segment Window
     * depending on which segment is selected for the dump.  Written
     * using PrintStream's println() method.
     *
     * @see AbstractDumpFormat
     */
    public void dumpMemoryRange(File file, int firstAddress, int lastAddress, Memory memory)
            throws AddressErrorException, IOException {

        PrintStream out = new PrintStream(new FileOutputStream(file));

        // TODO: check if these settings work right
        boolean hexAddresses = Globals.getSettings().getBooleanSetting(Settings.Bool.DISPLAY_ADDRESSES_IN_HEX);

        // If address in data segment, print in same format as Data Segment Window
        if (Memory.inDataSegment(firstAddress)) {
            boolean hexValues = Globals.getSettings().getBooleanSetting(Settings.Bool.DISPLAY_VALUES_IN_HEX);
            int offset = 0;
            String string = "";
            try {
                for (int address = firstAddress; address <= lastAddress; address += Memory.WORD_LENGTH_BYTES) {
                    if (offset % 8 == 0) {
                        string = ((hexAddresses) ? Binary.intToHexString(address) : Binary.unsignedIntToIntString(address)) + "    ";
                    }
                    offset++;
                    Integer temp = memory.getRawWordOrNull(address);
                    if (temp == null)
                        break;
                    string += ((hexValues)
                            ? Binary.intToHexString(temp)
                            : ("           " + temp).substring(temp.toString().length())
                    ) + " ";
                    if (offset % 8 == 0) {
                        out.println(string);
                        string = "";
                    }
                }
            } finally {
                out.close();
            }
            return;
        }

        if (!Memory.inTextSegment(firstAddress)) {
            return;
        }
        
        // If address in text segment, print in same format as Text Segment Window
        out.println("Address     Code        Basic                        Line Source");
        out.println();
        String string = null;
        try {
            for (int address = firstAddress; address <= lastAddress; address += Memory.WORD_LENGTH_BYTES) {
                string = ((hexAddresses) ? Binary.intToHexString(address) : Binary.unsignedIntToIntString(address)) + "  ";
                Integer temp = memory.getRawWordOrNull(address);
                if (temp == null)
                    break;
                string += Binary.intToHexString(temp) + "  ";
                try {
                    ProgramStatement ps = memory.getStatement(address);
                    string += (ps.getPrintableBasicAssemblyStatement() + "                             ").substring(0, 29);
                    string += (((ps.getSource() == "") ? "" : Integer.toString(ps.getSourceLine())) + "     ").substring(0, 5);
                    string += ps.getSource();
                } catch (AddressErrorException aee) {
                }
                out.println(string);
            }
        } finally {
            out.close();
        }
    }


}