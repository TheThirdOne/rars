package rars.riscv.dump;

import rars.riscv.hardware.AddressErrorException;
import rars.riscv.hardware.Memory;

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
 * Abstract class for memory dump file formats.  Provides constructors and
 * defaults for everything except the dumpMemoryRange method itself.
 *
 * @author Pete Sanderson
 * @version December 2007
 */


public abstract class AbstractDumpFormat implements DumpFormat {

    private final String name, commandDescriptor, description, extension;

    /**
     * Typical constructor.  Note you cannot creates objects from this
     * class but subclass constructor can call this one.
     *
     * @param name              Brief descriptive name to be displayed in selection list.
     * @param commandDescriptor One-word descriptive name to be used by RARS command mode parser and user.
     *                          Any spaces in this string will be removed.
     * @param description       Description to go with standard file extension for
     *                          display in file save dialog or to be used as tool tip.
     * @param extension         Standard file extension for this format.  Null if none.
     */
    public AbstractDumpFormat(String name, String commandDescriptor,
                              String description, String extension) {
        this.name = name;
        this.commandDescriptor = (commandDescriptor == null) ? null : commandDescriptor.replaceAll(" ", "");
        this.description = description;
        this.extension = extension;
    }


    /**
     * Get the file extension associated with this format.
     *
     * @return String containing file extension -- without the leading "." -- or
     * null if there is no standard extension.
     */
    public String getFileExtension() {
        return extension;
    }

    /**
     * Get a short description of the format, suitable for displaying along with
     * the extension, in the file save dialog, or as a tool tip.
     *
     * @return String containing short description to go with the extension
     * or for use as tool tip.  Possibly null.
     */
    public String getDescription() {
        return description;
    }

    /**
     * String representing this object.
     *
     * @return Name given for this object.
     */
    public String toString() {
        return name;
    }

    /**
     * One-word description of format to be used by RARS command mode parser
     * and user in conjunction with the "dump" option.
     *
     * @return One-word String describing the format.
     */
    public String getCommandDescriptor() {
        return commandDescriptor;
    }

    /**
     * Write memory contents according to the
     * specification for this format.
     *
     * @param file         File in which to store memory contents.
     * @param firstAddress first (lowest) memory address to dump.  In bytes but
     *                     must be on word boundary.
     * @param lastAddress  last (highest) memory address to dump.  In bytes but
     *                     must be on word boundary.  Will dump the word that starts at this address.
     * @param memory
     * @throws AddressErrorException if firstAddress is invalid or not on a word boundary.
     * @throws IOException           if error occurs during file output.
     */
    public abstract void dumpMemoryRange(File file, int firstAddress, int lastAddress, Memory memory)
            throws AddressErrorException, IOException;

}