package mars.mips.instructions;

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
 * These are the MIPS-defined formats of basic machine instructions.  The R-format indicates
 * the instruction works only with registers.  The I-format indicates the instruction
 * works with an immediate value (e.g. constant).  The J-format indicates this is a Jump
 * instruction.  The I-branch-format is defined by me, not MIPS, to to indicate this is
 * a Branch instruction, specifically to distinguish immediate
 * values used as target addresses.
 *
 * @author Pete Sanderson
 * @version August 2003
 */
public class BasicInstructionFormat {
    public static final BasicInstructionFormat R_FORMAT = new BasicInstructionFormat();
    public static final BasicInstructionFormat I_FORMAT = new BasicInstructionFormat();
    public static final BasicInstructionFormat I_BRANCH_FORMAT = new BasicInstructionFormat();
    public static final BasicInstructionFormat J_FORMAT = new BasicInstructionFormat();

    // private default constructor prevents objects of this class other than those above.
    private BasicInstructionFormat() {
    }
}
