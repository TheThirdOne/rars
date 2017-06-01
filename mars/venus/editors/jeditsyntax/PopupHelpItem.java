/*
Copyright (c) 2003-2010,  Pete Sanderson and Kenneth Vollmar

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

package mars.venus.editors.jeditsyntax;

/**
 * Handly little class to contain help information for a popupMenu or
 * tool tip item.
 */
public class PopupHelpItem {
    private String tokenText;
    private String example;
    private String description;
    private boolean exact;        // from exact match?
    private int exampleLength;
    private static final String spaces = "                                        "; // 40 spaces

    /**
     * Create popup help item.  This is created as result of either an exact-match or
     * prefix-match search.  Note that prefix-match search includes exact as well as partial matches.
     *
     * @param tokenText   The document text that matched
     * @param example     An example instruction
     * @param description A textual description of the instruction
     * @param exact       True if match occurred as result of exact-match search, false otherwise.
     */
    public PopupHelpItem(String tokenText, String example, String description, boolean exact) {
        this.tokenText = tokenText;
        this.example = example;
        if (exact) {
            this.description = description;
        } else {
            int detailPosition = description.indexOf(mars.venus.HelpHelpAction.descriptionDetailSeparator);
            this.description = (detailPosition == -1) ? description : description.substring(0, detailPosition);
        }
        this.exampleLength = this.example.length();
        this.exact = exact;
    }

    /**
     * Create popup help item, where match is result of an exact-match search.
     *
     * @param tokenText   The document text that matched
     * @param example     An example instruction
     * @param description A textual description of the instruction
     */
    public PopupHelpItem(String tokenText, String example, String description) {
        this(tokenText, example, description, true);

    }

    /**
     * The document text that mached this item
     */
    public String getTokenText() {
        return this.tokenText;
    }

    public String getExample() {
        return this.example;
    }

    public String getDescription() {
        return this.description;
    }

    /**
     * Determines whether match occurred in an exact-match or prefix-match search.
     * Note this can return false even if the match is exact because prefix-match also
     * includes exact match results.  E.g. prefix match on "lw" will match both "lwl" and "lw".
     *
     * @return True if exact-match search, false otherwise.
     */
    public boolean getExact() {
        return this.exact;
    }

    public int getExampleLength() {
        return this.exampleLength;
    }

    // for performance purposes, length limited to example length + 40
    public String getExamplePaddedToLength(int length) {
        String result = null;
        if (length > this.exampleLength) {
            int numSpaces = length - this.exampleLength;
            if (numSpaces > spaces.length()) {
                numSpaces = spaces.length();
            }
            result = this.example + spaces.substring(0, numSpaces);
        } else if (length == this.exampleLength) {
            result = this.example;
        } else {
            result = this.example.substring(0, length);
        }
        return result;
    }

    public void setExample(String example) {
        this.example = example;
        this.exampleLength = example.length();
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
