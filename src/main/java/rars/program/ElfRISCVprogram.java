package rars.program;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import rars.AssemblyException;
import rars.ErrorList;
import rars.ErrorMessage;

/**
 * Internal structure of a program based on an ELF file
 */
public class ElfRISCVprogram extends RISCVprogram {
	@Override
	protected ErrorList assembleHelper(ArrayList<? extends RISCVprogram> programsToAssemble, boolean warningsAreErrors)
			throws AssemblyException {
		ErrorList err = new ErrorList();
		
		if(programsToAssemble.size() < 1) {
			ErrorMessage errMsg = new ErrorMessage(null, "No source file");
			err.add(errMsg);
			return err;
		}
		else if(programsToAssemble.size() > 1) {
			ErrorMessage errMsg = new ErrorMessage(null, "Too many source files. Only one source executable is allowed");
			err.add(errMsg);
			return err;
		}
		
		RISCVprogram p = programsToAssemble.get(0);
		ElfRISCVprogram program;
		if(!(p instanceof ElfRISCVprogram)) {
			ErrorMessage errMsg = new ErrorMessage(p, "Program instance is not compatible with ELF format");
			err.add(errMsg);
			return err;
		}
		
		/*
		 * 	TODO: Implement ELF parsing and loading
		 */
		
		return err;
	}

	/**
	 * 	Returns the source file as a list of hex strings and their
	 * 	corresponding ASCII interpretation
	 */
	@Override
	public ArrayList<String> readSourceHelper() throws AssemblyException {
        ArrayList<String> sourceList = new ArrayList<>();
        ErrorList errors;
        
        // Generate header
        final int BYTES_PER_LINE = 16;
        final int ADDR_SIZE = 4;	// 32-bit address
        final String SEPERATOR = "  ";
        
        String header = "Address";
        while(header.length() < ADDR_SIZE * 2) header += " ";
        header += SEPERATOR;
        
        int tmp_length = header.length();
        header += "Raw Data";
        while(header.length() - tmp_length <  BYTES_PER_LINE * 2 + BYTES_PER_LINE - 1) header += " ";
        header += SEPERATOR;
        header += "Char Data";
        
        sourceList.add(header);
        
        // Read raw data
        try {
            InputStream is = new FileInputStream(super.getFilename());
            
            byte[] line = new byte[BYTES_PER_LINE];
            
            int addr = 0;
            int count = is.read(line);
            while (count > 0) {
            	String tmp_line = String.format("%08x", addr) + SEPERATOR;
            	
            	String tmp_char = "";
            	for(int i = 0; i < BYTES_PER_LINE; i++) {
            		// Convert bytes to hex
            		if(i < count) {
            			tmp_line += String.format("%02x", line[i]);
            			
            			// Only print the byte in character form if it is printable ASCII
            			if(!Character.isISOControl(line[i]) && ((line[i] & 0x80) == 0))
            				tmp_char += (char)line[i];
            			else
            				tmp_char += '.';
            		}
            		else {
            			tmp_line += "  ";
            		}
            		
            		// Add a space between bytes
            		if(i != BYTES_PER_LINE - 1)
            			tmp_line += " ";
            	}
            	tmp_line += SEPERATOR;
            	tmp_line += tmp_char;
            	sourceList.add(tmp_line);
            	
            	addr += BYTES_PER_LINE;
            	count = is.read(line);
            }
            
            is.close();
        } catch (Exception e) {
            errors = new ErrorList();
            errors.add(new ErrorMessage(this, "Could not read from source file, " + e.getMessage()));
            throw new AssemblyException(errors);
        }
        
        return sourceList;
	}

}
