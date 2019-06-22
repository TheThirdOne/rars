package rars.riscv;

import rars.ExitingException;
import rars.ProgramStatement;
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
 * Abstract class that a syscall system service must extend.  A qualifying service
 * must be a class in the rars.riscv.syscalls package, must be compiled into a .class file.
 * Rars will detect a qualifying syscall upon startup, create an instance
 * using its no-argument constructor and add it to its syscall list.
 * When its service is invoked at runtime ("ecall" instruction
 * with its service number stored in register a7), its simulate()
 * method will be invoked.
 */

public abstract class AbstractSyscall implements Comparable<AbstractSyscall> {
    private int serviceNumber;
    private String serviceName;
    private String description, inputs, outputs;

    /**
     * Constructor is provided so subclass may initialize instance variables.
     *
     * @param name service name which may be used for reference independent of number
     */
    protected AbstractSyscall(String name) {
        this(name, "N/A");
    }

    /**
     * @param name service name which may be used for reference independent of number
     * @param descr a hort description of what the system calll does
     */
    protected AbstractSyscall(String name, String descr) {
        this(name, descr, "N/A", "N/A");
    }


    /**
     * @param name service name which may be used for reference independent of number
     * @param descr a short description of what the system call does
     * @param in    a description of what registers should be set to before the system call
     * @param out   a description of what registers are set to after the system call
     */
    protected AbstractSyscall(String name, String descr, String in, String out) {
        serviceNumber = -1;
        serviceName = name;
        description = descr;
        inputs = in;
        outputs = out;
    }

    /**
     * Return the name you have chosen for this syscall.  This can be used by a RARS
     * user to refer to the service when choosing to override its default service
     * number in the configuration file.
     *
     * @return service name as a string
     */
    public String getName() {
        return serviceName;
    }

    /**
     * @return a string describing what the system call does
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return a string documenting what registers should be set to before the system call runs
     */
    public String getInputs() {
        return inputs;
    }

    /**
     * @return a string documenting what registers are set to after the system call runs
     */
    public String getOutputs() {
        return outputs;
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
     * Return the assigned service number.  This is the number the programmer
     * must store into a7 before issuing the ECALL instruction.
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
            throws ExitingException;

    public int compareTo(AbstractSyscall other) {
        if (this == other) return 0;
        assert getNumber() != other.getNumber() : "Different syscalls have to have different numbers";
        return getNumber() > other.getNumber() ? 1 : -1;
    }

}