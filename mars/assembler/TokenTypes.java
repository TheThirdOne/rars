package mars.assembler;

import mars.Globals;
import mars.riscv.hardware.Coprocessor1;
import mars.riscv.hardware.Register;
import mars.riscv.hardware.RegisterFile;
import mars.util.Binary;

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
 * Constants to identify the types of tokens found in MIPS programs.  If Java had
 * enumerated types, that's how these would probably be implemented.
 *
 * @author Pete Sanderson
 * @version August 2003
 **/

public final class TokenTypes {

    public static final String TOKEN_DELIMITERS = "\t ,()";
    public static final TokenTypes COMMENT = new TokenTypes("COMMENT");
    public static final TokenTypes DIRECTIVE = new TokenTypes("DIRECTIVE");
    public static final TokenTypes OPERATOR = new TokenTypes("OPERATOR");
    public static final TokenTypes DELIMITER = new TokenTypes("DELIMITER");
    /**
     * note: REGISTER_NAME is token of form $zero whereas REGISTER_NUMBER is token
     * of form $0.  The former is part of extended assembler, and latter is part
     * of basic assembler.
     **/
    public static final TokenTypes REGISTER_NAME = new TokenTypes("REGISTER_NAME"); // mnemonic
    public static final TokenTypes REGISTER_NUMBER = new TokenTypes("REGISTER_NUMBER");
    public static final TokenTypes FP_REGISTER_NAME = new TokenTypes("FP_REGISTER_NAME");
    public static final TokenTypes IDENTIFIER = new TokenTypes("IDENTIFIER");
    public static final TokenTypes LEFT_PAREN = new TokenTypes("LEFT_PAREN");
    public static final TokenTypes RIGHT_PAREN = new TokenTypes("RIGHT_PAREN");
    //public static final TokenTypes INTEGER       = new TokenTypes("INTEGER");
    public static final TokenTypes INTEGER_5 = new TokenTypes("INTEGER_5");
    public static final TokenTypes INTEGER_12 = new TokenTypes("INTEGER_12");
    public static final TokenTypes INTEGER_20 = new TokenTypes("INTEGER_20");
    public static final TokenTypes INTEGER_32 = new TokenTypes("INTEGER_32");
    public static final TokenTypes REAL_NUMBER = new TokenTypes("REAL_NUMBER");
    public static final TokenTypes QUOTED_STRING = new TokenTypes("QUOTED_STRING");
    public static final TokenTypes PLUS = new TokenTypes("PLUS");
    public static final TokenTypes MINUS = new TokenTypes("MINUS");
    public static final TokenTypes COLON = new TokenTypes("COLON");
    public static final TokenTypes ERROR = new TokenTypes("ERROR");
    public static final TokenTypes MACRO_PARAMETER = new TokenTypes("MACRO_PARAMETER");

    private String descriptor;

    private TokenTypes() {
        // private ctor assures no objects can be created other than those above.
        descriptor = "generic";
    }

    private TokenTypes(String name) {
        descriptor = name;
    }

    /**
     * Produces String equivalent of this token type, which is its name.
     *
     * @return String containing descriptive name for token type.
     **/
    public String toString() {
        return descriptor;
    }

    /**
     * Classifies the given token into one of the MIPS types.
     *
     * @param value String containing candidate language element, extracted from MIPS program.
     * @return Returns the corresponding TokenTypes object if the parameter matches a
     * defined MIPS token type, else returns <tt>null</tt>.
     **/

    public static TokenTypes matchTokenType(String value) {

        TokenTypes type = null;
        // If it starts with single quote ('), it is a mal-formed character literal
        // because a well-formed character literal was converted to string-ified
        // integer before getting here...
        if (value.charAt(0) == '\'')
            return TokenTypes.ERROR;

        // See if it is a comment
        if (value.charAt(0) == '#')
            return TokenTypes.COMMENT;

        // See if it is one of the simple tokens
        if (value.length() == 1) {
            switch (value.charAt(0)) {
                case '(':
                    return TokenTypes.LEFT_PAREN;
                case ')':
                    return TokenTypes.RIGHT_PAREN;
                case ':':
                    return TokenTypes.COLON;
                case '+':
                    return TokenTypes.PLUS;
                case '-':
                    return TokenTypes.MINUS;
            }
        }

        // See if it is a macro parameter
        if (Macro.tokenIsMacroParameter(value, false))
            return TokenTypes.MACRO_PARAMETER;

        // See if it is a register
        Register reg = RegisterFile.getRegister(value);
        if (reg != null)
            if (reg.getName().equals(value))
                return TokenTypes.REGISTER_NAME;
            else
                return TokenTypes.REGISTER_NUMBER;

        // See if it is a floating point register

        reg = Coprocessor1.getRegister(value);
        if (reg != null)
            return TokenTypes.FP_REGISTER_NAME;

        // See if it is an immediate (constant) integer value
        // Classify based on # bits needed to represent in binary
        // This is needed because most immediate operands limited to 16 bits
        // others limited to 5 bits unsigned (shift amounts) others 32 bits.
        try {

            int i = Binary.stringToInt(value);   // KENV 1/6/05

            /* **************************************************************************
             *  MODIFICATION AND COMMENT, DPS 3-July-2008
             *
             * The modifications of January 2005 documented below are being rescinded.
             * All hexadecimal immediate values are considered 32 bits in length and
             * their classification as INTEGER_5, INTEGER_16, INTEGER_16U (new)
             * or INTEGER_32 depends on their 32 bit value.  So 0xFFFF will be
             * equivalent to 0x0000FFFF instead of 0xFFFFFFFF.  This change, along with
             * the introduction of INTEGER_16U (adopted from Greg Gibeling of Berkeley),
             * required extensive changes to instruction templates especially for
             * pseudo-instructions.
             *
             * This modification also appears inbuildBasicStatementFromBasicInstruction()
             * in mars.ProgramStatement.
             *
             *  ///// Begin modification 1/4/05 KENV   ///////////////////////////////////////////
             *  // We have decided to interpret non-signed (no + or -) 16-bit hexadecimal immediate
             *  // operands as signed values in the range -32768 to 32767. So 0xffff will represent
             *  // -1, not 65535 (bit 15 as sign bit), 0x8000 will represent -32768 not 32768.
             *  // NOTE: 32-bit hexadecimal immediate operands whose values fall into this range
             *  // will be likewise affected, but they are used only in pseudo-instructions.  The
             *  // code in ExtendedInstruction.java to split this number into upper 16 bits for "lui"
             *  // and lower 16 bits for "ori" works with the original source code token, so it is
             *  // not affected by this tweak.  32-bit immediates in data segment directives
             *  // are also processed elsewhere so are not affected either.
             *  ////////////////////////////////////////////////////////////////////////////////
             *
             *     if ( Binary.isHex(value) &&
             *         (i >= 32768) &&
             *         (i <= 65535) )  // Range 0x8000 ... 0xffff
             *     {
             *          // Subtract the 0xffff bias, because strings in the
             *          // range "0x8000" ... "0xffff" are used to represent
             *          // 16-bit negative numbers, not positive numbers.
             *        i = i - 65536;
             *     }
             *    // ------------- END    KENV 1/4/05   MODIFICATIONS --------------
             *
             * *************************  END DPS 3-July-2008 COMMENTS *******************************/
            // shift operands must be in range 0-31
            if (i >= 0 && i <= 31) {
                return TokenTypes.INTEGER_5;
            }
            if (i >= DataTypes.MIN_IMMEDIATE_VALUE && i <= DataTypes.MAX_IMMEDIATE_VALUE) {
                return TokenTypes.INTEGER_12;
            }
            if (i >= DataTypes.MIN_UPPER_VALUE && i <= DataTypes.MAX_UPPER_VALUE) {
                return TokenTypes.INTEGER_20;
            }
            return TokenTypes.INTEGER_32;  // default when no other type is applicable
        } catch (NumberFormatException e) {
            // NO ACTION -- exception suppressed
        }

        // See if it is a real (fixed or floating point) number.  Note that parseDouble()
        // accepts integer values but if it were an integer literal we wouldn't get this far.
        try {
            Double.parseDouble(value);
            return TokenTypes.REAL_NUMBER;
        } catch (NumberFormatException e) {
            // NO ACTION -- exception suppressed
        }

        // See if it is an instruction operator
        if (Globals.instructionSet.matchOperator(value) != null)
            return TokenTypes.OPERATOR;

        // See if it is a directive
        if (value.charAt(0) == '.' && Directives.matchDirective(value) != null) {
            return TokenTypes.DIRECTIVE;
        }

        // See if it is a quoted string
        if (value.charAt(0) == '"')
            return TokenTypes.QUOTED_STRING;

        // Test for identifier goes last because I have defined tokens for various
        // MIPS constructs (such as operators and directives) that also could fit
        // the lexical specifications of an identifier, and those need to be
        // recognized first.
        if (isValidIdentifier(value))
            return TokenTypes.IDENTIFIER;

        // Matches no MIPS language token.
        return TokenTypes.ERROR;
    }

    /**
     * Lets you know if given tokentype is for integers (INTGER_5, INTEGER_16, INTEGER_32).
     *
     * @param type the TokenType of interest
     * @return true if type is an integer type, false otherwise.
     **/
    public static boolean isIntegerTokenType(TokenTypes type) {
        return type == TokenTypes.INTEGER_5 || type == TokenTypes.INTEGER_12 ||
                type == TokenTypes.INTEGER_20 || type == TokenTypes.INTEGER_32;
    }


    /**
     * Lets you know if given tokentype is for floating point numbers (REAL_NUMBER).
     *
     * @param type the TokenType of interest
     * @return true if type is an floating point type, false otherwise.
     **/
    public static boolean isFloatingTokenType(TokenTypes type) {
        return type == TokenTypes.REAL_NUMBER;
    }


    // COD2, A-51:  "Identifiers are a sequence of alphanumeric characters,
    //               underbars (_), and dots (.) that do not begin with a number."
    // Ideally this would be in a separate Identifier class but I did not see an immediate
    // need beyond this method (refactoring effort would probably identify other uses
    // related to symbol table).
    //
    // DPS 14-Jul-2008: added '$' as valid symbol.  Permits labels to include $.
    //                  MIPS-target GCC will produce labels that start with $.
    public static boolean isValidIdentifier(String value) {
        boolean result =
                (Character.isLetter(value.charAt(0)) || value.charAt(0) == '_' || value.charAt(0) == '.' || value.charAt(0) == '$');
        int index = 1;
        while (result && index < value.length()) {
            if (!(Character.isLetterOrDigit(value.charAt(index)) || value.charAt(index) == '_' || value.charAt(index) == '.' || value.charAt(index) == '$'))
                result = false;
            index++;
        }
        return result;
    }

}
