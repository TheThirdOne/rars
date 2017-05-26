/*
 * SyntaxStyle.java - A simple text style class
 * Copyright (C) 1999 Slava Pestov
 *
 * You may use and modify this package for any purpose. Redistribution is
 * permitted, in both source and binary form, provided that this notice
 * remains intact in all source distributions of this package.
 */

package mars.venus.editors.jeditsyntax;

import java.awt.*;

/**
 * A simple text style class. It can specify the color, italic flag,
 * and bold flag of a run of text.
 *
 * @author Slava Pestov
 * @version $Id: SyntaxStyle.java,v 1.6 1999/12/13 03:40:30 sp Exp $
 */
public class SyntaxStyle {
    /**
     * Creates a new SyntaxStyle.
     *
     * @param color  The text color
     * @param italic True if the text should be italics
     * @param bold   True if the text should be bold
     */
    public SyntaxStyle(Color color, boolean italic, boolean bold) {
        this.color = color;
        this.italic = italic;
        this.bold = bold;
    }

    /**
     * Returns the color specified in this style.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Returns the color coded as Stringified 32-bit hex with
     * Red in bits 16-23, Green in bits 8-15, Blue in bits 0-7
     * e.g. "0x00FF3366" where Red is FF, Green is 33, Blue is 66.
     * This is used by Settings initialization to avoid direct
     * use of Color class.  Long story. DPS 13-May-2010
     *
     * @return String containing hex-coded color value.
     */

    public String getColorAsHexString() {
        return mars.util.Binary.intToHexString(color.getRed() << 16 | color.getGreen() << 8 | color.getBlue());
    }

    /**
     * Returns true if no font styles are enabled.
     */
    public boolean isPlain() {
        return !(bold || italic);
    }

    /**
     * Returns true if italics is enabled for this style.
     */
    public boolean isItalic() {
        return italic;
    }

    /**
     * Returns true if boldface is enabled for this style.
     */
    public boolean isBold() {
        return bold;
    }

    /**
     * Returns the specified font, but with the style's bold and
     * italic flags applied.
     */
    public Font getStyledFont(Font font) {
        if (font == null)
            throw new NullPointerException("font param must not"
                    + " be null");
        if (font.equals(lastFont))
            return lastStyledFont;
        lastFont = font;
        lastStyledFont = new Font(font.getFamily(),
                (bold ? Font.BOLD : 0)
                        | (italic ? Font.ITALIC : 0),
                font.getSize());
        return lastStyledFont;
    }

    /**
     * Returns the font metrics for the styled font.
     */
    public FontMetrics getFontMetrics(Font font) {
        if (font == null)
            throw new NullPointerException("font param must not"
                    + " be null");
        if (font.equals(lastFont) && fontMetrics != null)
            return fontMetrics;
        lastFont = font;
        lastStyledFont = new Font(font.getFamily(),
                (bold ? Font.BOLD : 0)
                        | (italic ? Font.ITALIC : 0),
                font.getSize());
        fontMetrics = Toolkit.getDefaultToolkit().getFontMetrics(
                lastStyledFont);
        return fontMetrics;
    }


    /**
     * Sets the foreground color and font of the specified graphics
     * context to that specified in this style.
     *
     * @param gfx  The graphics context
     * @param font The font to add the styles to
     */
    public void setGraphicsFlags(Graphics gfx, Font font) {
        Font _font = getStyledFont(font);
        gfx.setFont(_font);
        gfx.setColor(color);
    }

    /**
     * Returns a string representation of this object.
     */
    public String toString() {
        return getClass().getName() + "[color=" + color +
                (italic ? ",italic" : "") +
                (bold ? ",bold" : "") + "]";
    }

    // private members
    private Color color;
    private boolean italic;
    private boolean bold;
    private Font lastFont;
    private Font lastStyledFont;
    private FontMetrics fontMetrics;
}
