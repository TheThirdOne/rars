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
import java.util.ArrayList;
import java.util.List;

/**
 * http://www.sco.com/developers/gabi/latest/ch5.dynamic.html#dynamic_section
 * 
 * "If an object file participates in dynamic linking, its program header table will have an element of type PT_DYNAMIC. This ``segment'' contains the .dynamic
 * section. A special symbol, _DYNAMIC, labels the section, which contains an array of the following structures."
 * 
 * <pre>
 * typedef struct { Elf32_Sword d_tag; union { Elf32_Word d_val; Elf32_Addr d_ptr; } d_un; } Elf32_Dyn;
 * extern Elf32_Dyn _DYNAMIC[];
 * 
 * typedef struct { Elf64_Sxword d_tag; union { Elf64_Xword d_val; Elf64_Addr d_ptr; } d_un; } Elf64_Dyn;
 * extern Elf64_Dyn _DYNAMIC[];
 * </pre>
 * 
 * <pre>
 * http://www.sco.com/developers/gabi/latest/ch5.dynamic.html:
 * 
 * Name	        		Value		d_un		Executable	Shared Object
 * ----------------------------------------------------------------------
 * DT_NULL	    		0			ignored		mandatory	mandatory
 * DT_NEEDED			1			d_val		optional	optional
 * DT_PLTRELSZ			2			d_val		optional	optional
 * DT_PLTGOT			3			d_ptr		optional	optional
 * DT_HASH				4			d_ptr		mandatory	mandatory
 * DT_STRTAB			5			d_ptr		mandatory	mandatory
 * DT_SYMTAB			6			d_ptr		mandatory	mandatory
 * DT_RELA				7			d_ptr		mandatory	optional
 * DT_RELASZ			8			d_val		mandatory	optional
 * DT_RELAENT			9			d_val		mandatory	optional
 * DT_STRSZ				10			d_val		mandatory	mandatory
 * DT_SYMENT			11			d_val		mandatory	mandatory
 * DT_INIT  			12			d_ptr		optional	optional
 * DT_FINI	    		13			d_ptr		optional	optional
 * DT_SONAME			14			d_val		ignored		optional
 * DT_RPATH*			15			d_val		optional	ignored
 * DT_SYMBOLIC*			16			ignored		ignored		optional
 * DT_REL	    		17			d_ptr		mandatory	optional
 * DT_RELSZ	    		18			d_val		mandatory	optional
 * DT_RELENT			19			d_val		mandatory	optional
 * DT_PLTREL			20			d_val		optional	optional
 * DT_DEBUG	    		21			d_ptr		optional	ignored
 * DT_TEXTREL*			22			ignored		optional	optional
 * DT_JMPREL			23			d_ptr		optional	optional
 * DT_BIND_NOW*			24			ignored		optional	optional
 * DT_INIT_ARRAY		25			d_ptr		optional	optional
 * DT_FINI_ARRAY		26			d_ptr		optional	optional
 * DT_INIT_ARRAYSZ		27			d_val		optional	optional
 * DT_FINI_ARRAYSZ		28			d_val		optional	optional
 * DT_RUNPATH			29			d_val		optional	optional
 * DT_FLAGS				30			d_val		optional	optional
 * DT_ENCODING			32			unspecified	unspecified	unspecified
 * DT_PREINIT_ARRAY		32			d_ptr		optional	ignored
 * DT_PREINIT_ARRAYSZ	33			d_val		optional	ignored
 * DT_LOOS				0x6000000D	unspecified	unspecified	unspecified
 * DT_HIOS				0x6ffff000	unspecified	unspecified	unspecified
 * DT_LOPROC			0x70000000	unspecified	unspecified	unspecified
 * DT_HIPROC			0x7fffffff	unspecified	unspecified	unspecified
 * </pre>
 */
public class ElfDynamicStructure {

	public static final int DT_NULL = 0;
	public static final int DT_NEEDED = 1;
	public static final int DT_PLTRELSZ = 2;
	public static final int DT_PLTGOT = 3;
	public static final int DT_HASH = 4;
	/** DT_STRTAB entry holds the address, not offset, of the dynamic string table. */
	public static final int DT_STRTAB = 5;
	public static final int DT_SYMTAB = 6;
	public static final int DT_RELA = 7;
	public static final int DT_RELASZ = 8;
	public static final int DT_RELAENT = 9;
	/** The size in bytes of the {@link #DT_STRTAB} string table. */
	public static final int DT_STRSZ = 10;
	public static final int DT_SYMENT = 11;
	public static final int DT_INIT = 12;
	public static final int DT_FINI = 13;
	public static final int DT_SONAME = 14;
	public static final int DT_RPATH = 15;
	public static final int DT_RUNPATH = 29;
	public static final int DT_FLAGS_1 = 0x6ffffffb;
	public static final int DT_VERDEF = 0x6ffffffc; /* Address of version definition */
	public static final int DT_VERDEFNUM = 0x6ffffffd; /* Number of version definitions */
	public static final int DT_VERNEEDED = 0x6ffffffe;
	public static final int DT_VERNEEDNUM = 0x6fffffff;

	/** Some values of {@link #DT_FLAGS_1}. */
	public static final int DF_1_NOW = 0x00000001; /* Set RTLD_NOW for this object. */
	public static final int DF_1_GLOBAL = 0x00000002; /* Set RTLD_GLOBAL for this object. */
	public static final int DF_1_GROUP = 0x00000004; /* Set RTLD_GROUP for this object. */
	public static final int DF_1_NODELETE = 0x00000008; /* Set RTLD_NODELETE for this object. */

	/** For the {@link #DT_STRTAB}. Mandatory. */
	public long dt_strtab_offset;
	/** For the {@link #DT_STRSZ}. Mandatory. */
	public int dt_strtab_size;

	private MemoizedObject<ElfStringTable> dtStringTable;
	private final int[] dtNeeded;
	public final List<ElfDynamicSectionEntry> entries = new ArrayList<>();

	public static class ElfDynamicSectionEntry {
		public ElfDynamicSectionEntry(long d_tag, long d_val_or_ptr) {
			this.d_tag = d_tag;
			this.d_val_or_ptr = d_val_or_ptr;
		}

		public long d_tag;
		public long d_val_or_ptr;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (d_tag ^ (d_tag >>> 32));
			result = prime * result + (int) (d_val_or_ptr ^ (d_val_or_ptr >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			ElfDynamicSectionEntry other = (ElfDynamicSectionEntry) obj;
			if (d_tag != other.d_tag) return false;
			if (d_val_or_ptr != other.d_val_or_ptr) return false;
			return true;
		}

		@Override
		public String toString() {
			return "ElfDynamicSectionEntry[" + d_tag + ", " + d_val_or_ptr + "]";
		}
	}

	public ElfDynamicStructure(final ElfParser parser, long offset, int size) {
		parser.seek(offset);
		int numEntries = size / 8;

		List<Integer> dtNeededList = new ArrayList<>();
		// Except for the DT_NULL element at the end of the array, and the relative order of DT_NEEDED elements, entries
		// may appear in any order. So important to use lazy evaluation to only evaluating e.g. DT_STRTAB after the
		// necessary DT_STRSZ is read.
		loop: for (int i = 0; i < numEntries; i++) {
			long d_tag = parser.readIntOrLong();
			final long d_val_or_ptr = parser.readIntOrLong();
			entries.add(new ElfDynamicSectionEntry(d_tag, d_val_or_ptr));
			switch ((int) d_tag) {
			case DT_NULL:
				// A DT_NULL element ends the array (may be following DT_NULL values, but no need to look at them).
				break loop;
			case DT_NEEDED:
				dtNeededList.add((int) d_val_or_ptr);
				break;
			case DT_STRTAB: {
				dtStringTable = new MemoizedObject<ElfStringTable>() {
					@Override
					protected ElfStringTable computeValue() throws ElfException, IOException {
						long fileOffsetForStringTable = parser.virtualMemoryAddrToFileOffset(d_val_or_ptr);
						return new ElfStringTable(parser, fileOffsetForStringTable, dt_strtab_size);
					}
				};
				dt_strtab_offset = d_val_or_ptr;
			}
				break;
			case DT_STRSZ:
				if (d_val_or_ptr > Integer.MAX_VALUE) throw new ElfException("Too large DT_STRSZ: " + d_val_or_ptr);
				dt_strtab_size = (int) d_val_or_ptr;
				break;
			}
		}

		dtNeeded = new int[dtNeededList.size()];
		for (int i = 0, len = dtNeeded.length; i < len; i++)
			dtNeeded[i] = dtNeededList.get(i);
	}

	public List<String> getNeededLibraries() throws ElfException, IOException {
		List<String> result = new ArrayList<>();
		ElfStringTable stringTable = dtStringTable.getValue();
		for (int i = 0; i < dtNeeded.length; i++)
			result.add(stringTable.get(dtNeeded[i]));
		return result;
	}

	@Override
	public String toString() {
		return "ElfDynamicStructure[]";
	}
}
