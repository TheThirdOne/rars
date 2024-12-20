package rars.util;

import rars.Globals;

import java.awt.*;
import java.util.Arrays;

/**
 * Specialized Font class designed to be used by both the
 * settings menu methods and the Settings class.
 *
 * @author Pete Sanderson
 * @version July 2007
 */
public class EditorFont {

    // Note: These are parallel arrays so corresponding elements must match up.
    private static final String[] styleStrings = {"Plain", "Bold", "Italic", "Bold + Italic"};
    private static final int[] styleInts = {Font.PLAIN, Font.BOLD, Font.ITALIC, Font.BOLD | Font.ITALIC};
    public static final String DEFAULT_STYLE_STRING = styleStrings[0];
    public static final int DEFAULT_STYLE_INT = styleInts[0];
    public static final int MIN_SIZE = 6;
    public static final int MAX_SIZE = 72;
    public static final int DEFAULT_SIZE = 12;
    /* Fonts in 3 categories that are common to major Java platforms: Win, Mac, Linux.
     *    Monospace: Courier New and Lucida Sans Typewriter
     *    Serif: Georgia, Times New Roman
     *    Sans Serif: Ariel, Verdana
     * This is according to lists published by www.codestyle.org.
     */
    private static final String[] allCommonFamilies = {"Arial", "Courier New", "Georgia",
            "Lucida Sans Typewriter", "Times New Roman", "Verdana"};


    /**
     * Obtain an array of common font family names.  These are guaranteed to
     * be available at runtime, as they were checked against the local
     * GraphicsEnvironment.
     *
     * @return Array of strings, each is a common and available font family name.
     */

    public static String[] getCommonFamilies() {
        return commonFamilies;
    }

    /**
     * Obtain an array of all available font family names.  These are guaranteed to
     * be available at runtime, as they come from the local GraphicsEnvironment.
     *
     * @return Array of strings, each is an available font family name.
     */

    public static String[] getAllFamilies() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    }

    /**
     * Get array containing String values for font style names.
     */
    public static String[] getFontStyleStrings() {
        return styleStrings;
    }


    /**
     * Given a string that represents a font style, returns the
     * corresponding final int defined in Font: PLAIN, BOLD, ITALIC.  It
     * is not case-sensitive.
     *
     * @param style String representing the font style name
     * @return The int value of the corresponding Font style constant.  If the
     * string does not match any style name, returns Font.PLAIN.
     */
    public static int styleStringToStyleInt(String style) {
        String styleLower = style.toLowerCase();
        for (int i = 0; i < styleStrings.length; i++) {
            if (styleLower.equals(styleStrings[i].toLowerCase())) {
                return styleInts[i];
            }
        }
        return DEFAULT_STYLE_INT;
    }

    /**
     * Given an int that represents a font style from the Font class,
     * returns the corresponding String.
     *
     * @param style Must be one of Font.PLAIN, Font.BOLD, Font.ITALIC.
     * @return The String representation of that style.  If the parameter
     * is not one of the above, returns "Plain".
     */
    public static String styleIntToStyleString(int style) {
        for (int i = 0; i < styleInts.length; i++) {
            if (style == styleInts[i]) {
                return styleStrings[i];
            }
        }
        return DEFAULT_STYLE_STRING;
    }

    /**
     * Given an int representing font size, returns corresponding string.
     *
     * @param size Int representing size.
     * @return String value of parameter, unless it is less than MIN_SIZE (returns MIN_SIZE
     * as String) or greater than MAX_SIZE (returns MAX_SIZE as String).
     */
    public static String sizeIntToSizeString(int size) {
        int result = (size < MIN_SIZE) ? MIN_SIZE : ((size > MAX_SIZE) ? MAX_SIZE : size);
        return String.valueOf(result);
    }

    /**
     * Given a String representing font size, returns corresponding int.
     *
     * @param size String representing size.
     * @return int value of parameter, unless it is less than MIN_SIZE (returns
     * MIN_SIZE) or greater than MAX_SIZE (returns MAX_SIZE).  If the string
     * cannot be parsed as a decimal integer, it returns DEFAULT_SIZE.
     */
    public static int sizeStringToSizeInt(String size) {
        int result = DEFAULT_SIZE;
        try {
            result = Integer.parseInt(size);
        } catch (NumberFormatException e) {
        }
        return (result < MIN_SIZE) ? MIN_SIZE : ((result > MAX_SIZE) ? MAX_SIZE : result);
    }

    /**
     * Creates a new Font object based on the given String specifications.  This
     * is different than Font's constructor, which requires ints for style and size.
     * It assures that defaults and size limits are applied when necessary.
     *
     * @param family String containing font family.
     * @param style  String containing font style.  A list of available styles can
     *               be obtained from getFontStyleStrings().  The default of styleStringToStyleInt()
     *               is substituted if necessary.
     * @param size   String containing font size.  The defaults and limits of
     *               sizeStringToSizeInt() are substituted if necessary.
     */
    public static Font createFontFromStringValues(String family, String style, String size) {
        return new Font(family, styleStringToStyleInt(style), sizeStringToSizeInt(size));
    }

    private static final String TAB_STRING = "\t";
    private static final char TAB_CHAR = '\t';
    private static final String SPACES = "                                                  ";

    /**
     * Handy utility to produce a string that substitutes spaces for all tab characters
     * in the given string.  The number of spaces generated is based on the position of
     * the tab character and the editor's current tab size setting.
     *
     * @param string The original string
     * @return New string in which spaces are substituted for tabs
     * @throws NullPointerException if string is null
     */
    public static String substituteSpacesForTabs(String string) {
        return substituteSpacesForTabs(string, Globals.getSettings().getEditorTabSize());
    }

    /**
     * Handy utility to produce a string that substitutes spaces for all tab characters
     * in the given string.  The number of spaces generated is based on the position of
     * the tab character and the specified tab size.
     *
     * @param string  The original string
     * @param tabSize The number of spaces each tab character represents
     * @return New string in which spaces are substituted for tabs
     * @throws NullPointerException if string is null
     */
    public static String substituteSpacesForTabs(String string, int tabSize) {
        if (!string.contains(TAB_STRING))
            return string;
        StringBuffer result = new StringBuffer(string);
        for (int i = 0; i < result.length(); i++) {
            if (result.charAt(i) == TAB_CHAR) {
                result.replace(i, i + 1, SPACES.substring(0, tabSize - (i % tabSize)));
            }
        }
        return result.toString();
    }

    /*
   * We want to vett the above list against the actual available families and give
   * our client only those that are actually available.
   */
    private static final String[] commonFamilies = actualCommonFamilies();

    private static String[] actualCommonFamilies() {
        String[] result = new String[allCommonFamilies.length];
        String[] availableFamilies = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        Arrays.sort(availableFamilies); // not sure if necessary; is the list already alphabetical?
        int k = 0;
        for (String family : allCommonFamilies) {
            if (Arrays.binarySearch(availableFamilies, family) >= 0) {
                result[k++] = family;
            }
        }
        // If not all are found, creat a new array with only the ones that are.
        if (k < allCommonFamilies.length) {
            String[] temp = new String[k];
            System.arraycopy(result, 0, temp, 0, k);
            result = temp;
        }
        return result;
    }
}

