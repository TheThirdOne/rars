package mars.simulator;

import mars.Globals;
import mars.ProgramStatement;
import mars.mips.hardware.Coprocessor0;
import mars.mips.hardware.Coprocessor1;
import mars.mips.hardware.RegisterFile;
import mars.mips.instructions.Instruction;

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
 * Used to "step backward" through execution, undoing each instruction.
 *
 * @author Pete Sanderson
 * @version February 2006
 */

public class BackStepper {
    // The types of "undo" actions.  Under 1.5, these would be enumerated type.
    // These fit better in the BackStep class below but inner classes cannot have static members.
    private static final int MEMORY_RESTORE_RAW_WORD = 0;
    private static final int MEMORY_RESTORE_WORD = 1;
    private static final int MEMORY_RESTORE_HALF = 2;
    private static final int MEMORY_RESTORE_BYTE = 3;
    private static final int REGISTER_RESTORE = 4;
    private static final int PC_RESTORE = 5;
    private static final int COPROC0_REGISTER_RESTORE = 6;
    private static final int COPROC1_REGISTER_RESTORE = 7;
    private static final int COPROC1_CONDITION_CLEAR = 8;
    private static final int COPROC1_CONDITION_SET = 9;
    private static final int DO_NOTHING = 10;  // instruction does not write anything.

    // Flag to mark BackStep object as prepresenting specific situation: user manipulates
    // memory/register value via GUI after assembling program but before running it.
    private static final int NOT_PC_VALUE = -1;

    private boolean engaged;
    private BackstepStack backSteps;

    // One can argue using java.util.Stack, given its clumsy implementation.
    // A homegrown linked implementation will be more streamlined, but
    // I anticipate that backstepping will only be used during timed
    // (currently max 30 instructions/second) or stepped execution, where
    // performance is not an issue.  Its Vector implementation may result
    // in quicker garbage collection than a pure linked list implementation.

    /**
     * Create a fresh BackStepper.  It is enabled, which means all
     * subsequent instruction executions will have their "undo" action
     * recorded here.
     */
    public BackStepper() {
        engaged = true;
        backSteps = new BackstepStack(Globals.maximumBacksteps);
    }

    /**
     * Determine whether execution "undo" steps are currently being recorded.
     *
     * @return true if undo steps being recorded, false if not.
     */
    public boolean enabled() {
        return engaged;
    }

    /**
     * Set enable status.
     *
     * @param state If true, will begin (or continue) recoding "undo" steps.  If false, will stop.
     */
    public void setEnabled(boolean state) {
        engaged = state;
    }

    /**
     * Test whether there are steps that can be undone.
     *
     * @return true if there are no steps to be undone, false otherwise.
     */
    public boolean empty() {
        return backSteps.empty();
    }

    /**
     * Determine whether the next back-step action occurred as the result of
     * an instruction that executed in the "delay slot" of a delayed branch.
     *
     * @return true if next backstep is instruction that executed in delay slot,
     * false otherwise.
     */
    // Added 25 June 2007
    public boolean inDelaySlot() {
        return !empty() && backSteps.peek().inDelaySlot;
    }

    /**
     * Carry out a "back step", which will undo the latest execution step.
     * Does nothing if backstepping not enabled or if there are no steps to undo.
     */

    // Note that there may be more than one "step" in an instruction execution; for
    // instance the multiply, divide, and double-precision floating point operations
    // all store their result in register pairs which results in two store operations.
    // Both must be undone transparently, so we need to detect that multiple steps happen
    // together and carry out all of them here.
    // Use a do-while loop based on the backstep's program statement reference.
    public void backStep() {
        if (engaged && !backSteps.empty()) {
            ProgramStatement statement = ((BackStep) backSteps.peek()).ps;
            engaged = false; // GOTTA DO THIS SO METHOD CALL IN SWITCH WILL NOT RESULT IN NEW ACTION ON STACK!
            do {
                BackStep step = (BackStep) backSteps.pop();
            /*
                System.out.println("backstep POP: action "+step.action+" pc "+mars.util.Binary.intToHexString(step.pc)+
            	                   " source "+((step.ps==null)? "none":step.ps.getSource())+
            							 " parm1 "+step.param1+" parm2 "+step.param2);
            */
                if (step.pc != NOT_PC_VALUE) {
                    RegisterFile.setProgramCounter(step.pc);
                }
                try {
                    switch (step.action) {
                        case MEMORY_RESTORE_RAW_WORD:
                            Globals.memory.setRawWord(step.param1, step.param2);
                            break;
                        case MEMORY_RESTORE_WORD:
                            Globals.memory.setWord(step.param1, step.param2);
                            break;
                        case MEMORY_RESTORE_HALF:
                            Globals.memory.setHalf(step.param1, step.param2);
                            break;
                        case MEMORY_RESTORE_BYTE:
                            Globals.memory.setByte(step.param1, step.param2);
                            break;
                        case REGISTER_RESTORE:
                            RegisterFile.updateRegister(step.param1, step.param2);
                            break;
                        case PC_RESTORE:
                            RegisterFile.setProgramCounter(step.param1);
                            break;
                        case COPROC0_REGISTER_RESTORE:
                            Coprocessor0.updateRegister(step.param1, step.param2);
                            break;
                        case COPROC1_REGISTER_RESTORE:
                            Coprocessor1.updateRegister(step.param1, step.param2);
                            break;
                        case COPROC1_CONDITION_CLEAR:
                            Coprocessor1.clearConditionFlag(step.param1);
                            break;
                        case COPROC1_CONDITION_SET:
                            Coprocessor1.setConditionFlag(step.param1);
                            break;
                        case DO_NOTHING:
                            break;
                    }
                } catch (Exception e) {
                    // if the original action did not cause an exception this will not either.
                    System.out.println("Internal MARS error: address exception while back-stepping.");
                    System.exit(0);
                }
            } while (!backSteps.empty() && statement == ((BackStep) backSteps.peek()).ps);
            engaged = true;  // RESET IT (was disabled at top of loop -- see comment)
        }
    }
  
     
      /* Convenience method called below to get program counter value.  If it needs to be
   	 * be modified (e.g. to subtract 4) that can be done here in one place.
   	 */

    private int pc() {
        // PC incremented prior to instruction simulation, so need to adjust for that.
        return RegisterFile.getProgramCounter() - Instruction.INSTRUCTION_LENGTH;
    }

    /**
     * Add a new "back step" (the undo action) to the stack. The action here
     * is to restore a raw memory word value (setRawWord).
     *
     * @param address The affected memory address.
     * @param value   The "restore" value to be stored there.
     * @return the argument value
     */
    public int addMemoryRestoreRawWord(int address, int value) {
        backSteps.push(MEMORY_RESTORE_RAW_WORD, pc(), address, value);
        return value;
    }

    /**
     * Add a new "back step" (the undo action) to the stack. The action here
     * is to restore a memory word value.
     *
     * @param address The affected memory address.
     * @param value   The "restore" value to be stored there.
     * @return the argument value
     */
    public int addMemoryRestoreWord(int address, int value) {
        backSteps.push(MEMORY_RESTORE_WORD, pc(), address, value);
        return value;
    }

    /**
     * Add a new "back step" (the undo action) to the stack.  The action here
     * is to restore a memory half-word value.
     *
     * @param address The affected memory address.
     * @param value   The "restore" value to be stored there, in low order half.
     * @return the argument value
     */
    public int addMemoryRestoreHalf(int address, int value) {
        backSteps.push(MEMORY_RESTORE_HALF, pc(), address, value);
        return value;
    }

    /**
     * Add a new "back step" (the undo action) to the stack.  The action here
     * is to restore a memory byte value.
     *
     * @param address The affected memory address.
     * @param value   The "restore" value to be stored there, in low order byte.
     * @return the argument value
     */
    public int addMemoryRestoreByte(int address, int value) {
        backSteps.push(MEMORY_RESTORE_BYTE, pc(), address, value);
        return value;
    }

    /**
     * Add a new "back step" (the undo action) to the stack.  The action here
     * is to restore a register file register value.
     *
     * @param register The affected register number.
     * @param value    The "restore" value to be stored there.
     * @return the argument value
     */
    public int addRegisterFileRestore(int register, int value) {
        backSteps.push(REGISTER_RESTORE, pc(), register, value);
        return value;
    }

    /**
     * Add a new "back step" (the undo action) to the stack.  The action here
     * is to restore the program counter.
     *
     * @param value The "restore" value to be stored there.
     * @return the argument value
     */
    public int addPCRestore(int value) {
        // adjust for value reflecting incremented PC.
        value -= Instruction.INSTRUCTION_LENGTH;
        // Use "value" insead of "pc()" for second arg because RegisterFile.getProgramCounter()
        // returns branch target address at this point.
        backSteps.push(PC_RESTORE, value, value);
        return value;
    }

    /**
     * Add a new "back step" (the undo action) to the stack.  The action here
     * is to restore a coprocessor 0 register value.
     *
     * @param register The affected register number.
     * @param value    The "restore" value to be stored there.
     * @return the argument value
     */
    public int addCoprocessor0Restore(int register, int value) {
        backSteps.push(COPROC0_REGISTER_RESTORE, pc(), register, value);
        return value;
    }

    /**
     * Add a new "back step" (the undo action) to the stack.  The action here
     * is to restore a coprocessor 1 register value.
     *
     * @param register The affected register number.
     * @param value    The "restore" value to be stored there.
     * @return the argument value
     */
    public int addCoprocessor1Restore(int register, int value) {
        backSteps.push(COPROC1_REGISTER_RESTORE, pc(), register, value);
        return value;
    }

    /**
     * Add a new "back step" (the undo action) to the stack.  The action here
     * is to set the given coprocessor 1 condition flag (to 1).
     *
     * @param flag The condition flag number.
     * @return the argument value
     */
    public int addConditionFlagSet(int flag) {
        backSteps.push(COPROC1_CONDITION_SET, pc(), flag);
        return flag;
    }

    /**
     * Add a new "back step" (the undo action) to the stack.  The action here
     * is to clear the given coprocessor 1 condition flag (to 0).
     *
     * @param flag The condition flag number.
     * @return the argument value
     */
    public int addConditionFlagClear(int flag) {
        backSteps.push(COPROC1_CONDITION_CLEAR, pc(), flag);
        return flag;
    }

    /**
     * Add a new "back step" (the undo action) to the stack.  The action here
     * is to do nothing!  This is just a place holder so when user is backstepping
     * through the program no instructions will be skipped.  Cosmetic. If the top of the
     * stack has the same PC counter, the do-nothing action will not be added.
     *
     * @return 0
     */
    public int addDoNothing(int pc) {
        if (backSteps.empty() || backSteps.peek().pc != pc) {
            backSteps.push(DO_NOTHING, pc);
        }
        return 0;
    }


    // Represents a "back step" (undo action) on the stack.
    private class BackStep {
        private int action;  // what do do MEMORY_RESTORE_WORD, etc
        private int pc;      // program counter value when original step occurred
        private ProgramStatement ps;   // statement whose action is being "undone" here
        private int param1;  // first parameter required by that action
        private int param2;  // optional second parameter required by that action
        private boolean inDelaySlot; // true if instruction executed in "delay slot" (delayed branching enabled)

        // it is critical that BackStep object get its values by calling this method
        // rather than assigning to individual members, because of the technique used
        // to set its ps member (and possibly pc).
        private void assign(int act, int programCounter, int parm1, int parm2) {
            action = act;
            pc = programCounter;
            try {
                // Client does not have direct access to program statement, and rather than making all
                // of them go through the methods below to obtain it, we will do it here.
                // Want the program statement but do not want observers notified.
                ps = Globals.memory.getStatementNoNotify(programCounter);
            } catch (Exception e) {
                // The only situation causing this so far: user modifies memory or register
                // contents through direct manipulation on the GUI, after assembling the program but
                // before starting to run it (or after backstepping all the way to the start).
                // The action will not be associated with any instruction, but will be carried out
                // when popped.
                ps = null;
                pc = NOT_PC_VALUE; // Backstep method above will see this as flag to not set PC
            }
            param1 = parm1;
            param2 = parm2;
            inDelaySlot = Simulator.inDelaySlot(); // ADDED 25 June 2007
         /*				
            System.out.println("backstep PUSH: action "+action+" pc "+mars.util.Binary.intToHexString(pc)+
         		                   " source "+((ps==null)? "none":ps.getSource())+
         								 " parm1 "+param1+" parm2 "+param2);
         */
        }
    }

    // *****************************************************************************
    // special purpose stack class for backstepping.  You've heard of circular queues
    // implemented with an array, right?  This is a circular stack!  When full, the
    // newly-pushed item overwrites the oldest item, with circular top!  All operations
    // are constant time.  It's synchronized too, to be safe (is used by both the
    // simulation thread and the GUI thread for the back-step button).
    // Upon construction, it is filled with newly-created empty BackStep objects which
    // will exist for the life of the stack.  Push does not create a BackStep object
    // but instead overwrites the contents of the existing one.  Thus during MIPS
    // program (simulated) execution, BackStep objects are never created or junked
    // regardless of how many steps are executed.  This will speed things up a bit
    // and make life easier for the garbage collector.

    private class BackstepStack {
        private int capacity;
        private int size;
        private int top;
        private BackStep[] stack;

        // Stack is created upon successful assembly or reset.  The one-time overhead of
        // creating all the BackStep objects will not be noticed by the user, and enhances
        // runtime performance by not having to create or recycle them during MIPS
        // program execution.
        private BackstepStack(int capacity) {
            this.capacity = capacity;
            this.size = 0;
            this.top = -1;
            this.stack = new BackStep[capacity];
            for (int i = 0; i < capacity; i++) {
                this.stack[i] = new BackStep();
            }
        }

        private synchronized boolean empty() {
            return size == 0;
        }

        private synchronized void push(int act, int programCounter, int parm1, int parm2) {
            if (size == 0) {
                top = 0;
                size++;
            } else if (size < capacity) {
                top = (top + 1) % capacity;
                size++;
            } else { // size == capacity.  The top moves up one, replacing oldest entry (goodbye!)
                top = (top + 1) % capacity;
            }
            // We'll re-use existing objects rather than create/discard each time.
            // Must use assign() method rather than series of assignment statements!
            stack[top].assign(act, programCounter, parm1, parm2);
        }

        private synchronized void push(int act, int programCounter, int parm1) {
            push(act, programCounter, parm1, 0);
        }

        private synchronized void push(int act, int programCounter) {
            push(act, programCounter, 0, 0);
        }

        // NO PROTECTION.  This class is used only within this file so there is no excuse
        // for trying to pop from empty stack.
        private synchronized BackStep pop() {
            BackStep bs;
            bs = stack[top];
            if (size == 1) {
                top = -1;
            } else {
                top = (top + capacity - 1) % capacity;
            }
            size--;
            return bs;
        }

        // NO PROTECTION.  This class is used only within this file so there is no excuse
        // for trying to peek from empty stack.
        private synchronized BackStep peek() {
            return stack[top];
        }

    }

}