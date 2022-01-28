package rars.assembler;

import rars.ErrorList;
import rars.ErrorMessage;
import rars.riscv.Instruction;

import java.util.ArrayList;

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
 * Provides utility method related to operand formats.
 *
 * @author Pete Sanderson
 * @version August 2003
 */


public class OperandFormat {
   /**
    * Syntax test for correct match in both numbers and types of operands.
    * 
    * @param candidateList List of tokens generated from programmer's MIPS statement.
    * @param inst The (presumably best matched) RISCV instruction.
    * @param errors ErrorList into which any error messages generated here will be added.
    * 
    * @return Returns <tt>true</tt> if the programmer's statement matches the MIPS 
    * specification, else returns <tt>false</tt>.
    */

    static boolean tokenOperandMatch(TokenList candidateList, Instruction inst, ErrorList errors) {
        return numOperandsCheck(candidateList, inst, errors) && operandTypeCheck(candidateList, inst, errors);
    }

    /**
     * If candidate operator token matches more than one instruction mnemonic, then select
     * first such Instruction that has an exact operand match.  If none match,
     * return the first Instruction and let client deal with operand mismatches.
     */
    static Instruction bestOperandMatch(TokenList tokenList, ArrayList<Instruction> instrMatches) {
        if (instrMatches == null)
            return null;
        if (instrMatches.size() == 1)
            return instrMatches.get(0);
        for (Instruction potentialMatch : instrMatches) {
            if (tokenOperandMatch(tokenList, potentialMatch, new ErrorList()))
                return potentialMatch;
        }
        return instrMatches.get(0);
    }

    // Simply check to see if numbers of operands are correct and generate error message if not.
    private static boolean numOperandsCheck(TokenList cand, Instruction spec, ErrorList errors) {
        int numOperands = cand.size() - 1;
        int reqNumOperands = spec.getTokenList().size() - 1;
        Token operator = cand.get(0);
        if (numOperands == reqNumOperands) {
            return true;
        } else if (numOperands < reqNumOperands) {

            String mess = "Too few or incorrectly formatted operands. Expected: " + spec.getExampleFormat();
            generateMessage(operator, mess, errors);
        } else {
            String mess = "Too many or incorrectly formatted operands. Expected: " + spec.getExampleFormat();
            generateMessage(operator, mess, errors);
        }
        return false;
    }

    // Generate error message if operand is not of correct type for this operation & operand position
    private static boolean operandTypeCheck(TokenList cand, Instruction spec, ErrorList errors) {
        Token candToken, specToken;
        TokenTypes candType, specType;
        for (int i = 1; i < spec.getTokenList().size(); i++) {
            candToken = cand.get(i);
            specToken = spec.getTokenList().get(i);
            candType = candToken.getType();
            specType = specToken.getType();
            // Type mismatch is error EXCEPT when (1) spec calls for register name and candidate is
            // register number, (2) spec calls for register number, candidate is register name and
            // names are permitted, (3)spec calls for integer of specified max bit length and
            // candidate is integer of smaller bit length.
            // Type match is error when spec calls for register name, candidate is register name, and
            // names are not permitted.

            // added 2-July-2010 DPS
            // Not an error if spec calls for identifier and candidate is operator, since operator names can be used as labels.
            // TODO: maybe add more cases in here
            if (specType == TokenTypes.IDENTIFIER && candType == TokenTypes.OPERATOR) {
                Token replacement = new Token(TokenTypes.IDENTIFIER, candToken.getValue(), candToken.getSourceProgram(), candToken.getSourceLine(), candToken.getStartPos());
                cand.set(i, replacement);
                continue;
            }
            // end 2-July-2010 addition

            if ((specType == TokenTypes.REGISTER_NAME || specType == TokenTypes.REGISTER_NUMBER) &&
                    candType == TokenTypes.REGISTER_NAME) {
                    continue;
            }
            if (specType == TokenTypes.REGISTER_NAME &&
                    candType == TokenTypes.REGISTER_NUMBER)
                continue;
            if(specType == TokenTypes.CSR_NAME &&
                    (candType==TokenTypes.INTEGER_5 || candType==TokenTypes.INTEGER_6 || candType == TokenTypes.INTEGER_12
                            || candType ==TokenTypes.INTEGER_12U || candType == TokenTypes.CSR_NAME))
                continue;
            if ((specType == TokenTypes.INTEGER_6 && candType == TokenTypes.INTEGER_5) ||
                    (specType == TokenTypes.INTEGER_12 && candType == TokenTypes.INTEGER_5) ||
                    (specType == TokenTypes.INTEGER_20 && candType == TokenTypes.INTEGER_5) ||
                    (specType == TokenTypes.INTEGER_12 && candType == TokenTypes.INTEGER_6) ||
                    (specType == TokenTypes.INTEGER_20 && candType == TokenTypes.INTEGER_6) ||
                    (specType == TokenTypes.INTEGER_20 && candType == TokenTypes.INTEGER_12) ||
                    (specType == TokenTypes.INTEGER_20 && candType == TokenTypes.INTEGER_12U) ||
                    (specType == TokenTypes.INTEGER_32 && candType == TokenTypes.INTEGER_5) ||
                    (specType == TokenTypes.INTEGER_32 && candType == TokenTypes.INTEGER_6) ||
                    (specType == TokenTypes.INTEGER_32 && candType == TokenTypes.INTEGER_12) ||
                    (specType == TokenTypes.INTEGER_32 && candType == TokenTypes.INTEGER_12U) ||
                    (specType == TokenTypes.INTEGER_32 && candType == TokenTypes.INTEGER_20))
                continue;
            if (specType == TokenTypes.INTEGER_12 && candType == TokenTypes.INTEGER_12U) {
                generateMessage(candToken, "Unsigned value is too large to fit into a sign-extended immediate", errors);
                return false;
            }
            if ((specType == TokenTypes.INTEGER_5 && candType == TokenTypes.INTEGER_6) ||
                    (specType == TokenTypes.INTEGER_5 && candType == TokenTypes.INTEGER_12) ||
                    (specType == TokenTypes.INTEGER_5 && candType == TokenTypes.INTEGER_12U) ||
                    (specType == TokenTypes.INTEGER_5 && candType == TokenTypes.INTEGER_20) ||
                    (specType == TokenTypes.INTEGER_5 && candType == TokenTypes.INTEGER_32) ||
                    (specType == TokenTypes.INTEGER_6 && candType == TokenTypes.INTEGER_12) ||
                    (specType == TokenTypes.INTEGER_6 && candType == TokenTypes.INTEGER_12U) ||
                    (specType == TokenTypes.INTEGER_6 && candType == TokenTypes.INTEGER_20) ||
                    (specType == TokenTypes.INTEGER_6 && candType == TokenTypes.INTEGER_32) ||
                    (specType == TokenTypes.INTEGER_12 && candType == TokenTypes.INTEGER_20) ||
                    (specType == TokenTypes.INTEGER_12 && candType == TokenTypes.INTEGER_32) ||
                    (specType == TokenTypes.INTEGER_20 && candType == TokenTypes.INTEGER_32)) {
                generateMessage(candToken, "operand is out of range", errors);
                return false;
            }
            if (candType != specType) {
                generateMessage(candToken, "operand is of incorrect type", errors);
                return false;
            }
        }

        return true;
    }

    // Handy utility for all parse errors...
    private static void generateMessage(Token token, String mess, ErrorList errors) {
        errors.add(new ErrorMessage(token.getSourceProgram(), token.getSourceLine(), token.getStartPos(),
                "\"" + token.getValue() + "\": " + mess));
    }

}
