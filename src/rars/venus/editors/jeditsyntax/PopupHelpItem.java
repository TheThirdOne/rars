package rars.venus.editors.jeditsyntax;

import java.util.ArrayList;

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
            int detailPosition = description.indexOf(rars.venus.HelpHelpAction.descriptionDetailSeparator);
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

    // Utility method.  Traverse ArrayList of PopupHelpItem objects
    // and return String length of longest example.
    public static int maxExampleLength(ArrayList<PopupHelpItem> matches) {
        int length = 0;
        if (matches != null) {
            for (PopupHelpItem match : matches) {
                length = Math.max(length, match.getExampleLength());
            }
        }
        return length;
    }
}
