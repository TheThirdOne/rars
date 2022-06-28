package rars.assembler;

import java.util.ArrayList;

/*
Copyright (c) 2003-2012,  Pete Sanderson and Kenneth Vollmar

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
 * Class representing RISCV assembler directives.  If Java had enumerated types, these
 * would probably be implemented that way.  Each directive is represented by a unique object.
 * The directive name is indicative of the directive it represents.  For example, DATA
 * represents the RISCV .data directive.
 *
 * @author Pete Sanderson
 * @version August 2003
 **/

public final class Directives {

    private static ArrayList<Directives> directiveList = new ArrayList<>();
    public static final Directives DATA = new Directives(".data", "Subsequent items stored in Data segment at next available address");
    public static final Directives TEXT = new Directives(".text", "Subsequent items (instructions) stored in Text segment at next available address");
    public static final Directives WORD = new Directives(".word", "Store the listed value(s) as 32 bit words on word boundary");
    public static final Directives DWORD = new Directives(".dword", "Store the listed value(s) as 64 bit double-word on word boundary");
    public static final Directives ASCII = new Directives(".ascii", "Store the string in the Data segment but do not add null terminator");
    public static final Directives ASCIZ = new Directives(".asciz", "Store the string in the Data segment and add null terminator");
    public static final Directives STRING = new Directives(".string", "Alias for .asciz");
    public static final Directives BYTE = new Directives(".byte", "Store the listed value(s) as 8 bit bytes");
    public static final Directives ALIGN = new Directives(".align", "Align next data item on specified byte boundary (0=byte, 1=half, 2=word, 3=double)");
    public static final Directives HALF = new Directives(".half", "Store the listed value(s) as 16 bit halfwords on halfword boundary");
    public static final Directives SPACE = new Directives(".space", "Reserve the next specified number of bytes in Data segment");
    public static final Directives DOUBLE = new Directives(".double", "Store the listed value(s) as double precision floating point");
    public static final Directives FLOAT = new Directives(".float", "Store the listed value(s) as single precision floating point");
    public static final Directives EXTERN = new Directives(".extern", "Declare the listed label and byte length to be a global data field");
    public static final Directives GLOBL = new Directives(".globl", "Declare the listed label(s) as global to enable referencing from other files");
    public static final Directives GLOBAL = new Directives(".global", "Declare the listed label(s) as global to enable referencing from other files");
    /*  EQV added by DPS 11 July 2012 */
    public static final Directives EQV = new Directives(".eqv", "Substitute second operand for first. First operand is symbol, second operand is expression (like #define)");
    /* MACRO and END_MACRO added by Mohammad Sekhavat Oct 2012 */
    public static final Directives MACRO = new Directives(".macro", "Begin macro definition.  See .end_macro");
    public static final Directives END_MACRO = new Directives(".end_macro", "End macro definition.  See .macro");
    /*  INCLUDE added by DPS 11 Jan 2013 */
    public static final Directives INCLUDE = new Directives(".include", "Insert the contents of the specified file.  Put filename in quotes.");
    public static final Directives SECTION = new Directives(".section", "Allows specifying sections without .text or .data directives. Included for gcc comparability");

    private String descriptor;
    private String description; // help text

    private Directives(String name, String description) {
        this.descriptor = name;
        this.description = description;
        directiveList.add(this);
    }

    /**
     * Find Directive object, if any, which matches the given String.
     *
     * @param str A String containing candidate directive name (e.g. ".ascii")
     * @return If match is found, returns matching Directives object, else returns <tt>null</tt>.
     **/

    public static Directives matchDirective(String str) {
        for (Directives match : directiveList) {
            if (str.equalsIgnoreCase(match.descriptor)) {
                return match;
            }
        }
        return null;
    }


    /**
     * Find Directive object, if any, which contains the given string as a prefix. For example,
     * ".a" will match ".ascii", ".asciiz" and ".align"
     *
     * @param str A String
     * @return If match is found, returns ArrayList of matching Directives objects, else returns <tt>null</tt>.
     **/

    public static ArrayList<Directives> prefixMatchDirectives(String str) {
        ArrayList<Directives> matches = null;
        for (Directives direct : directiveList) {
            if (direct.descriptor.toLowerCase().startsWith(str.toLowerCase())) {
                if (matches == null) {
                    matches = new ArrayList<>();
                }
                matches.add(direct);
            }
        }
        return matches;
    }


    /**
     * Produces String-ified version of Directive object
     *
     * @return String representing Directive: its MIPS name
     **/

    public String toString() {
        return this.descriptor;
    }


    /**
     * Get name of this Directives object
     *
     * @return name of this directive as a String
     **/

    public String getName() {
        return this.descriptor;
    }

    /**
     * Get description of this Directives object
     *
     * @return description of this directive (for help purposes)
     **/

    public String getDescription() {
        return this.description;
    }

    /**
     * Produces List of Directive objects
     *
     * @return All directives defined
     **/
    public static ArrayList<Directives> getDirectiveList() {
        return directiveList;
    }


    /**
     * Lets you know whether given directive is for integer (WORD,HALF,BYTE).
     *
     * @param direct a directive
     * @return true if given directive is WORD, HALF, or BYTE, false otherwise
     **/
    public static boolean isIntegerDirective(Directives direct) {
        return direct == Directives.DWORD || direct == Directives.WORD || direct == Directives.HALF || direct == Directives.BYTE;

    }


    /**
     * Lets you know whether given directive is for floating number (FLOAT,DOUBLE).
     *
     * @param direct a directive
     * @return true if given directive is FLOAT or DOUBLE, false otherwise.
     **/
    public static boolean isFloatingDirective(Directives direct) {
        return direct == Directives.FLOAT || direct == Directives.DOUBLE;
    }

}
