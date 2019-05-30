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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;

/**
 * An ELF (Executable and Linkable Format) file can be a relocatable, executable, shared or core file.
 * 
 * <pre>
 * http://man7.org/linux/man-pages/man5/elf.5.html
 * http://en.wikipedia.org/wiki/Executable_and_Linkable_Format
 * http://www.ibm.com/developerworks/library/l-dynamic-libraries/
 * http://downloads.openwatcom.org/ftp/devel/docs/elf-64-gen.pdf
 * 
 * Elf64_Addr, Elf64_Off, Elf64_Xword, Elf64_Sxword: 8 bytes
 * Elf64_Word, Elf64_Sword: 4 bytes
 * Elf64_Half: 2 bytes
 * </pre>
 */
public final class ElfFile {

	/** Relocatable file type. A possible value of {@link #file_type}. */
	public static final int FT_REL = 1;
	/** Executable file type. A possible value of {@link #file_type}. */
	public static final int FT_EXEC = 2;
	/** Shared object file type. A possible value of {@link #file_type}. */
	public static final int FT_DYN = 3;
	/** Core file file type. A possible value of {@link #file_type}. */
	public static final int FT_CORE = 4;

	/** 32-bit objects. */
	public static final byte CLASS_32 = 1;
	/** 64-bit objects. */
	public static final byte CLASS_64 = 2;

	/** LSB data encoding. */
	public static final byte DATA_LSB = 1;
	/** MSB data encoding. */
	public static final byte DATA_MSB = 2;

	/** No architecture type. */
	public static final int ARCH_NONE = 0;
	/** AT&amp;T architecture type. */
	public static final int ARCH_ATT = 1;
	/** SPARC architecture type. */
	public static final int ARCH_SPARC = 2;
	/** Intel 386 architecture type. */
	public static final int ARCH_i386 = 3;
	/** Motorola 68000 architecture type. */
	public static final int ARCH_68k = 4;
	/** Motorola 88000 architecture type. */
	public static final int ARCH_88k = 5;
	/** Intel 860 architecture type. */
	public static final int ARCH_i860 = 7;
	/** MIPS architecture type. */
	public static final int ARCH_MIPS = 8;
	public static final int ARCH_ARM = 0x28;
	public static final int ARCH_X86_64 = 0x3E;
	public static final int ARCH_AARCH64 = 0xB7;

	/** Byte identifying the size of objects, either {@link #CLASS_32} or {link {@value #CLASS_64} . */
	public final byte objectSize;

	/**
	 * Returns a byte identifying the data encoding of the processor specific data. This byte will be either
	 * DATA_INVALID, DATA_LSB or DATA_MSB.
	 */
	public final byte encoding;

	/** Identifies the object file type. One of the FT_* constants in the class. */
	public final short file_type; // Elf32_Half
	/** The required architecture. One of the ARCH_* constants in the class. */
	public final short arch; // Elf32_Half
	/** Version */
	public final int version; // Elf32_Word
	/**
	 * Virtual address to which the system first transfers control. If there is no entry point for the file the value is
	 * 0.
	 */
	public final long entry_point; // Elf32_Addr
	/** Program header table offset in bytes. If there is no program header table the value is 0. */
	public final long ph_offset; // Elf32_Off
	/** Section header table offset in bytes. If there is no section header table the value is 0. */
	public final long sh_offset; // Elf32_Off
	/** Processor specific flags. */
	public int flags; // Elf32_Word
	/** ELF header size in bytes. */
	public short eh_size; // Elf32_Half
	/** e_phentsize. Size of one entry in the file's program header table in bytes. All entries are the same size. */
	public final short ph_entry_size; // Elf32_Half
	/** e_phnum. Number of {@link ElfSegment} entries in the program header table, 0 if no entries. */
	public final short num_ph; // Elf32_Half
	/** Section header entry size in bytes. */
	public final short sh_entry_size; // Elf32_Half
	/** Number of entries in the section header table, 0 if no entries. */
	public final short num_sh; // Elf32_Half

	/**
	 * Elf{32,64}_Ehdr#e_shstrndx. Index into the section header table associated with the section name string table.
	 * SH_UNDEF if there is no section name string table.
	 */
	private short sh_string_ndx; // Elf32_Half

	/** MemoizedObject array of section headers associated with this ELF file. */
	private MemoizedObject<ElfSection>[] sectionHeaders;
	/** MemoizedObject array of program headers associated with this ELF file. */
	private MemoizedObject<ElfSegment>[] programHeaders;

	/** Used to cache symbol table lookup. */
	private ElfSection symbolTableSection;
	/** Used to cache dynamic symbol table lookup. */
	private ElfSection dynamicSymbolTableSection;

	private ElfSection dynamicLinkSection;

	/**
	 * Returns the section header at the specified index. The section header at index 0 is defined as being a undefined
	 * section.
	 */
	public ElfSection getSection(int index) throws ElfException, IOException {
		return sectionHeaders[index].getValue();
	}

	/** Returns the section header string table associated with this ELF file. */
	public ElfStringTable getSectionNameStringTable() throws ElfException, IOException {
		return getSection(sh_string_ndx).getStringTable();
	}

	/** Returns the string table associated with this ELF file. */
	public ElfStringTable getStringTable() throws ElfException, IOException {
		return findStringTableWithName(ElfSection.STRING_TABLE_NAME);
	}

	/**
	 * Returns the dynamic symbol table associated with this ELF file, or null if one does not exist.
	 */
	public ElfStringTable getDynamicStringTable() throws ElfException, IOException {
		return findStringTableWithName(ElfSection.DYNAMIC_STRING_TABLE_NAME);
	}

	private ElfStringTable findStringTableWithName(String tableName) throws ElfException, IOException {
		// Loop through the section header and look for a section
		// header with the name "tableName". We can ignore entry 0
		// since it is defined as being undefined.
		for (int i = 1; i < num_sh; i++) {
			ElfSection sh = getSection(i);
			if (tableName.equals(sh.getName())) return sh.getStringTable();
		}
		return null;
	}

	/** The {@link ElfSection#SHT_SYMTAB} section (of which there may be only one), if any. */
	public ElfSection getSymbolTableSection() throws ElfException, IOException {
		return (symbolTableSection != null) ? symbolTableSection : (symbolTableSection = getSymbolTableSection(ElfSection.SHT_SYMTAB));
	}

	/** The {@link ElfSection#SHT_DYNSYM} section (of which there may be only one), if any. */
	public ElfSection getDynamicSymbolTableSection() throws ElfException, IOException {
		return (dynamicSymbolTableSection != null) ? dynamicSymbolTableSection : (dynamicSymbolTableSection = getSymbolTableSection(ElfSection.SHT_DYNSYM));
	}

	/** The {@link ElfSection#SHT_DYNAMIC} section (of which there may be only one). Named ".dynamic". */
	public ElfSection getDynamicLinkSection() throws IOException {
		return (dynamicLinkSection != null) ? dynamicLinkSection : (dynamicLinkSection = getSymbolTableSection(ElfSection.SHT_DYNAMIC));
	}

	private ElfSection getSymbolTableSection(int type) throws ElfException, IOException {
		for (int i = 1; i < num_sh; i++) {
			ElfSection sh = getSection(i);
			if (sh.type == type) return sh;
		}
		return null;
	}

	/** Returns the elf symbol with the specified name or null if one is not found. */
	public ElfSymbol getELFSymbol(String symbolName) throws ElfException, IOException {
		if (symbolName == null) return null;

		// Check dynamic symbol table for symbol name.
		ElfSection sh = getDynamicSymbolTableSection();
		if (sh != null) {
			int numSymbols = sh.getNumberOfSymbols();
			for (int i = 0; i < Math.ceil(numSymbols / 2); i++) {
				ElfSymbol symbol = sh.getELFSymbol(i);
				if (symbolName.equals(symbol.getName())) {
					return symbol;
				} else if (symbolName.equals((symbol = sh.getELFSymbol(numSymbols - 1 - i)).getName())) {
					return symbol;
				}
			}
		}

		// Check symbol table for symbol name.
		sh = getSymbolTableSection();
		if (sh != null) {
			int numSymbols = sh.getNumberOfSymbols();
			for (int i = 0; i < Math.ceil(numSymbols / 2); i++) {
				ElfSymbol symbol = sh.getELFSymbol(i);
				if (symbolName.equals(symbol.getName())) {
					return symbol;
				} else if (symbolName.equals((symbol = sh.getELFSymbol(numSymbols - 1 - i)).getName())) {
					return symbol;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the elf symbol with the specified address or null if one is not found. 'address' is relative to base of
	 * shared object for .so's.
	 */
	public ElfSymbol getELFSymbol(long address) throws ElfException, IOException {
		// Check dynamic symbol table for address.
		ElfSymbol symbol = null;
		long value = 0L;

		ElfSection sh = getDynamicSymbolTableSection();
		if (sh != null) {
			int numSymbols = sh.getNumberOfSymbols();
			for (int i = 0; i < numSymbols; i++) {
				symbol = sh.getELFSymbol(i);
				value = symbol.value;
				if (address >= value && address < value + symbol.size) return symbol;
			}
		}

		// Check symbol table for symbol name.
		sh = getSymbolTableSection();
		if (sh != null) {
			int numSymbols = sh.getNumberOfSymbols();
			for (int i = 0; i < numSymbols; i++) {
				symbol = sh.getELFSymbol(i);
				value = symbol.value;
				if (address >= value && address < value + symbol.size) return symbol;
			}
		}
		return null;
	}

	public ElfSegment getProgramHeader(int index) throws IOException {
		return programHeaders[index].getValue();
	}

	public static ElfFile fromStream(InputStream in) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int totalRead = 0;
		byte[] buffer = new byte[8096];
		boolean firstRead = true;
		while (true) {
			int readNow = in.read(buffer, totalRead, buffer.length - totalRead);
			if (readNow == -1) {
				return fromBytes(baos.toByteArray());
			} else {
				if (firstRead) {
					// Abort early.
					if (readNow < 4) {
						throw new ElfException("Bad first read");
					} else {
						if (!(0x7f == buffer[0] && 'E' == buffer[1] && 'L' == buffer[2] && 'F' == buffer[3]))
							throw new ElfException("Bad magic number for file");
					}
					firstRead = false;
				}
				baos.write(buffer, 0, readNow);
			}
		}
	}

	public static ElfFile fromFile(File file) throws ElfException, IOException {
		byte[] buffer = new byte[(int) file.length()];
		try (FileInputStream in = new FileInputStream(file)) {
			int totalRead = 0;
			while (totalRead < buffer.length) {
				int readNow = in.read(buffer, totalRead, buffer.length - totalRead);
				if (readNow == -1) {
					throw new ElfException("Premature end of file");
				} else {
					totalRead += readNow;
				}
			}
		}
		return new ElfFile(new ByteArrayInputStream(buffer));
	}

	public static ElfFile fromBytes(byte[] buffer) throws ElfException, IOException {
		return new ElfFile(new ByteArrayInputStream(buffer));
	}
    public ElfFile(MappedByteBuffer buffer, long startPosition) throws ElfException, IOException {
        final ElfParser parser = new ElfParser(this, buffer, startPosition);

        //Parsing is a shitty thing to do in constructors.
        byte[] ident = new byte[16];
        int bytesRead = parser.read(ident);
        if (bytesRead != ident.length)
            throw new ElfException("Error reading elf header (read " + bytesRead + "bytes - expected to read " + ident.length + "bytes)");

        if (!(0x7f == ident[0] && 'E' == ident[1] && 'L' == ident[2] && 'F' == ident[3])) throw new ElfException("Bad magic number for file");

        objectSize = ident[4];
        if (!(objectSize == CLASS_32 || objectSize == CLASS_64)) throw new ElfException("Invalid object size class: " + objectSize);
        encoding = ident[5];
        if (!(encoding == DATA_LSB || encoding == DATA_MSB)) throw new ElfException("Invalid encoding: " + encoding);
        int elfVersion = ident[6];
        if (elfVersion != 1) throw new ElfException("Invalid elf version: " + elfVersion);
        // ident[7]; // EI_OSABI, target operating system ABI
        // ident[8]; // EI_ABIVERSION, ABI version. Linux kernel (after at least 2.6) has no definition of it.
        // ident[9-15] // EI_PAD, currently unused.

        file_type = parser.readShort();
        arch = parser.readShort();
        version = parser.readInt();
        entry_point = parser.readIntOrLong();
        ph_offset = parser.readIntOrLong();
        sh_offset = parser.readIntOrLong();
        flags = parser.readInt();
        eh_size = parser.readShort();
        ph_entry_size = parser.readShort();
        num_ph = parser.readShort();
        sh_entry_size = parser.readShort();
        num_sh = parser.readShort();
        if (num_sh == 0) {
            throw new ElfException("e_shnum is SHN_UNDEF(0), which is not supported yet"
                    + " (the actual number of section header table entries is contained in the sh_size field of the section header at index 0)");
        }
        sh_string_ndx = parser.readShort();
        if (sh_string_ndx == /* SHN_XINDEX= */0xffff) {
            throw new ElfException("e_shstrndx is SHN_XINDEX(0xffff), which is not supported yet"
                    + " (the actual index of the section name string table section is contained in the sh_link field of the section header at index 0)");
        }

        sectionHeaders = MemoizedObject.uncheckedArray(num_sh);
        for (int i = 0; i < num_sh; i++) {
            final long sectionHeaderOffset = sh_offset + (i * sh_entry_size);
            sectionHeaders[i] = new MemoizedObject<ElfSection>() {
                @Override
                public ElfSection computeValue() throws ElfException, IOException {
                    return new ElfSection(parser, sectionHeaderOffset);
                }
            };
        }

        programHeaders = MemoizedObject.uncheckedArray(num_ph);
        for (int i = 0; i < num_ph; i++) {
            final long programHeaderOffset = ph_offset + (i * ph_entry_size);
            programHeaders[i] = new MemoizedObject<ElfSegment>() {
                @Override
                public ElfSegment computeValue() throws IOException {
                    return new ElfSegment(parser, programHeaderOffset);
                }
            };
        }

    }
    

	public ElfFile(ByteArrayInputStream baos) throws ElfException, IOException {
		final ElfParser parser = new ElfParser(this, baos);

		byte[] ident = new byte[16];        
		int bytesRead = parser.read(ident);
		if (bytesRead != ident.length)
			throw new ElfException("Error reading elf header (read " + bytesRead + "bytes - expected to read " + ident.length + "bytes)");

		if (!(0x7f == ident[0] && 'E' == ident[1] && 'L' == ident[2] && 'F' == ident[3])) throw new ElfException("Bad magic number for file");

		objectSize = ident[4];
		if (!(objectSize == CLASS_32 || objectSize == CLASS_64)) throw new ElfException("Invalid object size class: " + objectSize);
		encoding = ident[5];
		if (!(encoding == DATA_LSB || encoding == DATA_MSB)) throw new ElfException("Invalid encoding: " + encoding);
		int elfVersion = ident[6];
		if (elfVersion != 1) throw new ElfException("Invalid elf version: " + elfVersion);
		// ident[7]; // EI_OSABI, target operating system ABI
		// ident[8]; // EI_ABIVERSION, ABI version. Linux kernel (after at least 2.6) has no definition of it.
		// ident[9-15] // EI_PAD, currently unused.

		file_type = parser.readShort();
		arch = parser.readShort();
		version = parser.readInt();
		entry_point = parser.readIntOrLong();
		ph_offset = parser.readIntOrLong();
		sh_offset = parser.readIntOrLong();
		flags = parser.readInt();
		eh_size = parser.readShort();
		ph_entry_size = parser.readShort();
		num_ph = parser.readShort();
		sh_entry_size = parser.readShort();
		num_sh = parser.readShort();
		if (num_sh == 0) {
			throw new ElfException("e_shnum is SHN_UNDEF(0), which is not supported yet"
					+ " (the actual number of section header table entries is contained in the sh_size field of the section header at index 0)");
		}
		sh_string_ndx = parser.readShort();
		if (sh_string_ndx == /* SHN_XINDEX= */0xffff) {
			throw new ElfException("e_shstrndx is SHN_XINDEX(0xffff), which is not supported yet"
					+ " (the actual index of the section name string table section is contained in the sh_link field of the section header at index 0)");
		}

		sectionHeaders = MemoizedObject.uncheckedArray(num_sh);
		for (int i = 0; i < num_sh; i++) {
			final long sectionHeaderOffset = sh_offset + (i * sh_entry_size);
			sectionHeaders[i] = new MemoizedObject<ElfSection>() {
				@Override
				public ElfSection computeValue() throws ElfException, IOException {
					return new ElfSection(parser, sectionHeaderOffset);
				}
			};
		}

		programHeaders = MemoizedObject.uncheckedArray(num_ph);
		for (int i = 0; i < num_ph; i++) {
			final long programHeaderOffset = ph_offset + (i * ph_entry_size);
			programHeaders[i] = new MemoizedObject<ElfSegment>() {
				@Override
				public ElfSegment computeValue() throws IOException {
					return new ElfSegment(parser, programHeaderOffset);
				}
			};
		}
	}

	/** The interpreter specified by the {@link ElfSegment#PT_INTERP} program header, if any. */
	public String getInterpreter() throws IOException {
		for (int i = 0; i < programHeaders.length; i++) {
			ElfSegment ph = programHeaders[i].getValue();
			if (ph.type == ElfSegment.PT_INTERP) return ph.getIntepreter();
		}
		return null;
	}

}
