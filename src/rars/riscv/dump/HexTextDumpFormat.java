package rars.riscv.dump;

import rars.riscv.hardware.AddressErrorException;
import rars.riscv.hardware.Memory;

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
 * Class that represents the "hexadecimal text" memory dump format.  The output
 * is a text file with one word of memory per line.  The word is formatted
 * using hexadecimal characters, e.g. 3F205A39.
 *
 * @author Pete Sanderson
 * @version December 2007
 */


public class HexTextDumpFormat extends AbstractDumpFormat {

    /**
     * Constructor.  There is no standard file extension for this format.
     */
    public HexTextDumpFormat() {
        super("Hexadecimal Text", "HexText", "Written as hex characters to text file", null);
    }


    /**
     * Write memory contents in hexadecimal text format.  Each line of
     * text contains one memory word written in hexadecimal characters.  Written
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
                string = Integer.toHexString(temp);
                while (string.length() < 8) {
                    string = '0' + string;
                }
                out.println(string);
            }
        } finally {
            out.close();
        }
    }

}