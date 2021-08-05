package rars.riscv;

import rars.Globals;
import rars.ProgramStatement;
import rars.Settings;
import rars.SimulationException;
import rars.riscv.hardware.RegisterFile;
import rars.riscv.syscalls.*;
import rars.util.FilenameFinder;
import rars.util.SystemIO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.util.*;

	/*
Copyright (c) 2003-2013,  Pete Sanderson and Kenneth Vollmar

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
 * The list of Instruction objects, each of which represents a RISCV instruction.
 * The instruction may either be basic (translates into binary machine code) or
 * extended (translates into sequence of one or more basic instructions).
 *
 * @author Pete Sanderson and Ken Vollmar
 * @version August 2003-5
 */

public class InstructionSet {
    private static final String CLASS_PREFIX = "rars.riscv.instructions.";
    private static final String INSTRUCTIONS_DIRECTORY_PATH = "rars/riscv/instructions";
    private static final String CLASS_EXTENSION = "class";
    public static boolean rv64 = Globals.getSettings().getBooleanSetting(Settings.Bool.RV64_ENABLED);

    private ArrayList<Instruction> instructionList;
    private ArrayList<MatchMap> opcodeMatchMaps;

    /**
     * Creates a new InstructionSet object.
     */
    public InstructionSet() {
        instructionList = new ArrayList<>();

    }

    /**
     * Retrieve the current instruction set.
     */
    public ArrayList<Instruction> getInstructionList() {
        return instructionList;

    }

    /**
     * Adds all instructions to the set.  A given extended instruction may have
     * more than one Instruction object, depending on how many formats it can have.
     *
     * @see Instruction
     * @see BasicInstruction
     * @see ExtendedInstruction
     */
    public void populate() {
        /* Here is where the parade begins.  Every instruction is added to the set here.*/
        instructionList.clear();
        // ////////////////////////////////////   BASIC INSTRUCTIONS START HERE ////////////////////////////////

        addBasicInstructions();

        ////////////// READ PSEUDO-INSTRUCTION SPECS FROM DATA FILE AND ADD //////////////////////
        if(rv64) {
            addPseudoInstructions("/PseudoOps-64.txt");
        }

        addPseudoInstructions("/PseudoOps.txt");
        // Initialization step.  Create token list for each instruction example.  This is
        // used by parser to determine user program correct syntax.
        for (Instruction inst : instructionList) {
            inst.createExampleTokenList();
        }

        HashMap<Integer, HashMap<Integer, BasicInstruction>> maskMap = new HashMap<>();
        ArrayList<MatchMap> matchMaps = new ArrayList<>();
        for (Instruction inst : instructionList) {
            if (inst instanceof BasicInstruction) {
                BasicInstruction basic = (BasicInstruction) inst;
                Integer mask = basic.getOpcodeMask();
                Integer match = basic.getOpcodeMatch();
                HashMap<Integer, BasicInstruction> matchMap = maskMap.get(mask);
                if (matchMap == null) {
                    matchMap = new HashMap<>();
                    maskMap.put(mask, matchMap);
                    matchMaps.add(new MatchMap(mask, matchMap));
                }
                matchMap.put(match, basic);
            }
        }
        Collections.sort(matchMaps);
        this.opcodeMatchMaps = matchMaps;
    }

    public BasicInstruction findByBinaryCode(int binaryInstr) {
        for (MatchMap map : this.opcodeMatchMaps) {
            BasicInstruction ret = map.find(binaryInstr);
            if (ret != null) return ret;
        }
        return null;
    }

    private void addBasicInstructions() {
        // grab all class files in the same directory as Syscall
        ArrayList<String> candidates = FilenameFinder.getFilenameList(this.getClass().getClassLoader(),
                INSTRUCTIONS_DIRECTORY_PATH, CLASS_EXTENSION);
        HashSet<String> insts = new HashSet<>();
        for (String file : candidates) {
            // Do not add class if already encountered (happens if run in MARS development directory)
            if (insts.contains(file)) {
                continue;
            } else {
                insts.add(file);
            }
            try {
                // grab the class, make sure it implements Syscall, instantiate, add to list
                String syscallClassName = CLASS_PREFIX + file.substring(0, file.indexOf(CLASS_EXTENSION) - 1);
                Class clas = Class.forName(syscallClassName);
                if (!BasicInstruction.class.isAssignableFrom(clas) ||
                        Modifier.isAbstract(clas.getModifiers()) ||
                        Modifier.isInterface(clas.getModifiers())) {
                    continue;
                }
                try {
                    instructionList.add((BasicInstruction) clas.newInstance());
                }catch (NullPointerException ne){
                    if (ne.toString().contains("rv"))continue;
                    throw ne;
                }
            } catch (Exception e) {
                System.out.println("Error instantiating Instruction from file " + file + ": " + e);
                System.exit(0);
            }
        }
    }
    /*  METHOD TO ADD PSEUDO-INSTRUCTIONS
    */
    private void addPseudoInstructions(String file) {
        InputStream is = null;
        BufferedReader in = null;
        try {
            // leading "/" prevents package name being prepended to filepath.
            is = this.getClass().getResourceAsStream(file);
            in = new BufferedReader(new InputStreamReader(is));
        } catch (NullPointerException e) {
            System.out.println(
                    "Error: Pseudo-instruction file PseudoOps.txt not found.");
            System.exit(0);
        }
        try {
            String line, pseudoOp, template, token;
            String description;
            StringTokenizer tokenizer;
            while ((line = in.readLine()) != null) {
                // skip over: comment lines, empty lines, lines starting with blank.
                if (!line.startsWith("#") && !line.startsWith(" ")
                        && line.length() > 0) {
                    description = "";
                    tokenizer = new StringTokenizer(line, ";");
                    pseudoOp = tokenizer.nextToken();
                    template = "";
                    while (tokenizer.hasMoreTokens()) {
                        token = tokenizer.nextToken();
                        if (token.startsWith("#")) {
                            // Optional description must be last token in the line.
                            description = token.substring(1);
                            break;
                        }
                        template = template + token;
                        if (tokenizer.hasMoreTokens()) {
                            template = template + "\n";
                        }
                    }
                    instructionList.add(new ExtendedInstruction(pseudoOp, template, description));
                    //if (firstTemplate != null) System.out.println("\npseudoOp: "+pseudoOp+"\ndefault template:\n"+firstTemplate+"\ncompact template:\n"+template);
                }
            }
            in.close();
        } catch (IOException ioe) {
            System.out.println(
                    "Internal Error: Pseudo-instructions could not be loaded.");
            System.exit(0);
        } catch (Exception ioe) {
            System.out.println(
                    "Internal Error: Invalid pseudo-instruction specification.");
            System.exit(0);
        }

    }

    /**
     * Given an operator mnemonic, will return the corresponding Instruction object(s)
     * from the instruction set.  Uses straight linear search technique.
     *
     * @param name operator mnemonic (e.g. addi, sw,...)
     * @return list of corresponding Instruction object(s), or null if not found.
     */
    public ArrayList<Instruction> matchOperator(String name) {
        ArrayList<Instruction> matchingInstructions = null;
        // Linear search for now....
        for (Instruction inst : instructionList) {
            if (inst.getName().equalsIgnoreCase(name)) {
                if (matchingInstructions == null)
                    matchingInstructions = new ArrayList<>();
                matchingInstructions.add(inst);
            }
        }
        return matchingInstructions;
    }


    // TODO: check to see if autocomplete was accidentally removed
    /**
     * Given a string, will return the Instruction object(s) from the instruction
     * set whose operator mnemonic prefix matches it.  Case-insensitive.  For example
     * "s" will match "sw", "sh", "sb", etc.  Uses straight linear search technique.
     *
     * @param name a string
     * @return list of matching Instruction object(s), or null if none match.
     */
    public ArrayList<Instruction> prefixMatchOperator(String name) {
        ArrayList<Instruction> matchingInstructions = null;
        // Linear search for now....
        if (name != null) {
            for (Instruction inst : instructionList) {
                if (inst.getName().toLowerCase().startsWith(name.toLowerCase())) {
                    if (matchingInstructions == null)
                        matchingInstructions = new ArrayList<>();
                    matchingInstructions.add(inst);
                }
            }
        }
        return matchingInstructions;
    }

   	/*
        * Method to find and invoke a syscall given its service number.  Each syscall
   	 * function is represented by an object in an array list.  Each object is of
   	 * a class that implements Syscall or extends AbstractSyscall.
   	 */

    public static void findAndSimulateSyscall(int number, ProgramStatement statement)
            throws SimulationException {
        AbstractSyscall service = SyscallLoader.findSyscall(number);
        if (service != null) {
            // TODO: find a cleaner way of doing this
            // This was introduced to solve issue #108
            boolean is_writing = service instanceof SyscallPrintChar ||
                    service instanceof SyscallPrintDouble ||
                    service instanceof SyscallPrintFloat ||
                    service instanceof SyscallPrintInt ||
                    service instanceof SyscallPrintIntBinary ||
                    service instanceof SyscallPrintIntHex ||
                    service instanceof SyscallPrintIntUnsigned ||
                    service instanceof SyscallPrintString ||
                    service instanceof SyscallWrite;
            if (!is_writing) {
                SystemIO.flush(true);
            }
            service.simulate(statement);
            return;
        }
        throw new SimulationException(statement,
                "invalid or unimplemented syscall service: " +
                        number + " ", SimulationException.ENVIRONMENT_CALL);
    }

   	/*
        * Method to process a successful branch condition.  DO NOT USE WITH JUMP
   	 * INSTRUCTIONS!  The branch operand is a relative displacement in words
   	 * whereas the jump operand is an absolute address in bytes.
   	 *
   	 * The parameter is displacement operand from instruction.
   	 */

    public static void processBranch(int displacement) {
        // Decrement needed because PC has already been incremented
        RegisterFile.setProgramCounter(RegisterFile.getProgramCounter() + displacement - Instruction.INSTRUCTION_LENGTH);
    }
    public static void processBranch(int displacement, int hart) {
        // Decrement needed because PC has already been incremented
        RegisterFile.setProgramCounter(RegisterFile.getProgramCounter(hart) + displacement - Instruction.INSTRUCTION_LENGTH, hart);
    }

   	/*
        * Method to process a jump.  DO NOT USE WITH BRANCH INSTRUCTIONS!
   	 * The branch operand is a relative displacement in words
   	 * whereas the jump operand is an absolute address in bytes.
   	 *
   	 * The parameter is jump target absolute byte address.
   	 */

    public static void processJump(int targetAddress) {
        RegisterFile.setProgramCounter(targetAddress);
    }
    public static void processJump(int targetAddress, int hart) {
        RegisterFile.setProgramCounter(targetAddress, hart);
    }
   	/*
        * Method to process storing of a return address in the given
   	 * register.  This is used only by the "and link"
   	 * instructions: jal and jalr
   	 * The parameter is register number to receive the return address.
   	 */

    public static void processReturnAddress(int register) {
        RegisterFile.updateRegister(register, RegisterFile.getProgramCounter());
    }
    public static void processReturnAddress(int register, int hart){
        RegisterFile.updateRegister(register, RegisterFile.getProgramCounter(hart), hart);
    }
    private static class MatchMap implements Comparable<MatchMap> {
        private int mask;
        private int maskLength; // number of 1 bits in mask
        private HashMap<Integer, BasicInstruction> matchMap;

        public MatchMap(int mask, HashMap<Integer, BasicInstruction> matchMap) {
            this.mask = mask;
            this.matchMap = matchMap;

            int k = 0;
            int n = mask;
            while (n != 0) {
                k++;
                n &= n - 1;
            }
            this.maskLength = k;
        }

        public boolean equals(Object o) {
            return o instanceof MatchMap && mask == ((MatchMap) o).mask;
        }

        public int compareTo(MatchMap other) {
            int d = other.maskLength - this.maskLength;
            if (d == 0) d = this.mask - other.mask;
            return d;
        }

        public BasicInstruction find(int instr) {
            int match = instr & mask;
            return matchMap.get(match);
        }
    }
}
