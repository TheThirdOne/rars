package mars.mips.dump;

import mars.mips.hardware.AddressErrorException;

import java.io.File;
import java.io.IOException;
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
 * The Memory Initialization File (.mif) VHDL-supported file format
 * This is documented for the Altera platform at
 * www.altera.com/support/software/nativelink/quartus2/glossary/def_mif.html.
 *
 * @author Pete Sanderson
 * @version December 2007
 */

// NOT READY FOR PRIME TIME.  WHEN IT IS, UNCOMMENT THE "extends" CLAUSE
// AND THE SUPERCLASS CONSTRUCTOR CALL SO THE FORMAT LOADER WILL ACCEPT IT 
// AND IT WILL BE ADDED TO THE LIST.
public class MIFDumpFormat { //extends AbstractDumpFormat {

    /**
     * Constructor.  File extention is "mif".
     */
    public MIFDumpFormat() {
        //   super("MIF", "MIF", "Written as Memory Initialization File (Altera)", "mif");
    }

    /**
     * Write MIPS memory contents according to the Memory Initialization File
     * (MIF) specification.
     *
     * @param file         File in which to store MIPS memory contents.
     * @param firstAddress first (lowest) memory address to dump.  In bytes but
     *                     must be on word boundary.
     * @param lastAddress  last (highest) memory address to dump.  In bytes but
     *                     must be on word boundary.  Will dump the word that starts at this address.
     * @throws AddressErrorException if firstAddress is invalid or not on a word boundary.
     * @throws IOException           if error occurs during file output.
     */
    public void dumpMemoryRange(File file, int firstAddress, int lastAddress)
            throws AddressErrorException, IOException {

    }
}