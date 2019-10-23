package rars.riscv.hardware;

/*
Copyright (c) 2003-2009,  Pete Sanderson and Kenneth Vollmar

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
 * Models the memory configuration for the simulated MIPS machine.
 * "configuration" refers to the starting memory addresses for
 * the various memory segments.
 * The default configuration is based on SPIM.  Starting with MARS 3.7,
 * the configuration can be changed.
 *
 * @author Pete Sanderson
 * @version August 2009
 */


public class MemoryConfiguration {
    // TODO: remove kernel mode maybe?
    // TODO: move away from a multi-array approach to array of ranges approach
    // Identifier is used for saving setting; name is used for display
    private String configurationIdentifier, configurationName;
    private String[] configurationItemNames;
    private int[] configurationItemValues;


    public MemoryConfiguration(String ident, String name, String[] items, int[] values) {
        this.configurationIdentifier = ident;
        this.configurationName = name;
        this.configurationItemNames = items;
        this.configurationItemValues = values;
    }

    public String getConfigurationIdentifier() {
        return configurationIdentifier;
    }

    public String getConfigurationName() {
        return configurationName;
    }

    public int[] getConfigurationItemValues() {
        return configurationItemValues;
    }

    public String[] getConfigurationItemNames() {
        return configurationItemNames;
    }

    public int getTextBaseAddress() {
        return configurationItemValues[0];
    }

    public int getDataSegmentBaseAddress() {
        return configurationItemValues[1];
    }

    public int getExternBaseAddress() {
        return configurationItemValues[2];
    }

    public int getGlobalPointer() {
        return configurationItemValues[3];
    }

    public int getDataBaseAddress() {
        return configurationItemValues[4];
    }

    public int getHeapBaseAddress() {
        return configurationItemValues[5];
    }

    public int getStackPointer() {
        return configurationItemValues[6];
    }

    public int getStackBaseAddress() {
        return configurationItemValues[7];
    }

    public int getUserHighAddress() {
        return configurationItemValues[8];
    }

    public int getKernelBaseAddress() {
        return configurationItemValues[9];
    }

    public int getMemoryMapBaseAddress() {
        return configurationItemValues[10];
    }

    public int getKernelHighAddress() {
        return configurationItemValues[11];
    }

    public int getDataSegmentLimitAddress() {
        return configurationItemValues[12];
    }

    public int getTextLimitAddress() {
        return configurationItemValues[13];
    }

    public int getStackLimitAddress() {
        return configurationItemValues[14];
    }

    public int getMemoryMapLimitAddress() {
        return configurationItemValues[15];
    }

}