package rars.program;

import java.util.ArrayList;

import rars.AssemblyException;

public class ElfParser {
	private ArrayList<Byte> rawData;
	
	public ElfParser(ArrayList<Byte> data) throws AssemblyException {
		this.rawData = data;
	}
}
