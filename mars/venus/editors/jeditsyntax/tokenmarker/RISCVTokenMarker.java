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
            for (Register r : RegisterFile.getRegisters()) {
                cKeywords.add(r.getName(), Token.KEYWORD3);
                cKeywords.add("x" + r.getNumber(), Token.KEYWORD3);  // also recognize x0, x1, x2, etc
            }
            // add floating point register file
            for (Register r : FloatingPointRegisterFile.getRegisters()) {
                cKeywords.add(r.getName(), Token.KEYWORD3);
                cKeywords.add("f" + r.getNumber(), Token.KEYWORD3);
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
}
