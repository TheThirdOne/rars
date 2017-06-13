package mars.mips.instructions;

import mars.Globals;
import mars.MIPSprogram;
import mars.Settings;
import mars.assembler.Symbol;
import mars.assembler.TokenList;
import mars.mips.hardware.Coprocessor1;
import mars.mips.hardware.RegisterFile;
import mars.util.Binary;

import java.util.ArrayList;
import java.util.StringTokenizer;

	/*
Copyright (c) 2003-2008,  Pete Sanderson and Kenneth Vollmar

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
 * ExtendedInstruction represents a MIPS extended (a.k.a pseudo) instruction.  This
 * assembly language instruction does not have a corresponding machine instruction.  Instead
 * it is translated by the extended assembler into one or more basic instructions (operations
 * that have a corresponding machine instruction).
 *
 * @author Pete Sanderson
 * @version August 2003
 */
public class ExtendedInstruction extends Instruction {

    private ArrayList<String> translationStrings, compactTranslationStrings;

    /**
     * Constructor for ExtendedInstruction.
     *
     * @param example            A String containing example use of the MIPS extended instruction.
     * @param translation        Specifications for translating this instruction into a sequence
     *                           of one or more MIPS basic instructions.
     * @param compactTranslation Alternative translation that can be used if running under
     *                           a compact (16 bit) memory configuration.
     * @param description        a helpful description to be included on help requests
     *                           <p>
     *                           The presence of an alternative "compact translation" can optimize code generation
     *                           by assuming that data label addresses are 16 bits instead of 32
     **/

    public ExtendedInstruction(String example, String translation, String compactTranslation, String description) {
        this.exampleFormat = example;
        this.description = description;
        this.mnemonic = this.extractOperator(example);
        this.createExampleTokenList();
        this.translationStrings = buildTranslationList(translation);
        this.compactTranslationStrings = buildTranslationList(compactTranslation);
    }

    /**
     * Constructor for ExtendedInstruction.  No compact translation is provided.
     *
     * @param example     A String containing example use of the MIPS extended instruction.
     * @param translation Specifications for translating this instruction into a sequence
     *                    of one or more MIPS basic instructions.
     * @param description a helpful description to be included on help requests
     **/

    public ExtendedInstruction(String example, String translation, String description) {
        this.exampleFormat = example;
        this.description = description;
        this.mnemonic = this.extractOperator(example);
        this.createExampleTokenList();
        this.translationStrings = buildTranslationList(translation);
        this.compactTranslationStrings = null;
    }

    /**
     * Constructor for ExtendedInstruction, where no instruction description or
     * compact translation is provided.
     *
     * @param example     A String containing example use of the MIPS extended instruction.
     * @param translation Specifications for translating this instruction into a sequence
     *                    of one or more MIPS basic instructions.
     **/

    public ExtendedInstruction(String example, String translation) {
        this(example, translation, "");
    }

    /**
     * Get length in bytes that this extended instruction requires in its
     * binary form. The answer depends on how many basic instructions it
     * expands to.  This may vary, if expansion includes a nop, depending on
     * whether or not delayed branches are enabled. Each requires 4 bytes.
     *
     * @return int length in bytes of corresponding binary instruction(s).
     */

    public int getInstructionLength() {
        return getInstructionLength(translationStrings);
    }


    /**
     * Get ArrayList of Strings that represent list of templates for
     * basic instructions generated by this extended instruction.
     *
     * @return ArrayList of Strings.
     */

    public ArrayList<String> getBasicIntructionTemplateList() {
        return translationStrings;
    }

    /**
     * Get length in bytes that this extended instruction requires in its
     * binary form if it includes an alternative expansion for compact
     * memory (16 bit addressing) configuration. The answer depends on
     * how many basic instructions it expands to.  This may vary, if
     * expansion includes a nop, depending on whether or not delayed
     * branches are enabled. Each requires 4 bytes.
     *
     * @return int length in bytes of corresponding binary instruction(s).
     * Returns 0 if an alternative expansion is not defined for this
     * instruction.
     */

    public int getCompactInstructionLength() {
        return getInstructionLength(compactTranslationStrings);
    }


    /**
     * Determine whether or not this pseudo-instruction has a second
     * translation optimized for 16 bit address space: a compact version.
     */
    public boolean hasCompactTranslation() {
        return compactTranslationStrings != null;
    }


    /**
     * Get ArrayList of Strings that represent list of templates for
     * basic instructions generated by the "compact" or 16-bit version
     * of this extended instruction.
     *
     * @return ArrayList of Strings.  Returns null if the instruction does not
     * have a compact alternative.
     */

    public ArrayList<String> getCompactBasicIntructionTemplateList() {
        return compactTranslationStrings;
    }

    /**
     * Given a basic instruction template and the list of tokens from an extended
     * instruction statement, substitute operands from the token list appropriately into the
     * template to generate the basic statement.  Assumes the extended instruction statement has
     * been translated from source form to basic assembly form (e.g. register mnemonics
     * translated to corresponding register numbers).
     * Operand format of source statement is already verified correct.
     * Assume the template has correct number and positions of operands.
     * Template is String with special markers.  In the list below, n represents token position (1,2,3,etc)
     * in source statement (operator is token 0, parentheses count but commas don't):
     * <UL>
     * <LI>RGn means substitute register found in n'th token of source statement
     * <LI>NRn means substitute next higher register than the one in n'th token of source code
     * <LI>OPn means substitute n'th token of source code as is
     * <LI>LLn means substitute low order 16 bits from label address in source token n.
     * <LI>LLnU means substitute low order 16 bits (unsigned) from label address in source token n.
     * <LI>LLnPm (m=1,2,3,4) means substitute low order 16 bits from label address in source token n, after adding m.
     * <LI>LHn means substitute high order 16 bits from label address in source token n. Must add 1 if address bit 15 is 1.
     * <LI>LHnPm (m=1,2,3,4) means substitute high order 16 bits from label address in source token n, after adding m. Must then add 1 if bit 15 is 1.
     * <LI>VLn means substitute low order 16 bits from 32 bit value in source token n.
     * <LI>VLnU means substitute low order 16 bits (unsigned) from 32 bit value in source token n.
     * <LI>VLnPm (m=1,2,3,4) means substitute low order 16 bits from 32 bit value in source token n, after adding m to value.
     * <LI>VLnPmU (m=1,2,3,4) means substitute low order 16 bits (unsigned) from 32 bit value in source token n, after adding m to value.
     * <LI>VHLn means substitute high order 16 bits from 32 bit value in source token n.  Use this if later combined with low order 16 bits using "ori $1,$1,VLn". See logical and branch operations.
     * <LI>VHn means substitute high order 16 bits from 32 bit value in source token n, then add 1 if value's bit 15 is 1.  Use this only if later instruction uses VLn($1) to calculate 32 bit address.  See loads and stores.
     * <LI>VHLnPm (m=1,2,3,4) means substitute high order 16 bits from 32 bit value in source token n, after adding m.  See VHLn.
     * <LI>VHnPm (m=1,2,3,4) means substitute high order 16 bits from 32 bit value in source token n, after adding m. Must then add 1 if bit 15 is 1. See VHn.
     * <LI>LLP is similar to LLn, but is needed for "label+100000" address offset. Immediate is added before taking low order 16.
     * <LI>LLPU is similar to LLnU, but is needed for "label+100000" address offset. Immediate is added before taking low order 16 (unsigned).
     * <LI>LLPPm (m=1,2,3,4) is similar to LLP except m is added along with mmediate before taking low order 16.
     * <LI>LHPA is similar to LHn, but is needed for "label+100000" address offset. Immediate is added before taking high order 16.
     * <LI>LHPN is similar to LHPA, used only by "la" instruction. Address resolved by "ori" so do not add 1 if bit 15 is 1.
     * <LI>LHPAPm (m=1,2,3,4) is similar to LHPA except value m is added along with immediate before taking high order 16.
     * <LI>LHL means substitute high order 16 bits from label address in token 2 of "la" (load address) source statement.
     * <LI>LAB means substitute textual label from last token of source statement.  Used for various branches.
     * <LI>S32 means substitute the result of subtracting the constant value in last token from 32.  Used by "ror", "rol".
     * <LI>DBNOP means Delayed Branching NOP - generate a "nop" instruction but only if delayed branching is enabled. Added in 3.4.1 release.
     * <LI>BROFFnm means substitute n if delayed branching is NOT enabled otherwise substitute m.  n and m are single digit numbers indicating constant branch offset (in words).  Added in 3.4.1 release.
     * </UL>
     *
     * @param template  a String containing template for basic statement.
     * @param tokenList a TokenList containing tokens from extended instruction.
     * @return String representing basic assembler statement.
     */

    public static String makeTemplateSubstitutions(MIPSprogram program, String template, TokenList tokenList) {
        String instruction = template;
        int index;
        // Added 22 Jan 2008 by DPS.  The DBNOP template means to generate a "nop" instruction if delayed branching
        // is enabled and generate no instruction otherwise.

        //This is the goal, but it leads to a cascade of
        // additional changes, so for now I will generate "nop" in either case, then come back to it for the
        // next major release.
        if (instruction.contains("DBNOP")) {
            return Globals.getSettings().getBooleanSetting(Settings.DELAYED_BRANCHING_ENABLED) ? "nop" : "";
        }
        // substitute first operand token for template's RG1 or OP1, second for RG2 or OP2, etc
        for (int op = 1; op < tokenList.size(); op++) {
            instruction = substitute(instruction, "RG" + op, tokenList.get(op).getValue());
            instruction = substitute(instruction, "OP" + op, tokenList.get(op).getValue());
            // substitute upper 16 bits of label address, after adding singe digit constant following P
            if ((index = instruction.indexOf("LH" + op + "P")) >= 0) {
                // Label, last operand, has already been translated to address by symtab lookup
                String label = tokenList.get(op).getValue();
                int addr = 0;
                int add = instruction.charAt(index + 4) - 48; // extract the digit following P
                try {
                    addr = Binary.stringToInt(label) + add;    // KENV   1/6/05
                } catch (NumberFormatException e) {
                    // this won't happen...
                }
                // If bit 15 is 1, that means lower 16 bits will become a negative offset!  To
                // compensate if that is the case, we need to add 1 to the high 16 bits.
                int extra = Binary.bitValue(addr, 15);
                instruction = substitute(instruction, "LH" + op + "P" + add, String.valueOf((addr >> 16) + extra));
            }
            // substitute upper 16 bits of label address
            // NOTE: form LHnPm will not match here since it is discovered and substituted above.
            if (instruction.contains("LH" + op)) {
                // Label, last operand, has already been translated to address by symtab lookup
                String label = tokenList.get(op).getValue();
                int addr = 0;
                try {
                    addr = Binary.stringToInt(label);    // KENV   1/6/05
                } catch (NumberFormatException e) {
                    // this won't happen...
                }
                // If bit 15 is 1, that means lower 16 bits will become a negative offset!  To
                // compensate if that is the case, we need to add 1 to the high 16 bits.
                int extra = Binary.bitValue(addr, 15);
                instruction = substitute(instruction, "LH" + op, String.valueOf((addr >> 16) + extra));
            }
            // substitute lower 16 bits of label address, after adding single digit that follows P
            if ((index = instruction.indexOf("LL" + op + "P")) >= 0) {
                // label has already been translated to address by symtab lookup.
                String label = tokenList.get(op).getValue();
                int addr = 0;
                int add = instruction.charAt(index + 4) - 48; // digit that follows P
                try {
                    addr = Binary.stringToInt(label) + add;     // KENV   1/6/05
                } catch (NumberFormatException e) {
                    // this won't happen...
                }
                instruction = substitute(instruction, "LL" + op + "P" + add, String.valueOf(addr << 16 >> 16));//addr & 0xffff));
            }
            // substitute lower 16 bits of label address.
            // NOTE: form LLnPm will not match here since it is discovered and substituted above.
            if ((index = instruction.indexOf("LL" + op)) >= 0) {
                // label has already been translated to address by symtab lookup.
                String label = tokenList.get(op).getValue();
                int addr = 0;
                try {
                    addr = Binary.stringToInt(label);     // KENV   1/6/05
                } catch (NumberFormatException e) {
                    // this won't happen...
                }
                if ((instruction.length() > index + 3) && (instruction.charAt(index + 3) == 'U')) {
                    instruction = substitute(instruction, "LL" + op + "U", String.valueOf(addr & 0xffff));
                } else {
                    instruction = substitute(instruction, "LL" + op, String.valueOf(addr << 16 >> 16));//addr & 0xffff));
                }
            }
            // Substitute upper 16 bits of value after adding 1,2,3,4, (any single digit)
            // Added by DPS on 22 Jan 2008 to fix "ble" and "bgt" bug [do not adjust for bit 15==1]
            if ((index = instruction.indexOf("VHL" + op + "P")) >= 0) {
                String value = tokenList.get(op).getValue();
                int val = 0;
                int add = instruction.charAt(index + 5) - '0'; // amount to add: 1,2,3,4 (any single digit)
                try { // KENV 1/6/05
                    val = Binary.stringToInt(value) + add;
                } catch (NumberFormatException e) {
                    // this won't happen...
                }
                instruction = substitute(instruction, "VHL" + op + "P" + add, String.valueOf(val >> 16));
            }
            // substitute upper 16 bits of value after adding 1,2,3,4, then adjust
            // if necessary, if resulting bit 15 is 1 (see "extra" below)
            if ((index = instruction.indexOf("VH" + op + "P")) >= 0) {
                String value = tokenList.get(op).getValue();
                int val = 0;
                int add = instruction.charAt(index + 4) - '0'; // amount to add: 1,2,3,4 (any single digit)
                try { // KENV 1/6/05
                    val = Binary.stringToInt(value) + add;
                } catch (NumberFormatException e) {
                    // this won't happen...
                }
                // If bit 15 is 1, that means lower 16 bits will become a negative offset!  To
                // compensate if that is the case, we need to add 1 to the high 16 bits.
                int extra = Binary.bitValue(val, 15);
                instruction = substitute(instruction, "VH" + op + "P" + add, String.valueOf((val >> 16) + extra));
            }
            // substitute upper 20 bits of value, adjusted if necessary (see "extra" below)
            // NOTE: if VHnPm appears it will not match here; already substituted by code above
            if (instruction.contains("VH" + op)) {
                String value = tokenList.get(op).getValue();
                int val = 0;
                try {
                    val = Binary.stringToInt(value);      // KENV   1/6/05
                } catch (NumberFormatException e) {
                    // this won't happen...
                }
                // If bit 11 is 1, that means lower 12 bits will become a negative offset!  To
                // compensate if that is the case, we need to add 1 to the high 20 bits.
                int extra = Binary.bitValue(val, 11);
                instruction = substitute(instruction, "VH" + op, String.valueOf((val >> 12) + extra));
            }
            // substitute lower 16 bits of value after adding specified amount (1,2,3,4)
            if ((index = instruction.indexOf("VL" + op + "P")) >= 0) {
                String value = tokenList.get(op).getValue();
                int val = 0;
                int add = instruction.charAt(index + 4) - '0'; // P is followed by 1,2,3,4(any single digit OK)
                try {  // KENV 1/6/05
                    val = Binary.stringToInt(value) + add;
                } catch (NumberFormatException e) {
                    // this won't happen...
                }
                if ((instruction.length() > index + 5) && (instruction.charAt(index + 5) == 'U')) {
                    instruction = substitute(instruction, "VL" + op + "P" + add + "U", String.valueOf(val & 0xffff));
                } else {
                    instruction = substitute(instruction, "VL" + op + "P" + add, String.valueOf(val << 16 >> 16));//val & 0xffff));
                }
            }
            // substitute lower 12 bits of value.  NOTE: VLnPm already substituted by above code.
            if ((index = instruction.indexOf("VL" + op)) >= 0) {
                String value = tokenList.get(op).getValue();
                int val = 0;
                try {
                    val = Binary.stringToInt(value);      // KENV   1/6/05
                } catch (NumberFormatException e) {
                    // this won't happen...
                }
                if ((instruction.length() > index + 3) && (instruction.charAt(index + 3) == 'U')) {
                    instruction = substitute(instruction, "VL" + op + "U", String.valueOf(val & 0xfff));
                } else {
                    instruction = substitute(instruction, "VL" + op, String.valueOf(val << 20 >> 20));
                }
            }
            // substitute upper 16 bits of 32 bit value
            if (instruction.contains("VHL" + op)) {
                // value has to be second operand token.
                String value = tokenList.get(op).getValue(); // has to be token 2 position
                int val = 0;
                try {
                    val = Binary.stringToInt(value);      // KENV   1/6/05
                } catch (NumberFormatException e) {
                    // this won't happen...
                }
                instruction = substitute(instruction, "VHL" + op, String.valueOf(val >> 16));
            }
        }
        // substitute upper 16 bits of label address for "la"
        if (instruction.contains("LHL")) {
            // Label has already been translated to address by symtab lookup
            String label = tokenList.get(2).getValue();  // has to be token 2 position
            int addr = 0;
            try {
                addr = Binary.stringToInt(label);    // KENV   1/6/05
            } catch (NumberFormatException e) {
                // this won't happen...
            }
            instruction = substitute(instruction, "LHL", String.valueOf(addr >> 16));
        }

        // substitute upper 16 bits of label address after adding the digit that follows "P" and
        // also adding the immediate e.g. here+44($s0)
        // Address will be resolved using addition, so need to add 1 to upper half if bit 15 is 1.
        if ((index = instruction.indexOf("LHPAP")) >= 0) {
            // Label has already been translated to address by symtab lookup
            String label = tokenList.get(2).getValue();  // 2 is only possible token position
            String addend = tokenList.get(4).getValue();  // 4 is only possible token position
            int addr = 0;
            int add = instruction.charAt(index + 5) - 48; // extract digit following P
            try {
                addr = Binary.stringToInt(label) +    // KENV   1/6/05
                        Binary.stringToInt(addend) + add;
            } catch (NumberFormatException e) {
                // this won't happen...
            }
            // If bit 15 is 1, that means lower 16 bits will become a negative offset!  To
            // compensate if that is the case, we need to add 1 to the high 16 bits.
            int extra = Binary.bitValue(addr, 15);
            instruction = substitute(instruction, "LHPAP" + add, String.valueOf((addr >> 16) + extra));
        }
        // substitute upper 16 bits of label address after adding constant e.g. here+4($s0)
        // Address will be resolved using addition, so need to add 1 to upper half if bit 15 is 1.
        // NOTE: format LHPAPm is recognized and substituted by the code above.
        if (instruction.contains("LHPA")) {
            // Label has already been translated to address by symtab lookup
            String label = tokenList.get(2).getValue();  // 2 is only possible token position
            String addend = tokenList.get(4).getValue();  // 4 is only possible token position
            int addr = 0;
            try {
                addr = Binary.stringToInt(label) +    // KENV   1/6/05
                        Binary.stringToInt(addend);
            } catch (NumberFormatException e) {
                // this won't happen...
            }
            // If bit 15 is 1, that means lower 16 bits will become a negative offset!  To
            // compensate if that is the case, we need to add 1 to the high 16 bits.
            int extra = Binary.bitValue(addr, 15);
            instruction = substitute(instruction, "LHPA", String.valueOf((addr >> 16) + extra));
        }
        // substitute upper 16 bits of label address after adding constant e.g. here+4($s0)
        // Address will be resolved using "ori", so DO NOT adjust upper 16 if bit 15 is 1.
        // This only happens in the "la" (load address) instruction.
        if (instruction.contains("LHPN")) {
            // Label has already been translated to address by symtab lookup
            String label = tokenList.get(2).getValue();  // 2 is only possible token position
            String addend = tokenList.get(4).getValue();  // 4 is only possible token position
            int addr = 0;
            try {
                addr = Binary.stringToInt(label) +    // KENV   1/6/05
                        Binary.stringToInt(addend);
            } catch (NumberFormatException e) {
                // this won't happen...
            }
            instruction = substitute(instruction, "LHPN", String.valueOf(addr >> 16));
        }
        // substitute lower 16 bits of label address after adding immediate value e.g. here+44($s0)
        // and also adding the digit following LLPP in the spec.
        if ((index = instruction.indexOf("LLPP")) >= 0) {
            // label has already been translated to address by symtab lookup.
            String label = tokenList.get(2).getValue(); // 2 is only possible token position
            String addend = tokenList.get(4).getValue(); // 4 is only possible token position
            int addr = 0;
            int add = instruction.charAt(index + 4) - 48; // extract digit following P
            try {
                addr = Binary.stringToInt(label) +    // KENV   1/6/05
                        Binary.stringToInt(addend) + add;

            } catch (NumberFormatException e) {
                // this won't happen...
            }
            instruction = substitute(instruction, "LLPP" + add, String.valueOf(addr << 16 >> 16));//addr & 0xffff));
        }
        // substitute lower 16 bits of label address after adding immediate value e.g. here+44($s0)
        // NOTE: format LLPPm is recognized and substituted by the code above
        if ((index = instruction.indexOf("LLP")) >= 0) {
            // label has already been translated to address by symtab lookup.
            String label = tokenList.get(2).getValue(); // 2 is only possible token position
            String addend = tokenList.get(4).getValue(); // 4 is only possible token position
            int addr = 0;
            try {
                addr = Binary.stringToInt(label) +    // KENV   1/6/05
                        Binary.stringToInt(addend);

            } catch (NumberFormatException e) {
                // this won't happen...
            }
            if ((instruction.length() > index + 3) && (instruction.charAt(index + 3) == 'U')) {
                instruction = substitute(instruction, "LLPU", String.valueOf(addr & 0xffff));
            } else {
                instruction = substitute(instruction, "LLP", String.valueOf(addr << 16 >> 16));//addr & 0xffff));
            }
        }
        // 23-Jan-2008 DPS.  Substitute correct constant branch offset depending on whether or not
        // delayed branching is enabled. BROFF is followed by 2 digits.  The first is branch offset
        // to substitute if delayed branching is DISABLED, second is offset if ENABLED.
        if ((index = instruction.indexOf("BROFF")) >= 0) {
            try {
                String disabled = instruction.substring(index + 5, index + 6);
                String enabled = instruction.substring(index + 6, index + 7);
                instruction = substitute(instruction, "BROFF" + disabled + enabled,
                        Globals.getSettings().getBooleanSetting(Settings.DELAYED_BRANCHING_ENABLED) ? enabled : disabled);
            } catch (IndexOutOfBoundsException iooe) {
                instruction = substitute(instruction, "BROFF", "BAD_PSEUDO_OP_SPEC");
            }
        }
        // substitute Next higher Register for registers in token list (for "mfc1.d","mtc1.d")
        if (instruction.contains("NR")) {
            for (int op = 1; op < tokenList.size(); op++) {
                String token = tokenList.get(op).getValue();
                int regNumber;
                try { // if token is a RegisterFile register, substitute next higher register
                    regNumber = RegisterFile.getUserRegister(token).getNumber();
                    if (regNumber >= 0) {
                        instruction = substitute(instruction, "NR" + op, "x" + (regNumber + 1));
                    }
                } catch (NullPointerException e) { // not in RegisterFile, must be Coprocessor1 register
                    regNumber = Coprocessor1.getRegisterNumber(token);
                    if (regNumber >= 0) {
                        instruction = substitute(instruction, "NR" + op, "$f" + (regNumber + 1));
                    }
                }
            }
        }

        // substitute result of subtracting last token from 32 (rol and ror constant rotate amount)
        if (instruction.contains("S32")) {
            String value = tokenList.get(tokenList.size() - 1).getValue();
            int val = 0;
            try {
                val = Binary.stringToInt(value);      // KENV   1/6/05
            } catch (NumberFormatException e) {
                // this won't happen...
            }
            instruction = substitute(instruction, "S32", Integer.toString(32 - val));
        }

        // substitute label if necessary
        if (instruction.contains("LAB")) {
            // label has to be last token.  It has already been translated to address
            // by symtab lookup, so I need to get the text label back so parseLine() won't puke.
            String label = tokenList.get(tokenList.size() - 1).getValue();
            Symbol sym = program.getLocalSymbolTable().getSymbolGivenAddressLocalOrGlobal(label);
            if (sym != null) {
                // should never be null, since there would not be an address if label were not in symtab!
                // DPS 9 Dec 2007: The "substitute()" method will substitute for ALL matches.  Here
                // we want to substitute only for the first match, for two reasons: (1) a statement
                // can only contain one label reference, its last operand, and (2) If the user's label
                // contains the substring "LAB", then substitute() will go into an infinite loop because
                // it will keep matching the substituted string!
                instruction = substituteFirst(instruction, "LAB", sym.getName());
            }
        }
        return instruction;
    }

    // Performs a String substitution.  Java 1.5 adds an overloaded String.replace method to
    // do this directly but I wanted to stay 1.4 compatible.
    // Modified 12 July 2006 to "substitute all occurances", not just the first.
    private static String substitute(String original, String find, String replacement) {
        if (!original.contains(find) || find.equals(replacement)) {
            return original;  // second condition prevents infinite loop below
        }
        int i;
        String modified = original;
        while ((i = modified.indexOf(find)) >= 0) {
            modified = modified.substring(0, i) + replacement + modified.substring(i + find.length());
        }
        return modified;
    }

    // Performs a String substitution, but will only substitute for the first match.
    // Java 1.5 adds an overloaded String.replace method to do this directly but I
    // wanted to stay 1.4 compatible.
    private static String substituteFirst(String original, String find, String replacement) {
        if (!original.contains(find) || find.equals(replacement)) {
            return original;  // second condition prevents infinite loop below
        }
        int i;
        String modified = original;
        if ((i = modified.indexOf(find)) >= 0) {
            modified = modified.substring(0, i) + replacement + modified.substring(i + find.length());
        }
        return modified;
    }


    // Takes list of basic instructions that this extended instruction
    // expands to, which is a string, and breaks out into separate
    // instructions.  They are separated by '\n' character.

    private ArrayList<String> buildTranslationList(String translation) {
        if (translation == null || translation.length() == 0) {
            return null;
        }
        ArrayList<String> translationList = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(translation, "\n");
        while (st.hasMoreTokens()) {
            translationList.add(st.nextToken());
        }
        return translationList;
    }


    /*
     * Get length in bytes that this extended instruction requires in its 
     * binary form. The answer depends on how many basic instructions it 
     * expands to.  This may vary, if expansion includes a nop, depending on
     * whether or not delayed branches are enabled. Each requires 4 bytes.
     * Returns length in bytes of corresponding binary instruction(s).
     * Returns 0 if the ArrayList is null or empty.
     */
    private int getInstructionLength(ArrayList<String> translationList) {
        if (translationList == null || translationList.size() == 0) {
            return 0;
        }
        // If instruction template is DBNOP, that means generate a "nop" instruction but only
        // if Delayed branching is enabled.  Otherwise generate nothing.  If generating nothing,
        // then don't count the nop in the instruction length.   DPS 23-Jan-2008
        int instructionCount = 0;
        for (String str : translationList) {
            if (str.contains("DBNOP") && !Globals.getSettings().getBooleanSetting(Settings.DELAYED_BRANCHING_ENABLED))
                continue;
            instructionCount++;
        }
        return 4 * instructionCount;
    }
}