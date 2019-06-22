package rars.assembler;

import rars.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
 * A tokenizer is capable of tokenizing a complete source program, or a given line from
 * a source program.  Since RARS assembly is line-oriented, each line defines a complete statement.
 * Tokenizing is the process of analyzing the input program for the purpose of
 * recognizing each RISCV language element.  The types of language elements are known as "tokens".
 * Tokens are defined in the TokenTypes class.<br><br>
 * Example: <br>
 * The RISCV statement  <tt>here:  lw  t3, 8(t4)   #load third member of array</tt><br>
 * generates the following token list<br>
 * IDENTIFIER, COLON, OPERATOR, REGISTER_NAME, COMMA, INTEGER_5, LEFT_PAREN,
 * REGISTER_NAME, RIGHT_PAREN, COMMENT<br>
 *
 * @author Pete Sanderson
 * @version August 2003
 **/

public class Tokenizer {

    private ErrorList errors;
    private RISCVprogram sourceRISCVprogram;
    private HashMap<String, String> equivalents; // DPS 11-July-2012
    // The 8 escaped characters are: single quote, double quote, backslash, newline (linefeed),
    // tab, backspace, return, form feed.  The characters and their corresponding decimal codes:
    // TODO: potentially make this automatic
    private static final String escapedCharacters = "'\"\\ntbrf0";
    private static final String[] escapedCharactersValues = {"39", "34", "92", "10", "9", "8", "13", "12", "0"};

    /**
     * Simple constructor. Initializes empty error list.
     */

    public Tokenizer() {
        this(null);
    }

    /**
     * Constructor for use with existing RISCVprogram.  Designed to be used with Macro feature.
     *
     * @param program A previously-existing RISCVprogram object or null if none.
     */
    public Tokenizer(RISCVprogram program) {
        errors = new ErrorList();
        sourceRISCVprogram = program;
    }

    /**
     * Will tokenize a complete soure program.
     *
     * @param p The RISCVprogram to be tokenized.
     * @return An ArrayList representing the tokenized program.  Each list member is a TokenList
     * that represents a tokenized source statement from the program.
     **/

    public ArrayList<TokenList> tokenize(RISCVprogram p) throws AssemblyException {
        sourceRISCVprogram = p;
        equivalents = new HashMap<>(); // DPS 11-July-2012
        ArrayList<TokenList> tokenList = new ArrayList<>();
        //ArrayList source = p.getSourceList();
        ArrayList<SourceLine> source = processIncludes(p, new HashMap<>()); // DPS 9-Jan-2013
        p.setSourceLineList(source);
        TokenList currentLineTokens;
        String sourceLine;
        for (int i = 0; i < source.size(); i++) {
            sourceLine = source.get(i).getSource();
            currentLineTokens = this.tokenizeLine(i + 1, sourceLine);
            tokenList.add(currentLineTokens);
            // DPS 03-Jan-2013. Related to 11-July-2012. If source code substitution was made
            // based on .eqv directive during tokenizing, the processed line, a String, is
            // not the same object as the original line.  Thus I can use != instead of !equals()
            // This IF statement will replace original source with source modified by .eqv substitution.
            // Not needed by assembler, but looks better in the Text Segment Display.
            if (sourceLine.length() > 0 && sourceLine != currentLineTokens.getProcessedLine()) {
                source.set(i, new SourceLine(currentLineTokens.getProcessedLine(), source.get(i).getRISCVprogram(), source.get(i).getLineNumber()));
            }
        }
        if (errors.errorsOccurred()) {
            throw new AssemblyException(errors);
        }
        return tokenList;
    }


    // pre-pre-processing pass through source code to process any ".include" directives.
    // When one is encountered, the contents of the included file are inserted at that
    // point.  If no .include statements, the return value is a new array list but
    // with the same lines of source code.  Uses recursion to correctly process included
    // files that themselves have .include.  Plus it will detect and report recursive
    // includes both direct and indirect.
    // DPS 11-Jan-2013
    private ArrayList<SourceLine> processIncludes(RISCVprogram program, Map<String, String> inclFiles) throws AssemblyException {
        ArrayList<String> source = program.getSourceList();
        ArrayList<SourceLine> result = new ArrayList<>(source.size());
        for (int i = 0; i < source.size(); i++) {
            String line = source.get(i);
            TokenList tl = tokenizeLine(program, i + 1, line, false);
            boolean hasInclude = false;
            for (int ii = 0; ii < tl.size(); ii++) {
                if (tl.get(ii).getValue().equalsIgnoreCase(Directives.INCLUDE.getName())
                        && (tl.size() > ii + 1)
                        && tl.get(ii + 1).getType() == TokenTypes.QUOTED_STRING) {
                    String filename = tl.get(ii + 1).getValue();
                    filename = filename.substring(1, filename.length() - 1); // get rid of quotes
                    // Handle either absolute or relative pathname for .include file
                    if (!new File(filename).isAbsolute()) {
                        filename = new File(program.getFilename()).getParent() + File.separator + filename;
                    }
                    if (inclFiles.containsKey(filename)) {
                        // This is a recursive include.  Generate error message and return immediately.
                        Token t = tl.get(ii + 1);
                        errors.add(new ErrorMessage(program, t.getSourceLine(), t.getStartPos(),
                                "Recursive include of file " + filename));
                        throw new AssemblyException(errors);
                    }
                    inclFiles.put(filename, filename);
                    RISCVprogram incl = new RISCVprogram();
                    try {
                        incl.readSource(filename);
                    } catch (AssemblyException p) {
                        Token t = tl.get(ii + 1);
                        errors.add(new ErrorMessage(program, t.getSourceLine(), t.getStartPos(),
                                "Error reading include file " + filename));
                        throw new AssemblyException(errors);
                    }
                    ArrayList<SourceLine> allLines = processIncludes(incl, inclFiles);
                    result.addAll(allLines);
                    hasInclude = true;
                    break;
                }
            }
            if (!hasInclude) {
                result.add(new SourceLine(line, program, i + 1));//line);
            }
        }
        return result;
    }

    /**
     * Used only to create a token list for the example provided with each instruction
     * specification.
     *
     * @param example The example RISCV instruction to be tokenized.
     * @return An TokenList representing the tokenized instruction.  Each list member is a Token
     * that represents one language element.
     * @throws AssemblyException This occurs only if the instruction specification itself
     *                           contains one or more lexical (i.e. token) errors.
     **/

    public TokenList tokenizeExampleInstruction(String example) throws AssemblyException {
        TokenList result = tokenizeLine(sourceRISCVprogram, 0, example, false);
        if (errors.errorsOccurred()) {
            throw new AssemblyException(errors);
        }
        return result;
    }


    /**
     * Will tokenize one line of source code.  If lexical errors are discovered,
     * they are noted in an ErrorMessage object which is added to the ErrorList.
     * Will NOT throw an exception yet because we want to persevere beyond first error.
     *
     * @param lineNum line number from source code (used in error message)
     * @param theLine String containing source code
     * @return the generated token list for that line
     **/
    /*
    * 
    * Tokenizing is not as easy as it appears at first blush, because the typical 
    * delimiters: space, tab, comma, can all appear inside quoted ASCII strings!
    * Also, spaces are not as necessary as they seem, the following line is accepted
    * and parsed correctly by SPIM:    label:lw,$t4,simple#comment
    * as is this weird variation:      label  :lw  $t4  ,simple ,  ,  , # comment
    * 
    * as is this line:  stuff:.asciiz"# ,\n\"","aaaaa"  (interestingly, if you put
    * additional characters after the \", they are ignored!!)
    * 
    * I also would like to know the starting character position in the line of each
    * token, for error reporting purposes.  StringTokenizer cannot give you this.
    * 
    * Given all the above, it is just as easy to "roll my own" as to use StringTokenizer
    */

    // Modified for release 4.3, to preserve existing API.
    public TokenList tokenizeLine(int lineNum, String theLine) {
        return tokenizeLine(sourceRISCVprogram, lineNum, theLine, true);
    }

    /**
     * Will tokenize one line of source code.  If lexical errors are discovered,
     * they are noted in an ErrorMessage object which is added to the provided ErrorList
     * instead of the Tokenizer's error list. Will NOT throw an exception.
     *
     * @param lineNum         line number from source code (used in error message)
     * @param theLine         String containing source code
     * @param callerErrorList errors will go into this list instead of tokenizer's list.
     * @return the generated token list for that line
     **/
    public TokenList tokenizeLine(int lineNum, String theLine, ErrorList callerErrorList) {
        ErrorList saveList = this.errors;
        this.errors = callerErrorList;
        TokenList tokens = this.tokenizeLine(lineNum, theLine);
        this.errors = saveList;
        return tokens;
    }


    /**
     * Will tokenize one line of source code.  If lexical errors are discovered,
     * they are noted in an ErrorMessage object which is added to the provided ErrorList
     * instead of the Tokenizer's error list. Will NOT throw an exception.
     *
     * @param lineNum          line number from source code (used in error message)
     * @param theLine          String containing source code
     * @param callerErrorList  errors will go into this list instead of tokenizer's list.
     * @param doEqvSubstitutes boolean param set true to perform .eqv substitutions, else false
     * @return the generated token list for that line
     **/
    public TokenList tokenizeLine(int lineNum, String theLine, ErrorList callerErrorList, boolean doEqvSubstitutes) {
        ErrorList saveList = this.errors;
        this.errors = callerErrorList;
        TokenList tokens = this.tokenizeLine(sourceRISCVprogram, lineNum, theLine, doEqvSubstitutes);
        this.errors = saveList;
        return tokens;
    }

    /**
     * Will tokenize one line of source code.  If lexical errors are discovered,
     * they are noted in an ErrorMessage object which is added to the provided ErrorList
     * instead of the Tokenizer's error list. Will NOT throw an exception.
     *
     * @param program          RISCVprogram containing this line of source
     * @param lineNum          line number from source code (used in error message)
     * @param theLine          String containing source code
     * @param doEqvSubstitutes boolean param set true to perform .eqv substitutions, else false
     * @return the generated token list for that line
     **/
    public TokenList tokenizeLine(RISCVprogram program, int lineNum, String theLine, boolean doEqvSubstitutes) {
        TokenTypes tokenType;
        TokenList result = new TokenList();
        if (theLine.length() == 0)
            return result;
        // will be faster to work with char arrays instead of strings
        char c;
        char[] line = theLine.toCharArray();
        int linePos = 0;
        char[] token = new char[line.length];
        int tokenPos = 0;
        int tokenStartPos = 1;
        boolean insideQuotedString = false;
        if (Globals.debug)
            System.out.println("source line --->" + theLine + "<---");
        // Each iteration of this loop processes one character in the source line.
        while (linePos < line.length) {
            c = line[linePos];
            if (insideQuotedString) { // everything goes into token
                token[tokenPos++] = c;
                if (c == '"' && token[tokenPos - 2] != '\\') { // If quote not preceded by backslash, this is end
                    this.processCandidateToken(token, program, lineNum, theLine, tokenPos, tokenStartPos, result);
                    tokenPos = 0;
                    insideQuotedString = false;
                }
            } else { // not inside a quoted string, so be sensitive to delimiters
                switch (c) {
                    case '#':  // # denotes comment that takes remainder of line
                        if (tokenPos > 0) {
                            this.processCandidateToken(token, program, lineNum, theLine, tokenPos, tokenStartPos, result);
                        }
                        tokenStartPos = linePos + 1;
                        tokenPos = line.length - linePos;
                        System.arraycopy(line, linePos, token, 0, tokenPos);
                        this.processCandidateToken(token, program, lineNum, theLine, tokenPos, tokenStartPos, result);
                        linePos = line.length;
                        tokenPos = 0;
                        break;
                    case ' ':
                    case '\t':
                    case ',': // space, tab or comma is delimiter
                        if (tokenPos > 0) {
                            this.processCandidateToken(token, program, lineNum, theLine, tokenPos, tokenStartPos, result);
                            tokenPos = 0;
                        }
                        break;
                    // These two guys are special.  Will be recognized as unary if and only if two conditions hold:
                    // 1. Immediately followed by a digit (will use look-ahead for this).
                    // 2. Previous token, if any, is _not_ an IDENTIFIER
                    // Otherwise considered binary and thus a separate token.  This is a slight hack but reasonable.
                    case '+':
                    case '-':
                        // Here's the REAL hack: recognizing signed exponent in E-notation floating point!
                        // (e.g. 1.2e-5) Add the + or - to the token and keep going.  DPS 17 Aug 2005
                        if (tokenPos > 0 && line.length >= linePos + 2 && Character.isDigit(line[linePos + 1]) &&
                                (line[linePos - 1] == 'e' || line[linePos - 1] == 'E')) {
                            token[tokenPos++] = c;
                            break;
                        }
                        // End of REAL hack.
                        if (tokenPos > 0) {
                            this.processCandidateToken(token, program, lineNum, theLine, tokenPos, tokenStartPos, result);
                            tokenPos = 0;
                        }

                        tokenStartPos = linePos + 1;
                        token[tokenPos++] = c;
                        if (line.length > linePos + 3 && line[linePos + 1] == 'I' && line[linePos + 2] == 'n' && line[linePos + 3] == 'f') {
                            result.add(new Token(TokenTypes.REAL_NUMBER, "-Inf", program, lineNum, tokenStartPos));
                            linePos += 3;
                            tokenPos = 0;
                            break;
                        }
                        if (!((result.isEmpty() || result.get(result.size() - 1).getType() != TokenTypes.IDENTIFIER) &&
                                (line.length >= linePos + 2 && Character.isDigit(line[linePos + 1])))) {
                            // treat it as binary.....
                            this.processCandidateToken(token, program, lineNum, theLine, tokenPos, tokenStartPos, result);
                            tokenPos = 0;
                        }
                        break;
                    // these are other single-character tokens
                    case ':':
                    case '(':
                    case ')':
                        if (tokenPos > 0) {
                            this.processCandidateToken(token, program, lineNum, theLine, tokenPos, tokenStartPos, result);
                            tokenPos = 0;
                        }
                        tokenStartPos = linePos + 1;
                        token[tokenPos++] = c;
                        this.processCandidateToken(token, program, lineNum, theLine, tokenPos, tokenStartPos, result);
                        tokenPos = 0;
                        break;
                    case '"': // we're not inside a quoted string, so start a new token...
                        if (tokenPos > 0) {
                            this.processCandidateToken(token, program, lineNum, theLine, tokenPos, tokenStartPos, result);
                            tokenPos = 0;
                        }
                        tokenStartPos = linePos + 1;
                        token[tokenPos++] = c;
                        insideQuotedString = true;
                        break;
                    case '\'': // start of character constant (single quote).
                        if (tokenPos > 0) {
                            this.processCandidateToken(token, program, lineNum, theLine, tokenPos, tokenStartPos, result);
                            tokenPos = 0;
                        }
                        // Our strategy is to process the whole thing right now...
                        tokenStartPos = linePos + 1;
                        token[tokenPos++] = c; // Put the quote in token[0]
                        int lookaheadChars = line.length - linePos - 1;
                        // need minimum 2 more characters, 1 for char and 1 for ending quote
                        if (lookaheadChars < 2)
                            break;  // gonna be an error
                        c = line[++linePos];
                        token[tokenPos++] = c; // grab second character, put it in token[1]
                        if (c == '\'')
                            break; // gonna be an error: nothing between the quotes
                        c = line[++linePos];
                        token[tokenPos++] = c; // grab third character, put it in token[2]
                        // Process if we've either reached second, non-escaped, quote or end of line.
                        if (c == '\'' && token[1] != '\\' || lookaheadChars == 2) {
                            this.processCandidateToken(token, program, lineNum, theLine, tokenPos, tokenStartPos, result);
                            tokenPos = 0;
                            tokenStartPos = linePos + 1;
                            break;
                        }
                        // At this point, there is at least one more character on this line. If we're
                        // still here after seeing a second quote, it was escaped.  Not done yet;
                        // we either have an escape code, an octal code (also escaped) or invalid.
                        c = line[++linePos];
                        token[tokenPos++] = c; // grab fourth character, put it in token[3]
                        // Process, if this is ending quote for escaped character or if at end of line
                        if (c == '\'' || lookaheadChars == 3) {
                            this.processCandidateToken(token, program, lineNum, theLine, tokenPos, tokenStartPos, result);
                            tokenPos = 0;
                            tokenStartPos = linePos + 1;
                            break;
                        }
                        // At this point, we've handled all legal possibilities except octal, e.g. '\377'
                        // Proceed, if enough characters remain to finish off octal.
                        if (lookaheadChars >= 5) {
                            c = line[++linePos];
                            token[tokenPos++] = c;  // grab fifth character, put it in token[4]
                            if (c != '\'') {
                                // still haven't reached end, last chance for validity!
                                c = line[++linePos];
                                token[tokenPos++] = c;  // grab sixth character, put it in token[5]
                            }
                        }
                        // process no matter what...we either have a valid character by now or not
                        this.processCandidateToken(token, program, lineNum, theLine, tokenPos, tokenStartPos, result);
                        tokenPos = 0;
                        tokenStartPos = linePos + 1;
                        break;
                    default:
                        if (tokenPos == 0)
                            tokenStartPos = linePos + 1;
                        token[tokenPos++] = c;
                        break;
                }  // switch
            } // if (insideQuotedString)
            linePos++;
        }  // while
        if (tokenPos > 0) {
            if(insideQuotedString){
                errors.add(new ErrorMessage(program, lineNum, tokenStartPos,
                        "String is not terminated."));
            }
            this.processCandidateToken(token, program, lineNum, theLine, tokenPos, tokenStartPos, result);
            tokenPos = 0;
        }
        if (doEqvSubstitutes) {
            result = processEqv(program, lineNum, theLine, result); // DPS 11-July-2012
        }
        return result;
    }

    // Process the .eqv directive, which needs to be applied prior to tokenizing of subsequent statements.
    // This handles detecting that theLine contains a .eqv directive, in which case it needs
    // to be added to the HashMap of equivalents.  It also handles detecting that theLine
    // contains a symbol that was previously defined in an .eqv directive, in which case
    // the substitution needs to be made.
    // DPS 11-July-2012
    private TokenList processEqv(RISCVprogram program, int lineNum, String theLine, TokenList tokens) {
        // See if it is .eqv directive.  If so, record it...
        // Have to assure it is a well-formed statement right now (can't wait for assembler).

        if (tokens.size() > 2 && (tokens.get(0).getType() == TokenTypes.DIRECTIVE || tokens.get(2).getType() == TokenTypes.DIRECTIVE)) {
            // There should not be a label but if there is, the directive is in token position 2 (ident, colon, directive).
            int dirPos = (tokens.get(0).getType() == TokenTypes.DIRECTIVE) ? 0 : 2;
            if (Directives.matchDirective(tokens.get(dirPos).getValue()) == Directives.EQV) {
                // Get position in token list of last non-comment token
                int tokenPosLastOperand = tokens.size() - ((tokens.get(tokens.size() - 1).getType() == TokenTypes.COMMENT) ? 2 : 1);
                // There have to be at least two non-comment tokens beyond the directive
                if (tokenPosLastOperand < dirPos + 2) {
                    errors.add(new ErrorMessage(program, lineNum, tokens.get(dirPos).getStartPos(),
                            "Too few operands for " + Directives.EQV.getName() + " directive"));
                    return tokens;
                }
                // Token following the directive has to be IDENTIFIER
                if (tokens.get(dirPos + 1).getType() != TokenTypes.IDENTIFIER) {
                    errors.add(new ErrorMessage(program, lineNum, tokens.get(dirPos).getStartPos(),
                            "Malformed " + Directives.EQV.getName() + " directive"));
                    return tokens;
                }
                String symbol = tokens.get(dirPos + 1).getValue();
                // Make sure the symbol is not contained in the expression.  Not likely to occur but if left
                // undetected it will result in infinite recursion.  e.g.  .eqv ONE, (ONE)
                for (int i = dirPos + 2; i < tokens.size(); i++) {
                    if (tokens.get(i).getValue().equals(symbol)) {
                        errors.add(new ErrorMessage(program, lineNum, tokens.get(dirPos).getStartPos(),
                                "Cannot substitute " + symbol + " for itself in " + Directives.EQV.getName() + " directive"));
                        return tokens;
                    }
                }
                // Expected syntax is symbol, expression.  I'm allowing the expression to comprise
                // multiple tokens, so I want to get everything from the IDENTIFIER to either the
                // COMMENT or to the end.
                int startExpression = tokens.get(dirPos + 2).getStartPos();
                int endExpression = tokens.get(tokenPosLastOperand).getStartPos() + tokens.get(tokenPosLastOperand).getValue().length();
                String expression = theLine.substring(startExpression - 1, endExpression - 1);
                // Symbol cannot be redefined - the only reason for this is to act like the Gnu .eqv
                if (equivalents.containsKey(symbol) && !equivalents.get(symbol).equals(expression)) {
                    errors.add(new ErrorMessage(program, lineNum, tokens.get(dirPos + 1).getStartPos(),
                            "\"" + symbol + "\" is already defined"));
                    return tokens;
                }
                equivalents.put(symbol, expression);
                return tokens;
            }
        }
        // Check if a substitution from defined .eqv is to be made.  If so, make one.
        boolean substitutionMade = false;
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.getType() == TokenTypes.IDENTIFIER && equivalents != null && equivalents.containsKey(token.getValue())) {
                // do the substitution
                String sub = equivalents.get(token.getValue());
                int startPos = token.getStartPos();
                theLine = theLine.substring(0, startPos - 1) + sub + theLine.substring(startPos + token.getValue().length() - 1);
                substitutionMade = true;                // one substitution per call.  If there are multiple, will catch next one on the recursion
                break;
            }
        }
        tokens.setProcessedLine(theLine); // DPS 03-Jan-2013. Related to changes of 11-July-2012.

        return (substitutionMade) ? tokenizeLine(lineNum, theLine) : tokens;
    }


    /**
     * Fetch this Tokenizer's error list.
     *
     * @return the error list
     */
    public ErrorList getErrors() {
        return errors;
    }


    // Given candidate token and its position, will classify and record it.
    private void processCandidateToken(char[] token, RISCVprogram program, int line, String theLine,
                                       int tokenPos, int tokenStartPos, TokenList tokenList) {
        String value = new String(token, 0, tokenPos);
        if (value.length() > 0 && value.charAt(0) == '\'') value = preprocessCharacterLiteral(value);
        TokenTypes type = TokenTypes.matchTokenType(value);
        if (type == TokenTypes.ERROR) {
            errors.add(new ErrorMessage(program, line, tokenStartPos,
                    theLine + "\nInvalid language element: " + value));
        }
        Token toke = new Token(type, value, program, line, tokenStartPos);
        tokenList.add(toke);
    }


    // If passed a candidate character literal, attempt to translate it into integer constant.
    // If the translation fails, return original value.
    private String preprocessCharacterLiteral(String value) {
        // must start and end with quote and have something in between
        if (value.length() < 3 || value.charAt(0) != '\'' || value.charAt(value.length() - 1) != '\'') {
            return value;
        }
        String quotesRemoved = value.substring(1, value.length() - 1);
        // if not escaped, then if one character left return its value else return original.
        if (quotesRemoved.charAt(0) != '\\') {
            return (quotesRemoved.length() == 1) ? Integer.toString((int) quotesRemoved.charAt(0)) : value;
        }
        // now we know it is escape sequence and have to decode which of the 8: ',",\,n,t,b,r,f
        if (quotesRemoved.length() == 2) {
            int escapedCharacterIndex = escapedCharacters.indexOf(quotesRemoved.charAt(1));
            return (escapedCharacterIndex >= 0) ? escapedCharactersValues[escapedCharacterIndex] : value;
        }
        // last valid possibility is 3 digit octal code 000 through 377
        if (quotesRemoved.length() == 4) {
            try {
                int intValue = Integer.parseInt(quotesRemoved.substring(1), 8);
                if (intValue >= 0 && intValue <= 255) {
                    return Integer.toString(intValue);
                }
            } catch (NumberFormatException nfe) {
            } // if not valid octal, will fall through and reject
        }
        return value;
    }
}
