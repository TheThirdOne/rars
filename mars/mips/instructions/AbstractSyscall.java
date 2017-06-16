package mars.mips.instructions;

import mars.ProcessingException;
import mars.ProgramStatement;
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
 * Abstract class that a MIPS syscall system service may extend.  A qualifying service
 * must be a class in the mars.mips.instructions.syscalls package that
 * implements the Syscall interface, must be compiled into a .class file,
 * and its .class file must be in the same folder as Syscall.class.
 * Mars will detect a qualifying syscall upon startup, create an instance
 * using its no-argument constructor and add it to its syscall list.
 * When its service is invoked at runtime ("syscall" instruction
 * with its service number stored in register $v0), its simulate()
 * method will be invoked.
 */

public abstract class AbstractSyscall implements Syscall {
    private int serviceNumber;
    private String serviceName;

    /**
     * Constructor is provided so subclass may initialize instance variables.
     *
     * @param number default assigned service number
     * @param name   service name which may be used for reference independent of number
     */
    protected AbstractSyscall(int number, String name) {
        serviceNumber = number;
        serviceName = name;
    }

    /**
     * Return the name you have chosen for this syscall.  This can be used by a MARS
     * user to refer to the service when choosing to override its default service
     * number in the configuration file.
     *
     * @return service name as a string
     */
    public String getName() {
        return serviceName;
    }

    /**
     * Set the service number.  This is provided to allow MARS implementer or user
     * to override the default service number.
     *
     * @param num specified service number to override the default.
     */
    public void setNumber(int num) {
        serviceNumber = num;
    }

    /**
     * Return the assigned service number.  This is the number the MIPS programmer
     * must store into a7 before issuing the SYSCALL instruction.
     *
     * @return assigned service number
     */
    public int getNumber() {
        return serviceNumber;
    }

    /**
     * Performs syscall function.  It will be invoked when the service is invoked
     * at simulation time.  Service is identified by value stored in a7.
     *
     * @param statement ProgramStatement object for this syscall instruction.
     */
    public abstract void simulate(ProgramStatement statement)
            throws ProcessingException;
}