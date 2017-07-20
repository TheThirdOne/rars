package mars.riscv;

import mars.Globals;
import mars.util.FilenameFinder;

import java.util.ArrayList;
import java.util.HashSet;

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


/****************************************************************************/
    /* This class provides functionality to bring external Syscall definitions
     * into MARS.  This permits anyone with knowledge of the Mars public interfaces, 
     * in particular of the Memory and Register classes, to write custom MIPS syscall
     * functions. This is adapted from the ToolLoader class, which is in turn adapted
     * from Bret Barker's GameServer class from the book "Developing Games In Java".
     */

public class SyscallLoader {

    private static final String CLASS_PREFIX = "mars.riscv.syscalls.";
    private static final String SYSCALLS_DIRECTORY_PATH = "mars/riscv/syscalls";
    private static final String CLASS_EXTENSION = "class";

    private static ArrayList<Syscall> syscallList;

    /*
       *  Dynamically loads Syscalls into an ArrayList.  This method is adapted from
       *  the loadGameControllers() method in Bret Barker's GameServer class.
       *  Barker (bret@hypefiend.com) is co-author of the book "Developing Games
       *  in Java".  Also see the "loadMarsTools()" method from ToolLoader class.
       */
    static {
        syscallList = new ArrayList<>();
        // grab all class files in the same directory as Syscall
        ArrayList<String> candidates = FilenameFinder.getFilenameList(SyscallLoader.class.getClassLoader(),
                SYSCALLS_DIRECTORY_PATH, CLASS_EXTENSION);
        HashSet<String> syscalls = new HashSet<>();
        for (String file : candidates) {
            // Do not add class if already encountered (happens if run in MARS development directory)
            if (syscalls.contains(file)) {
                continue;
            } else {
                syscalls.add(file);
            }
            try {
                // grab the class, make sure it implements Syscall, instantiate, add to list
                String syscallClassName = CLASS_PREFIX + file.substring(0, file.indexOf(CLASS_EXTENSION) - 1);
                Class clas = Class.forName(syscallClassName);
                if (!Syscall.class.isAssignableFrom(clas)) {
                    continue;
                }
                Syscall syscall = (Syscall) clas.newInstance();
                if (syscall.getNumber() == -1) {
                    syscallList.add(syscall);
                } else {
                    throw new Exception("Syscalls must assign -1 for number");
                }
            } catch (Exception e) {
                System.out.println("Error instantiating Syscall from file " + file + ": " + e);
                System.exit(0);
            }
        }
        syscallList = processSyscallNumberOverrides(syscallList);
    }

    // Will get any syscall number override specifications from MARS config file and
    // process them.  This will alter syscallList entry for affected names.
    private static ArrayList<Syscall> processSyscallNumberOverrides(ArrayList<Syscall> syscallList) {
        ArrayList<SyscallNumberOverride> overrides = new Globals().getSyscallOverrides();
        if (syscallList.size() != overrides.size()) {
            System.out.println("Error: the number of entries in the config file does not match the number of syscalls loaded");
            System.exit(0);
        }
        for (SyscallNumberOverride override : overrides) {
            boolean match = false;
            for (Syscall syscall : syscallList) {
                if (syscall.getNumber() == override.getNumber()) {
                    System.out.println("Duplicate service number: " + syscall.getNumber() + " already registered to " +
                            findSyscall(syscall.getNumber()).getName());
                    System.exit(0);
                }
                if (override.getName().equals(syscall.getName())) {
                    if (syscall.getNumber() != -1) {
                        System.out.println("Error: " + syscall.getName() + " was assigned a numebr twice in the config file");
                        System.exit(0);
                    }
                    if (override.getNumber() < 0) {
                        System.out.println("Error: " + override.getName() + " was assigned a negative number");
                        System.exit(0);
                    }
                    // we have a match to service name, assign new number
                    syscall.setNumber(override.getNumber());
                    match = true;
                }
            }
            if (!match) {
                System.out.println("Error: syscall name '" + override.getName() +
                        "' in config file does not match any name in syscall list");
                System.exit(0);
            }
        }
        // Wait until end to check for duplicate numbers.  To do so earlier
        // would disallow for instance the exchange of numbers between two
        // services.  This is N-squared operation but N is small.
        // This will also detect duplicates that accidently occur from addition
        // of a new Syscall subclass to the collection, even if the config file
        // does not contain any overrides.
        Syscall syscallA, syscallB;
        boolean duplicates = false;
        for (int i = 0; i < syscallList.size(); i++) {
            syscallA = syscallList.get(i);
            for (int j = i + 1; j < syscallList.size(); j++) {
                syscallB = syscallList.get(j);
                if (syscallA.getNumber() == syscallB.getNumber()) {
                    System.out.println("Error: syscalls " + syscallA.getName() + " and " +
                            syscallB.getName() + " are both assigned same number " + syscallA.getNumber());
                    duplicates = true;
                }
            }
        }
        if (duplicates) {
            System.exit(0);
        }
        return syscallList;
    }

    /*
     * Method to find Syscall object associated with given service number.
     * Returns null if no associated object found.
     */
    public static Syscall findSyscall(int number) {
        // linear search is OK since number of syscalls is small.
        for (Syscall service : syscallList) {
            if (service.getNumber() == number) {
                return service;
            }
        }
        return null;
    }
}
