package rars.program.elf;

/*
 * 	Code from https://github.com/fornwall/jelf
 * 
 * 	Copyright (c) 2016-2017 Fredrik Fornwall.
 *
 *	Permission is hereby granted, free of charge, to any person obtaining 
 *	a copy of this software and associated documentation files (the "Software"), 
 *	to deal in the Software without restriction, including without limitation 
 *	the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 *	and/or sell copies of the Software, and to permit persons to whom the 
 *	Software is furnished to do so, subject to the following conditions:
 *
 *	The above copyright notice and this permission notice shall be included in 
 *	all copies or substantial portions of the Software.
 *
 *	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 *	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 *	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 *	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 *	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 *	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
 *	SOFTWARE.
 */

import java.io.IOException;

class ElfNote {
    private int nameSize;
    private int descSize;
    private int type;
    private String name;
    private String desc;
    private byte[] descBytes;
    ElfNote(ElfParser parser, long offset, int size) throws ElfException, IOException {
        parser.seek(offset);
        nameSize = parser.readInt();
        descSize = parser.readInt();
        type = parser.readInt();
        byte nameBytes[] = new byte[nameSize];
        descBytes = new byte[descSize];
        int bytesRead = parser.read(nameBytes);
        if (bytesRead != nameSize) {
            throw new ElfException("Error reading note (read " + bytesRead + "bytes - expected to " + "read " + nameSize + "bytes)");
        }
        while (bytesRead % 4 != 0) { // finish reading the padding to the nearest 4 bytes
            parser.readUnsignedByte();
            bytesRead += 1;
        }
        bytesRead = parser.read(descBytes);
        if (bytesRead != descSize) {
            throw new ElfException("Error reading note (read " + bytesRead + "bytes - expected to " + "read " + descSize + "bytes)");
        }
        while (bytesRead % 4 != 0) { // finish reading the padding to the nearest 4 bytes
            parser.readUnsignedByte();
            bytesRead += 1;
        }
        name = new String(nameBytes, 0, nameSize-1); // unnecessary trailing 0
        desc = new String(descBytes, 0, descSize); // There's no trailing 0 on desc
    }

    String getName() {
        return name;
    }

    int getType() {
        return type;
    }

    String getDesc() {
        return desc;
    }

    byte[] getDescBytes() {
        return descBytes;
    }
}
