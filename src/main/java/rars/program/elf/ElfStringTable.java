package rars.program.elf;

import java.io.IOException;

public final class ElfStringTable {

	/** The string table data. */
	private final byte data[];
	public final int numStrings;

	/** Reads all the strings from [offset, length]. */
	ElfStringTable(ElfParser parser, long offset, int length) throws ElfException, IOException {
		parser.seek(offset);
		data = new byte[length];
		int bytesRead = parser.read(data);
		if (bytesRead != length)
			throw new ElfException("Error reading string table (read " + bytesRead + "bytes - expected to " + "read " + data.length + "bytes)");

		int stringsCount = 0;
		for (int ptr = 0; ptr < data.length; ptr++)
			if (data[ptr] == '\0') stringsCount++;
		numStrings = stringsCount;
	}

	public String get(int index) {
		int startPtr = index;
		int endPtr = index;
		while (data[endPtr] != '\0')
			endPtr++;
		return new String(data, startPtr, endPtr - startPtr);
	}
}
