package rars.venus;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/*
 * Use this to render Monospaced and right-aligned data in JTables.
 * I am using it to render integer addresses and values that are stored as
 * Strings containing either the decimal or hexidecimal version
 * of the integer value.
 *
 * This also add the ellipsis on the left on narrow columns, so the low order bytes are show first.
 * e.g. "0x0000000000001234" is shown as "...01234" if not enough with in the column.
 *
 */
public class MonoRightCellRenderer extends DefaultTableCellRenderer {
    public static final Font MONOSPACED_PLAIN_12POINT = new Font("Monospaced", Font.PLAIN, 12);

    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel cell = (JLabel) super.getTableCellRendererComponent(table, value,
                isSelected, hasFocus, row, column);
        cell.setFont(MONOSPACED_PLAIN_12POINT);
        cell.setHorizontalAlignment(SwingConstants.RIGHT);

        //  Determine the width available to render the text

        int availableWidth = table.getColumnModel().getColumn(column).getWidth();
        availableWidth -= (int)table.getIntercellSpacing().getWidth();
        Insets borderInsets = getBorder().getBorderInsets(cell);
        availableWidth -= (borderInsets.left + borderInsets.right);
        String cellText = cell.getText();
        FontMetrics fm = getFontMetrics( getFont() );

        //  Not enough space so start rendering from the end of the string
        //  until all the space is used up
        if (fm.stringWidth(cellText) > availableWidth) {
            String dots = "...";
            int textWidth = fm.stringWidth(dots);
            int i = cellText.length() - 1;

            for (; i > 0; i--) {
                textWidth += fm.charWidth(cellText.charAt(i));

                if (textWidth > availableWidth) {
                    break;
                }
            }

            cell.setText(dots + cellText.substring(i + 1));
        }

        return cell;
    }
}