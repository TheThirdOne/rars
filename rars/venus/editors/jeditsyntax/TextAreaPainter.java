/*
 * TextAreaPainter.java - Paints the text area
 * Copyright (C) 1999 Slava Pestov
 *
 * 08/05/2002	Cursor (caret) rendering fixed for JDK 1.4 (Anonymous)
 *
 * You may use and modify this package for any purpose. Redistribution is
 * permitted, in both source and binary form, provided that this notice
 * remains intact in all source distributions of this package.
 */

package rars.venus.editors.jeditsyntax;

import rars.venus.editors.jeditsyntax.tokenmarker.Token;
import rars.venus.editors.jeditsyntax.tokenmarker.TokenMarker;

import javax.swing.*;
import javax.swing.text.Segment;
import javax.swing.text.TabExpander;
import javax.swing.text.Utilities;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * The text area repaint manager. It performs double buffering and paints
 * lines of text.
 *
 * @author Slava Pestov
 * @version $Id: TextAreaPainter.java,v 1.24 1999/12/13 03:40:30 sp Exp $
 */
public class TextAreaPainter extends JComponent implements TabExpander {
    /**
     * Creates a new repaint manager. This should be not be called
     * directly.
     */
    public TextAreaPainter(JEditTextArea textArea, TextAreaDefaults defaults) {
        this.textArea = textArea;

        setAutoscrolls(true);
        setDoubleBuffered(true);
        setOpaque(true);


        ToolTipManager.sharedInstance().registerComponent(this);

        currentLine = new Segment();
        currentLineIndex = -1;

        setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));

        setFont(new Font("Courier New" /*"Monospaced"*/, Font.PLAIN, 14));
        setForeground(Color.black);
        setBackground(Color.white);

        tabSizeChars = defaults.tabSize;
        blockCaret = defaults.blockCaret;
        styles = defaults.styles;
        cols = defaults.cols;
        rows = defaults.rows;
        caretColor = defaults.caretColor;
        selectionColor = defaults.selectionColor;
        lineHighlightColor = defaults.lineHighlightColor;
        lineHighlight = defaults.lineHighlight;
        bracketHighlightColor = defaults.bracketHighlightColor;
        bracketHighlight = defaults.bracketHighlight;
        paintInvalid = defaults.paintInvalid;
        eolMarkerColor = defaults.eolMarkerColor;
        eolMarkers = defaults.eolMarkers;
    }

    /**
     * Returns if this component can be traversed by pressing the
     * Tab key. This returns false.
     * <p>
     * NOTE: as of Java 1.4 this method is deprecated and no longer
     * has the desired effect because the focus subsystem does not
     * call it.  I've implemented a KeyEventDispatcher in JEditTextArea
     * to handle Tabs. DPS 12-May-2010
     */
    public final boolean isManagingFocus() {
        return false;
    }

    /**
     * Fetch the tab size in characters.  DPS 12-May-2010.
     *
     * @return int tab size in characters
     */
    public int getTabSize() {
        return tabSizeChars;
    }

    /**
     * Set the tab size in characters. DPS 12-May-2010.
     * Originally it was fixed at PlainDocument property
     * value (8).
     *
     * @param size tab size in characters
     */
    public void setTabSize(int size) {
        tabSizeChars = size;
    }

    /**
     * Returns the syntax styles used to paint colorized text. Entry <i>n</i>
     * will be used to paint tokens with id = <i>n</i>.
     */
    public final SyntaxStyle[] getStyles() {
        return styles;
    }

    /**
     * Sets the syntax styles used to paint colorized text. Entry <i>n</i>
     * will be used to paint tokens with id = <i>n</i>.
     *
     * @param styles The syntax styles
     */
    public final void setStyles(SyntaxStyle[] styles) {
        this.styles = styles;
        repaint();
    }

    /**
     * Returns the caret color.
     */
    public final Color getCaretColor() {
        return caretColor;
    }

    /**
     * Sets the caret color.
     *
     * @param caretColor The caret color
     */
    public final void setCaretColor(Color caretColor) {
        this.caretColor = caretColor;
        invalidateSelectedLines();
    }

    /**
     * Returns the selection color.
     */
    public final Color getSelectionColor() {
        return selectionColor;
    }

    /**
     * Sets the selection color.
     *
     * @param selectionColor The selection color
     */
    public final void setSelectionColor(Color selectionColor) {
        this.selectionColor = selectionColor;
        invalidateSelectedLines();
    }

    /**
     * Returns the line highlight color.
     */
    public final Color getLineHighlightColor() {
        return lineHighlightColor;
    }

    /**
     * Sets the line highlight color.
     *
     * @param lineHighlightColor The line highlight color
     */
    public final void setLineHighlightColor(Color lineHighlightColor) {
        this.lineHighlightColor = lineHighlightColor;
        invalidateSelectedLines();
    }

    /**
     * Returns true if line highlight is enabled, false otherwise.
     */
    public final boolean isLineHighlightEnabled() {
        return lineHighlight;
    }

    /**
     * Enables or disables current line highlighting.
     *
     * @param lineHighlight True if current line highlight should be enabled,
     *                      false otherwise
     */
    public final void setLineHighlightEnabled(boolean lineHighlight) {
        this.lineHighlight = lineHighlight;
        invalidateSelectedLines();
    }

    /**
     * Returns the bracket highlight color.
     */
    public final Color getBracketHighlightColor() {
        return bracketHighlightColor;
    }

    /**
     * Sets the bracket highlight color.
     *
     * @param bracketHighlightColor The bracket highlight color
     */
    public final void setBracketHighlightColor(Color bracketHighlightColor) {
        this.bracketHighlightColor = bracketHighlightColor;
        invalidateLine(textArea.getBracketLine());
    }

    /**
     * Returns true if bracket highlighting is enabled, false otherwise.
     * When bracket highlighting is enabled, the bracket matching the
     * one before the caret (if any) is highlighted.
     */
    public final boolean isBracketHighlightEnabled() {
        return bracketHighlight;
    }

    /**
     * Enables or disables bracket highlighting.
     * When bracket highlighting is enabled, the bracket matching the
     * one before the caret (if any) is highlighted.
     *
     * @param bracketHighlight True if bracket highlighting should be
     *                         enabled, false otherwise
     */
    public final void setBracketHighlightEnabled(boolean bracketHighlight) {
        this.bracketHighlight = bracketHighlight;
        invalidateLine(textArea.getBracketLine());
    }

    /**
     * Returns true if the caret should be drawn as a block, false otherwise.
     */
    public final boolean isBlockCaretEnabled() {
        return blockCaret;
    }

    /**
     * Sets if the caret should be drawn as a block, false otherwise.
     *
     * @param blockCaret True if the caret should be drawn as a block,
     *                   false otherwise.
     */
    public final void setBlockCaretEnabled(boolean blockCaret) {
        this.blockCaret = blockCaret;
        invalidateSelectedLines();
    }

    /**
     * Returns the EOL marker color.
     */
    public final Color getEOLMarkerColor() {
        return eolMarkerColor;
    }

    /**
     * Sets the EOL marker color.
     *
     * @param eolMarkerColor The EOL marker color
     */
    public final void setEOLMarkerColor(Color eolMarkerColor) {
        this.eolMarkerColor = eolMarkerColor;
        repaint();
    }

    /**
     * Returns true if EOL markers are drawn, false otherwise.
     */
    public final boolean getEOLMarkersPainted() {
        return eolMarkers;
    }

    /**
     * Sets if EOL markers are to be drawn.
     *
     * @param eolMarkers True if EOL markers should be drawn, false otherwise
     */
    public final void setEOLMarkersPainted(boolean eolMarkers) {
        this.eolMarkers = eolMarkers;
        repaint();
    }

    /**
     * Returns true if invalid lines are painted as red tildes (~),
     * false otherwise.
     */
    public boolean getInvalidLinesPainted() {
        return paintInvalid;
    }

    /**
     * Sets if invalid lines are to be painted as red tildes.
     *
     * @param paintInvalid True if invalid lines should be drawn, false otherwise
     */
    public void setInvalidLinesPainted(boolean paintInvalid) {
        this.paintInvalid = paintInvalid;
    }

    /**
     * Adds a custom highlight painter.
     *
     * @param highlight The highlight
     */
    public void addCustomHighlight(Highlight highlight) {
        highlight.init(textArea, highlights);
        highlights = highlight;
    }

    /**
     * Highlight interface.
     */
    public interface Highlight {
        /**
         * Called after the highlight painter has been added.
         *
         * @param textArea The text area
         * @param next     The painter this one should delegate to
         */
        void init(JEditTextArea textArea, Highlight next);

        /**
         * This should paint the highlight and delgate to the
         * next highlight painter.
         *
         * @param gfx  The graphics context
         * @param line The line number
         * @param y    The y co-ordinate of the line
         */
        void paintHighlight(Graphics gfx, int line, int y);

        /**
         * Returns the tool tip to display at the specified
         * location. If this highlighter doesn't know what to
         * display, it should delegate to the next highlight
         * painter.
         *
         * @param evt The mouse event
         */
        String getToolTipText(MouseEvent evt);
    }

    /**
     * Returns the tool tip to display at the specified location.
     *
     * @param evt The mouse event
     */
    public String getToolTipText(MouseEvent evt) {
        //          if(highlights != null)
        //             return highlights.getToolTipText(evt);
        //          else
        //             return null;
        if (highlights != null){
            return highlights.getToolTipText(evt);
        }else if (this.textArea.getTokenMarker() == null) {
            return null;
        }else {
            return this.textArea.getSyntaxSensitiveToolTipText(evt.getX(), evt.getY());
        }
        //           int line = yToLine(evt.getY());
        //  int offset = xToOffset(line,evt.getX());
        //          {
        //             if (evt instanceof InstructionMouseEvent) {
        //                System.out.println("get Tool Tip Text for InstructionMouseEvent");
        //                return "Instruction: "+ ((InstructionMouseEvent)evt).getLine().toString();
        //             }
        //             else {
        //                return "Not a fake?";//null;
        //             }
        //          }
    }

    /**
     * Returns the font metrics used by this component.
     */
    public FontMetrics getFontMetrics() {
        return fm;
    }

    /**
     * Sets the font for this component. This is overridden to update the
     * cached font metrics and to recalculate which lines are visible.
     *
     * @param font The font
     */
    public void setFont(Font font) {
        super.setFont(font);
        fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
        textArea.recalculateVisibleLines();
    }


    /**
     * Repaints the text.
     *
     * @param gfx The graphics context
     */
    public void paint(Graphics gfx) {

        // Added 4/6/10 DPS to set antialiasing for text rendering - smoother letters
        // Second one says choose algorithm for quality over speed
        ((Graphics2D) gfx).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        ((Graphics2D) gfx).setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);


        tabSize = fm.charWidth(' ') * tabSizeChars; // was: ((Integer)textArea.getDocument().getProperty(PlainDocument.tabSizeAttribute)).intValue();

        Rectangle clipRect = gfx.getClipBounds();

        gfx.setColor(getBackground());
        gfx.fillRect(clipRect.x, clipRect.y, clipRect.width, clipRect.height);

        // We don't use yToLine() here because that method doesn't
        // return lines past the end of the document
        int height = fm.getHeight();
        int firstLine = textArea.getFirstLine();
        int firstInvalid = firstLine + clipRect.y / height;
        // Because the clipRect's height is usually an even multiple
        // of the font height, we subtract 1 from it, otherwise one
        // too many lines will always be painted.
        int lastInvalid = firstLine + (clipRect.y + clipRect.height - 1) / height;

        try {
            TokenMarker tokenMarker = ((SyntaxDocument) textArea.getDocument())
                    .getTokenMarker();
            int x = textArea.getHorizontalOffset();

            for (int line = firstInvalid; line <= lastInvalid; line++) {
                paintLine(gfx, tokenMarker, line, x);
            }

            if (tokenMarker != null && tokenMarker.isNextLineRequested()) {
                int h = clipRect.y + clipRect.height;
                repaint(0, h, getWidth(), getHeight() - h);
            }
        } catch (Exception e) {
            System.err.println("Error repainting line"
                    + " range {" + firstInvalid + ","
                    + lastInvalid + "}:");
            e.printStackTrace();
        }
    }

    /**
     * Marks a line as needing a repaint.
     *
     * @param line The line to invalidate
     */
    public final void invalidateLine(int line) {
        repaint(0, textArea.lineToY(line) + fm.getMaxDescent() + fm.getLeading(),
                getWidth(), fm.getHeight());
    }

    /**
     * Marks a range of lines as needing a repaint.
     *
     * @param firstLine The first line to invalidate
     * @param lastLine  The last line to invalidate
     */
    public final void invalidateLineRange(int firstLine, int lastLine) {
        repaint(0, textArea.lineToY(firstLine) + fm.getMaxDescent() + fm.getLeading(),
                getWidth(), (lastLine - firstLine + 1) * fm.getHeight());
    }

    /**
     * Repaints the lines containing the selection.
     */
    public final void invalidateSelectedLines() {
        invalidateLineRange(textArea.getSelectionStartLine(),
                textArea.getSelectionEndLine());
    }

    /**
     * Implementation of TabExpander interface. Returns next tab stop after
     * a specified point.
     *
     * @param x         The x co-ordinate
     * @param tabOffset Ignored
     * @return The next tab stop after <i>x</i>
     */
    public float nextTabStop(float x, int tabOffset) {
        int offset = textArea.getHorizontalOffset();
        int ntabs = ((int) x - offset) / tabSize;
        return (ntabs + 1) * tabSize + offset;
    }

    /**
     * Returns the painter's preferred size.
     */
    public Dimension getPreferredSize() {
        Dimension dim = new Dimension();
        dim.width = fm.charWidth('w') * cols;
        dim.height = fm.getHeight() * rows;
        return dim;
    }


    /**
     * Returns the painter's minimum size.
     */
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    // package-private members
    int currentLineIndex;
    Token currentLineTokens;
    Segment currentLine;

    // protected members
    protected JEditTextArea textArea;

    protected SyntaxStyle[] styles;
    protected Color caretColor;
    protected Color selectionColor;
    protected Color lineHighlightColor;
    protected Color bracketHighlightColor;
    protected Color eolMarkerColor;

    protected boolean blockCaret;
    protected boolean lineHighlight;
    protected boolean bracketHighlight;
    protected boolean paintInvalid;
    protected boolean eolMarkers;
    protected int cols;
    protected int rows;

    protected int tabSize, tabSizeChars;
    protected FontMetrics fm;

    protected Highlight highlights;

    protected void paintLine(Graphics gfx, TokenMarker tokenMarker,
                             int line, int x) {//System.out.println("paintLine "+ (++count));
        Font defaultFont = getFont();
        Color defaultColor = getForeground();

        currentLineIndex = line;
        int y = textArea.lineToY(line);

        if (line < 0 || line >= textArea.getLineCount()) {
            if (paintInvalid) {
                paintHighlight(gfx, line, y);
                styles[Token.INVALID].setGraphicsFlags(gfx, defaultFont);
                gfx.drawString("~", 0, y + fm.getHeight());
            }
        } else if (tokenMarker == null) {
            paintPlainLine(gfx, line, defaultFont, defaultColor, x, y);
        } else {
            paintSyntaxLine(gfx, tokenMarker, line, defaultFont,
                    defaultColor, x, y);
        }
    }

    protected void paintPlainLine(Graphics gfx, int line, Font defaultFont,
                                  Color defaultColor, int x, int y) {
        paintHighlight(gfx, line, y);
        textArea.getLineText(line, currentLine);

        gfx.setFont(defaultFont);
        gfx.setColor(defaultColor);

        y += fm.getHeight();
        x = Utilities.drawTabbedText(currentLine, x, y, gfx, this, 0);

        if (eolMarkers) {
            gfx.setColor(eolMarkerColor);
            gfx.drawString(".", x, y);
        }
    }

    //      private int count=0;
    protected void paintSyntaxLine(Graphics gfx, TokenMarker tokenMarker,
                                   int line, Font defaultFont, Color defaultColor, int x, int y) {//System.out.println("paintSyntaxLine line "+ line);
        textArea.getLineText(currentLineIndex, currentLine);
        currentLineTokens = tokenMarker.markTokens(currentLine,
                currentLineIndex);

        paintHighlight(gfx, line, y);

        gfx.setFont(defaultFont);
        gfx.setColor(defaultColor);
        y += fm.getHeight();
        x = SyntaxUtilities.paintSyntaxLine(currentLine,
                currentLineTokens, styles, this, gfx, x, y);
        //          count++;
        //          if (count % 100 == 10) {
        //             textArea.setToolTipText("Setting Text at Count of "+count); System.out.println("set tool tip");
        //          }
        //          if (count % 100 == 60) {
        //             textArea.setToolTipText(null);System.out.println("reset tool tip");
        //          }
        //System.out.println("SyntaxUtilities.paintSyntaxLine "+ (++count));
        if (eolMarkers) {
            gfx.setColor(eolMarkerColor);
            gfx.drawString(".", x, y);
        }
    }

    protected void paintHighlight(Graphics gfx, int line, int y) {//System.out.println("paintHighlight "+ (++count));
        if (line >= textArea.getSelectionStartLine()
                && line <= textArea.getSelectionEndLine())
            paintLineHighlight(gfx, line, y);

        if (highlights != null)
            highlights.paintHighlight(gfx, line, y);

        if (bracketHighlight && line == textArea.getBracketLine())
            paintBracketHighlight(gfx, line, y);

        if (line == textArea.getCaretLine())
            paintCaret(gfx, line, y);
    }

    protected void paintLineHighlight(Graphics gfx, int line, int y) {//System.out.println("paintLineHighlight "+ (++count));
        int height = fm.getHeight();
        y += fm.getLeading() + fm.getMaxDescent();

        int selectionStart = textArea.getSelectionStart();
        int selectionEnd = textArea.getSelectionEnd();

        if (selectionStart == selectionEnd) {
            if (lineHighlight) {
                gfx.setColor(lineHighlightColor);
                gfx.fillRect(0, y, getWidth(), height);
            }
        } else {
            gfx.setColor(selectionColor);

            int selectionStartLine = textArea.getSelectionStartLine();
            int selectionEndLine = textArea.getSelectionEndLine();
            int lineStart = textArea.getLineStartOffset(line);

            int x1, x2;
            if (textArea.isSelectionRectangular()) {
                int lineLen = textArea.getLineLength(line);
                x1 = textArea._offsetToX(line, Math.min(lineLen,
                        selectionStart - textArea.getLineStartOffset(
                                selectionStartLine)));
                x2 = textArea._offsetToX(line, Math.min(lineLen,
                        selectionEnd - textArea.getLineStartOffset(
                                selectionEndLine)));
                if (x1 == x2)
                    x2++;
            } else if (selectionStartLine == selectionEndLine) {
                x1 = textArea._offsetToX(line,
                        selectionStart - lineStart);
                x2 = textArea._offsetToX(line,
                        selectionEnd - lineStart);
            } else if (line == selectionStartLine) {
                x1 = textArea._offsetToX(line,
                        selectionStart - lineStart);
                x2 = getWidth();
            } else if (line == selectionEndLine) {
                x1 = 0;
                x2 = textArea._offsetToX(line,
                        selectionEnd - lineStart);
            } else {
                x1 = 0;
                x2 = getWidth();
            }

            // "inlined" min/max()
            gfx.fillRect(x1 > x2 ? x2 : x1, y, x1 > x2 ?
                    (x1 - x2) : (x2 - x1), height);
        }

    }

    protected void paintBracketHighlight(Graphics gfx, int line, int y) {
        int position = textArea.getBracketPosition();
        if (position == -1)
            return;
        y += fm.getLeading() + fm.getMaxDescent();
        int x = textArea._offsetToX(line, position);
        gfx.setColor(bracketHighlightColor);
        // Hack!!! Since there is no fast way to get the character
        // from the bracket matching routine, we use ( since all
        // brackets probably have the same width anyway
        gfx.drawRect(x, y, fm.charWidth('(') - 1,
                fm.getHeight() - 1);
    }

    protected void paintCaret(Graphics gfx, int line, int y) {
        if (textArea.isCaretVisible()) {
            int offset = textArea.getCaretPosition()
                    - textArea.getLineStartOffset(line);
            int caretX = textArea._offsetToX(line, offset);
            int caretWidth = ((blockCaret ||
                    textArea.isOverwriteEnabled()) ?
                    fm.charWidth('w') : 1);
            y += fm.getLeading() + fm.getMaxDescent();
            int height = fm.getHeight();

            gfx.setColor(caretColor);

            if (textArea.isOverwriteEnabled()) {
                gfx.fillRect(caretX, y + height - 1,
                        caretWidth, 1);
            } else {
                gfx.drawRect(caretX, y, caretWidth, height - 1);
            }
        }
    }
}
