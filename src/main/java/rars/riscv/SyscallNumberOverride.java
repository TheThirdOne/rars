package rars.riscv;

/*
Copyright (c) 2003-2006,  Pete Sanderson and Kenneth Vollmar

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
 * Represents User override of default syscall number assignment.
 * Such overrides are specified in the Syscall.properties file read when
 * RARS starts up.
 */

public class SyscallNumberOverride {
    private String serviceName;
    private int newServiceNumber;

    /**
     * Constructor is called with two strings: service name and desired
     * number.  Will throw an exception is number is malformed, but does
     * not check validity of the service name or number.
     *
     * @param serviceName a String containing syscall service mnemonic.
     * @param value       a String containing its reassigned syscall service number.
     *                    If this number is previously assigned to a different syscall which does not
     *                    also receive a new number, then an error for duplicate numbers will
     *                    be issued at RARS launch.
     */

    public SyscallNumberOverride(String serviceName, String value) {
        this.serviceName = serviceName;
        try {
            this.newServiceNumber = Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            System.out.println("Error processing Syscall number override: '" + value.trim() + "' is not a valid integer");
            System.exit(0);
        }
    }


    /**
     * Get the service name as a String.
     *
     * @return the service name
     */
    public String getName() {
        return serviceName;
    }

    /**
     * Get the new service number as an int.
     *
     * @return the service number
     */
    public int getNumber() {
        return newServiceNumber;
    }

}
 
   	
   	
