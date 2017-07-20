/*
 * MIPSTokenMarker.java - MIPS Assembly token marker
 * Copyright (C) 1998, 1999 Slava Pestov, 2010 Pete Sanderson
 *
 * You may use and modify this package for any purpose. Redistribution is
 * permitted, in both source and binary form, provided that this notice
 * remains intact in all source distributions of this package.
 */

package mars.venus.editors.jeditsyntax.tokenmarker;

import mars.Settings;
import mars.assembler.Directives;
import mars.riscv.hardware.FloatingPointRegisterFile;
import mars.riscv.hardware.Register;
import mars.riscv.hardware.RegisterFile;
import mars.riscv.BasicInstruction;
import mars.riscv.Instruction;
import mars.venus.editors.jeditsyntax.KeywordMap;
import mars.venus.editors.jeditsyntax.PopupHelpItem;

import javax.swing.text.Segment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

/**
 * RISCV token marker.
 *
 * @author Pete Sanderson (2010) and Slava Pestov (1999)
 */
public class RISCVTokenMarker extends TokenMarker {
    // TODO: simplify, I think this can be simplified dramatically
    public RISCVTokenMarker() {
        this(getKeywords());
    }

    public RISCVTokenMarker(KeywordMap keywords) {
        this.keywords = keywords;
    }

    public static String[] getRISCVTokenLabels() {
        if (tokenLabels == null) {
            tokenLabels = new String[Token.ID_COUNT];
            tokenLabels[Token.COMMENT1] = "Comment";
            tokenLabels[Token.LITERAL1] = "String literal";
            tokenLabels[Token.LITERAL2] = "Character literal";
            tokenLabels[Token.LABEL] = "Label";
            tokenLabels[Token.KEYWORD1] = "Instruction";
            tokenLabels[Token.KEYWORD2] = "Assembler directive";
            tokenLabels[Token.KEYWORD3] = "Register";
            tokenLabels[Token.INVALID] = "In-progress, invalid";
            tokenLabels[Token.MACRO_ARG] = "Macro parameter";
        }
        return tokenLabels;
    }

    public static String[] getRISCVTokenExamples() {
        if (tokenExamples == null) {
            tokenExamples = new String[Token.ID_COUNT];
            tokenExamples[Token.COMMENT1] = "# Load";
            tokenExamples[Token.LITERAL1] = "\"First\"";
            tokenExamples[Token.LITERAL2] = "'\\n'";
            tokenExamples[Token.LABEL] = "main:";
            tokenExamples[Token.KEYWORD1] = "lui";
            tokenExamples[Token.KEYWORD2] = ".text";
            tokenExamples[Token.KEYWORD3] = "zero";
            tokenExamples[Token.INVALID] = "\"Regi";
            tokenExamples[Token.MACRO_ARG] = "%arg";
        }
        return tokenExamples;
    }


    public byte markTokensImpl(byte token, Segment line, int lineIndex) {
        char[] array = line.array;
        int offset = line.offset;
        lastOffset = offset;
        lastKeyword = offset;
        int length = line.count + offset;
        boolean backslash = false;

        loop:
        for (int i = offset; i < length; i++) {
            int i1 = (i + 1);

            char c = array[i];
            if (c == '\\') {
                backslash = !backslash;
                continue;
            }

            switch (token) {
                case Token.NULL:
                    switch (c) {
                        case '"':
                            doKeyword(line, i, c);
                            if (backslash)
                                backslash = false;
                            else {
                                addToken(i - lastOffset, token);
                                token = Token.LITERAL1;
                                lastOffset = lastKeyword = i;
                            }
                            break;
                        case '\'':
                            doKeyword(line, i, c);
                            if (backslash)
                                backslash = false;
                            else {
                                addToken(i - lastOffset, token);
                                token = Token.LITERAL2;
                                lastOffset = lastKeyword = i;
                            }
                            break;
                        case ':':
                      /*  Original code for ':' case, replaced 3 Aug 2010. Details below.
                        if(lastKeyword == offset)
                        { // Commenting out this IF statement permits recognition of keywords
                          // used as labels when terminated with ":".  The most common example
                          // is "b:" (where b is mnemonic for branch instruction). DPS 6-July-2010.
                          //
                          // if(doKeyword(line,i,c)) 
                          //   break;
                           backslash = false;
                           addToken(i1 - lastOffset,Token.LABEL);
                           lastOffset = lastKeyword = i1;
                        }
                        else if(doKeyword(line,i,c))
                           break;
                     	break;
                     */
                            // Replacement code 3 Aug 2010.  Will recognize label definitions when:
                            // (1) label is same as instruction name, (2) label begins after column 1,
                            // (3) there are spaces between label name and colon, (4) label is valid
                            // MIPS identifier (otherwise would catch, say, 0 (zero) in .word 0:10)
                            backslash = false;
                            //String lab = new String(array, lastOffset, i1-lastOffset-1).trim();
                            boolean validIdentifier = false;
                            try {
                                validIdentifier = mars.assembler.TokenTypes.isValidIdentifier(new String(array, lastOffset, i1 - lastOffset - 1).trim());
                            } catch (StringIndexOutOfBoundsException e) {
                                validIdentifier = false;
                            }
                            if (validIdentifier) {
                                addToken(i1 - lastOffset, Token.LABEL);
                                lastOffset = lastKeyword = i1;
                            }
                            break;
                        case '#':
                            backslash = false;
                            doKeyword(line, i, c);
                            if (length - i >= 1) {
                                addToken(i - lastOffset, token);
                                addToken(length - i, Token.COMMENT1);
                                lastOffset = lastKeyword = length;
                                break loop;
                            }
                            break;
                        default:
                            backslash = false;
                            // . and $ added 4/6/10 DPS; % added 12/12 M.Sekhavat
                            if (!Character.isLetterOrDigit(c)
                                    && c != '_' && c != '.' && c != '$' && c != '%')
                                doKeyword(line, i, c);
                            break;
                    }
                    break;
                case Token.LITERAL1:
                    if (backslash)
                        backslash = false;
                    else if (c == '"') {
                        addToken(i1 - lastOffset, token);
                        token = Token.NULL;
                        lastOffset = lastKeyword = i1;
                    }
                    break;
                case Token.LITERAL2:
                    if (backslash)
                        backslash = false;
                    else if (c == '\'') {
                        addToken(i1 - lastOffset, Token.LITERAL1);
                        token = Token.NULL;
                        lastOffset = lastKeyword = i1;
                    }
                    break;
                default:
                    throw new InternalError("Invalid state: "
                            + token);
            }
        }

        if (token == Token.NULL)
            doKeyword(line, length, '\0');

        switch (token) {
            case Token.LITERAL1:
            case Token.LITERAL2:
                addToken(length - lastOffset, Token.INVALID);
                token = Token.NULL;
                break;
            case Token.KEYWORD2:
                addToken(length - lastOffset, token);
                if (!backslash)
                    token = Token.NULL;
            default:
                addToken(length - lastOffset, token);
                break;
        }

        return token;
    }


    /**
     * Construct and return any appropriate help information for
     * the given token.
     *
     * @param token     the pertinent Token object
     * @param tokenText the source String that matched to the token
     * @return ArrayList of PopupHelpItem objects, one per match.
     */
    public ArrayList<PopupHelpItem> getTokenExactMatchHelp(Token token, String tokenText) {
        ArrayList<PopupHelpItem> matches = null;
        if (token != null && token.id == Token.KEYWORD1) {
            ArrayList<Instruction> instrMatches = mars.Globals.instructionSet.matchOperator(tokenText);
            if (instrMatches.size() > 0) {
                int realMatches = 0;
                matches = new ArrayList<>();
                for (Instruction inst : instrMatches) {
                    if (mars.Globals.getSettings().getBooleanSetting(Settings.EXTENDED_ASSEMBLER_ENABLED) || inst instanceof BasicInstruction) {
                        matches.add(new PopupHelpItem(tokenText, inst.getExampleFormat(), inst.getDescription()));
                        realMatches++;
                    }
                }
                if (realMatches == 0) {
                    matches.add(new PopupHelpItem(tokenText, tokenText, "(is not a basic instruction)"));
                }
            }
        }
        if (token != null && token.id == Token.KEYWORD2) {
            Directives dir = Directives.matchDirective(tokenText);
            if (dir != null) {
                matches = new ArrayList<>();
                matches.add(new PopupHelpItem(tokenText, dir.getName(), dir.getDescription()));
            }
        }
        return matches;
    }


    /**
     * Construct and return any appropriate help information for
     * prefix match based on current line's token list.
     *
     * @param line      String containing current line
     * @param tokenList first Token on current line (head of linked list)
     * @param token     the pertinent Token object
     * @param tokenText the source String that matched to the token in previous parameter
     * @return ArrayList of PopupHelpItem objects, one per match.
     */

    public ArrayList<PopupHelpItem> getTokenPrefixMatchHelp(String line, Token tokenList, Token token, String tokenText) {
        // CASE:  Unlikely boundary case...
        if (tokenList == null || tokenList.id == Token.END) {
            return null;
        }

        // CASE:  if current token is a comment, turn off the text.
        if (token != null && token.id == Token.COMMENT1) {
            return null;
        }

        // Let's see if the line already contains an instruction or directive.  If so, we need its token
        // text as well so we can do the match.  Also need to distinguish the case where current
        // token is also an instruction/directive (moreThanOneKeyword variable).

        Token tokens = tokenList;
        String keywordTokenText = null;
        byte keywordType = -1;
        int offset = 0;
        boolean moreThanOneKeyword = false;
        while (tokens.id != Token.END) {
            if (tokens.id == Token.KEYWORD1 || tokens.id == Token.KEYWORD2) {
                if (keywordTokenText != null) {
                    moreThanOneKeyword = true;
                    break;
                }
                keywordTokenText = line.substring(offset, offset + tokens.length);
                keywordType = tokens.id;
            }
            offset += tokens.length;
            tokens = tokens.next;
        }

        // CASE:  Current token is valid KEYWORD1 (MIPS instruction).  If this line contains a previous KEYWORD1 or KEYWORD2
        //        token, then we ignore this one and do exact match on the first one.  If it does not, there may be longer
        //        instructions for which this is a prefix, so do a prefix match on current token.
        if (token != null && token.id == Token.KEYWORD1) {
            if (moreThanOneKeyword) {
                return (keywordType == Token.KEYWORD1) ? getTextFromInstructionMatch(keywordTokenText, true)
                        : getTextFromDirectiveMatch(keywordTokenText, true);
            } else {
                return getTextFromInstructionMatch(tokenText, false);
            }
        }

        // CASE:  Current token is valid KEYWORD2 (MIPS directive).  If this line contains a previous KEYWORD1 or KEYWORD2
        //        token, then we ignore this one and do exact match on the first one.  If it does not, there may be longer
        //        directives for which this is a prefix, so do a prefix match on current token.
        if (token != null && token.id == Token.KEYWORD2) {
            if (moreThanOneKeyword) {
                return (keywordType == Token.KEYWORD1) ? getTextFromInstructionMatch(keywordTokenText, true)
                        : getTextFromDirectiveMatch(keywordTokenText, true);
            } else {
                return getTextFromDirectiveMatch(tokenText, false);
            }
        }

        // CASE: line already contains KEYWORD1 or KEYWORD2 and current token is something other
        //       than KEYWORD1 or KEYWORD2. Generate text based on exact match of that token.
        if (keywordTokenText != null) {
            if (keywordType == Token.KEYWORD1) {
                return getTextFromInstructionMatch(keywordTokenText, true);
            }
            if (keywordType == Token.KEYWORD2) {
                return getTextFromDirectiveMatch(keywordTokenText, true);
            }
        }

        // CASE:  Current token is NULL, which can be any number of things.  Think of it as being either white space
        //        or an in-progress token possibly preceded by white space.  We'll do a trim on the token.  Now there
        //        are two subcases to consider:
        //    SUBCASE: The line does not contain any KEYWORD1 or KEYWORD2 tokens but nothing remains after trimming the
        //             current token's text.  This means it consists only of white space and there is nothing more to do
        //             but return.
        //    SUBCASE: The line does not contain any KEYWORD1 or KEYWORD2 tokens.  This means we do a prefix match of
        //             of the current token to either instruction or directive names.  Easy to distinguish since
        //             directives start with "."


        if (token != null && token.id == Token.NULL) {

            String trimmedTokenText = tokenText.trim();

            // Subcase: no KEYWORD1 or KEYWORD2 but current token contains nothing but white space.  We're done.
            if (keywordTokenText == null && trimmedTokenText.length() == 0) {
                return null;
            }

            // Subcase: no KEYWORD1 or KEYWORD2.  Generate text based on prefix match of trimmed current token.
            if (keywordTokenText == null && trimmedTokenText.length() > 0) {
                if (trimmedTokenText.charAt(0) == '.') {
                    return getTextFromDirectiveMatch(trimmedTokenText, false);
                } else if (trimmedTokenText.length() >= mars.Globals.getSettings().getEditorPopupPrefixLength()) {
                    return getTextFromInstructionMatch(trimmedTokenText, false);
                }
            }
        }
        // should never get here...
        return null;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Return ArrayList of PopupHelpItem for match of directives.  If second argument
    // true, will do exact match.  If false, will do prefix match.  Returns null
    // if no matches.
    private ArrayList<PopupHelpItem> getTextFromDirectiveMatch(String tokenText, boolean exact) {
        ArrayList<PopupHelpItem> matches = null;
        ArrayList<Directives> directiveMatches = null;
        if (exact) {
            Directives dir = Directives.matchDirective(tokenText);
            if (dir != null) {
                directiveMatches = new ArrayList<>();
                directiveMatches.add(dir);
            }
        } else {
            directiveMatches = Directives.prefixMatchDirectives(tokenText);
        }
        if (directiveMatches != null) {
            matches = new ArrayList<>();
            for (Directives direct : directiveMatches) {
                matches.add(new PopupHelpItem(tokenText, direct.getName(), direct.getDescription(), exact));
            }
        }
        return matches;
    }

    // Return text for match of instruction mnemonic.  If second argument true, will
    // do exact match.  If false, will do prefix match.   Text is returned as ArrayList
    // of PopupHelpItem objects. If no matches, returns null.
    private ArrayList<PopupHelpItem> getTextFromInstructionMatch(String tokenText, boolean exact) {
        ArrayList<Instruction> matches;
        ArrayList<PopupHelpItem> results = new ArrayList<>();
        if (exact) {
            matches = mars.Globals.instructionSet.matchOperator(tokenText);
        } else {
            matches = mars.Globals.instructionSet.prefixMatchOperator(tokenText);
        }
        if (matches == null) {
            return null;
        }
        int realMatches = 0;
        HashMap<String, String> insts = new HashMap<>();
        TreeSet<String> mnemonics = new TreeSet<>();
        for (Instruction inst : matches) {
            if (mars.Globals.getSettings().getBooleanSetting(Settings.EXTENDED_ASSEMBLER_ENABLED) || inst instanceof BasicInstruction) {
                if (exact) {
                    results.add(new PopupHelpItem(tokenText, inst.getExampleFormat(), inst.getDescription(), exact));
                } else {
                    String mnemonic = inst.getExampleFormat().split(" ")[0];
                    if (!insts.containsKey(mnemonic)) {
                        mnemonics.add(mnemonic);
                        insts.put(mnemonic, inst.getDescription());
                    }
                }
                realMatches++;
            }
        }
        if (realMatches == 0) {
            if (exact) {
                results.add(new PopupHelpItem(tokenText, tokenText, "(not a basic instruction)", exact));
            } else {
                return null;
            }
        } else {
            if (!exact) {
                for (String mnemonic : mnemonics) {
                    String info = insts.get(mnemonic);
                    results.add(new PopupHelpItem(tokenText, mnemonic, info, exact));
                }
            }
        }
        return results;
    }


    /**
     * Get KeywordMap containing all MIPS key words.  This includes all instruction mnemonics,
     * assembler directives, and register names.
     *
     * @return KeywordMap where key is the keyword and associated value is the token type (e.g. Token.KEYWORD1).
     */


    private static KeywordMap getKeywords() {
        if (cKeywords == null) {
            cKeywords = new KeywordMap(false);
            // add Instruction mnemonics
            for (Instruction inst : mars.Globals.instructionSet.getInstructionList()) {
                cKeywords.add(inst.getName(), Token.KEYWORD1);
            }
            // add assembler directives
            for (Directives direct : Directives.getDirectiveList()) {
                cKeywords.add(direct.getName(), Token.KEYWORD2);
            }
            // add integer register file
            for (Register r: RegisterFile.getRegisters()) {
                cKeywords.add(r.getName(), Token.KEYWORD3);
                cKeywords.add("x" + r.getNumber(), Token.KEYWORD3);  // also recognize x0, x1, x2, etc
            }
            // add floating point register file
            for (Register r : FloatingPointRegisterFile.getRegisters()){
                cKeywords.add(r.getName(), Token.KEYWORD3);
                cKeywords.add("f"+r.getNumber(),Token.KEYWORD3);
            }
        }
        return cKeywords;
    }

    // private members
    private static KeywordMap cKeywords;
    private static String[] tokenLabels, tokenExamples;
    private KeywordMap keywords;
    private int lastOffset;
    private int lastKeyword;

    private void doKeyword(Segment line, int i, char c) {
        int i1 = i + 1;

        int len = i - lastKeyword;
        byte id = keywords.lookup(line, lastKeyword, len);
        if (id != Token.NULL) {
            // If this is a Token.KEYWORD1 and line already contains a keyword,
            // then assume this one is a label reference and ignore it.
            //   if (id == Token.KEYWORD1 && tokenListContainsKeyword()) {
            //    }
            //    else {
            if (lastKeyword != lastOffset)
                addToken(lastKeyword - lastOffset, Token.NULL);
            addToken(len, id);
            lastOffset = i;
            //  }
        }
        lastKeyword = i1;
    }

    private boolean tokenListContainsKeyword() {
        Token token = firstToken;
        boolean result = false;
        String str = "";
        while (token != null) {
            str += "" + token.id + "(" + token.length + ") ";
            if (token.id == Token.KEYWORD1 || token.id == Token.KEYWORD2 || token.id == Token.KEYWORD3)
                result = true;
            token = token.next;
        }
        System.out.println("" + result + " " + str);
        return result;
    }
}
