package rars.program;

import java.util.ArrayList;

import rars.AssemblyException;
import rars.ErrorList;
import rars.AsmErrorMessage;

public class ElfRISCVprogram extends RISCVprogram {

	@Override
	protected ErrorList assembleHelper(ArrayList<? extends RISCVprogram> programsToAssemble, boolean warningsAreErrors)
			throws AssemblyException {

		if(programsToAssemble.size() != 1) {
			ErrorList err = new ErrorList();
			AsmErrorMessage errMsg = new AsmErrorMessage();
			err.add(mess);
		}
		
		return null;
	}

	@Override
	public void readSourceHelper() throws AssemblyException {
		// TODO Auto-generated method stub
		
	}

}
