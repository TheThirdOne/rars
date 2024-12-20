package rars.tools;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class ConversionTool {
    private boolean updating = false;

    /**
     * Creates a panel containing the conversion tool.
     * 
     * @return A JPanel containing the conversion tool.
     */

    public JPanel createConversionToolPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        // Create labels and text fields for each format
        JLabel decimalLabel = new JLabel("Dec:");
        JTextField decimalField = new JTextField(5);
        decimalField.setToolTipText("Enter a decimal number (e.g., 97)");

        JLabel hexLabel = new JLabel("Hex:");
        JTextField hexField = new JTextField(7);
        hexField.setToolTipText("Enter a hexadecimal number with or without '0x' prefix (e.g., 0x61)");

        JLabel binaryLabel = new JLabel("Bin:");
        JTextField binaryField = new JTextField(10);
        binaryField.setToolTipText("Enter a binary number with or without '0b' prefix (e.g., 0b01100001)");

        JLabel charLabel = new JLabel("ASCII:");
        JTextField charField = new JTextField(2);
        charField.setToolTipText("Enter a single character (e.g., a)");

        // Add labels and text fields to the panel
        panel.add(decimalLabel);
        panel.add(decimalField);
        panel.add(hexLabel);
        panel.add(hexField);
        panel.add(binaryLabel);
        panel.add(binaryField);
        panel.add(charLabel);
        panel.add(charField);

        // Document Listeners to the all formats text fields to update it when the user types
        addDocumentListeners(decimalField, hexField, binaryField, charField);

        return panel;
    }

    /**
     * Adds document listeners to the text fields to update the other fields when the user types.
     * 
     * @param decimalField      The decimal text field.
     * @param hexField          The hexadecimal text field.
     * @param binaryField       The binary text field.
     * @param charField         The character text field.
     */
    private void addDocumentListeners(JTextField decimalField, JTextField hexField, JTextField binaryField, JTextField charField) {
        decimalField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateFromDecimal(decimalField, hexField, binaryField, charField);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateFromDecimal(decimalField, hexField, binaryField, charField);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateFromDecimal(decimalField, hexField, binaryField, charField);
            }
        });

        hexField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateFromHex(hexField, decimalField, binaryField, charField);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateFromHex(hexField, decimalField, binaryField, charField);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateFromHex(hexField, decimalField, binaryField, charField);
            }
        });

        binaryField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateFromBinary(binaryField, decimalField, hexField, charField);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateFromBinary(binaryField, decimalField, hexField, charField);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateFromBinary(binaryField, decimalField, hexField, charField);
            }
        });

        charField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateFromChar(charField, decimalField, hexField, binaryField);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateFromChar(charField, decimalField, hexField, binaryField);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateFromChar(charField, decimalField, hexField, binaryField);
            }
        });
    }

    /**
     * Updates the other fields when the decimal field is changed.
     * 
     * @param decimalField      The decimal text field.
     * @param hexField          The hexadecimal text field.
     * @param binaryField       The binary text field.
     * @param charField         The character text field.
     */
    private void updateFromDecimal(JTextField decimalField, JTextField hexField, JTextField binaryField, JTextField charField) {
        if (updating) return;
        updating = true;
        try {
            int decimal = Integer.parseInt(decimalField.getText());
            hexField.setText("0x" + Integer.toHexString(decimal));
            binaryField.setText("0b" + Integer.toBinaryString(decimal));
            if (decimal <= 127)
                charField.setText(Character.toString((char) decimal));
            else
                charField.setText("");
        } catch (NumberFormatException ex) {
            hexField.setText("");
            binaryField.setText("");
            charField.setText("");
        }
        updating = false;
    }

    /**
     * Updates the other fields when the hexadecimal field is changed.
     * 
     * @param hexField          The hexadecimal text field.
     * @param decimalField      The decimal text field.
     * @param binaryField       The binary text field.
     * @param charField         The character text field.
     */
    private void updateFromHex(JTextField hexField, JTextField decimalField, JTextField binaryField, JTextField charField) {
        if (updating) return;
        updating = true;
        try {
            String hexInput = hexField.getText();
            if (hexInput.startsWith("0x")) {
                hexInput = hexInput.substring(2);
            }
            int decimal = Integer.parseInt(hexInput, 16);
            decimalField.setText(Integer.toString(decimal));
            binaryField.setText("0b" + Integer.toBinaryString(decimal));
            if (decimal <= 127)
                charField.setText(Character.toString((char) decimal));
            else
                charField.setText("");
        } catch (NumberFormatException ex) {
            decimalField.setText("");
            binaryField.setText("");
            charField.setText("");
        }
        updating = false;
    }

    /**
     * Updates the other fields when the binary field is changed.
     * 
     * @param binaryField       The binary text field.
     * @param decimalField      The decimal text field.
     * @param hexField          The hexadecimal text field.
     * @param charField         The character text field.
     */
    private void updateFromBinary(JTextField binaryField, JTextField decimalField, JTextField hexField, JTextField charField) {
        if (updating) return;
        updating = true;
        try {
            String binaryInput = binaryField.getText();
            if (binaryInput.startsWith("0b")) {
                binaryInput = binaryInput.substring(2);
            }
            int decimal = Integer.parseInt(binaryInput, 2);
            decimalField.setText(Integer.toString(decimal));
            hexField.setText("0x" + Integer.toHexString(decimal));
            if (decimal <= 127)
                charField.setText(Character.toString((char) decimal));
            else
                charField.setText("");
        } catch (NumberFormatException ex) {
            decimalField.setText("");
            hexField.setText("");
            charField.setText("");
        }
        updating = false;
    }

    /**
     * Updates the other fields when the character field is changed.
     * 
     * @param charField         The character text field.
     * @param decimalField      The decimal text field.
     * @param hexField          The hexadecimal text field.
     * @param binaryField       The binary text field.
     */
    private void updateFromChar(JTextField charField, JTextField decimalField, JTextField hexField, JTextField binaryField) {
        if (updating) return;
        updating = true;
        try {
            if (charField.getText().length() > 0) {
                char character = charField.getText().charAt(0);
                int decimal = (int) character;
                decimalField.setText(Integer.toString(decimal));
                hexField.setText("0x" + Integer.toHexString(decimal));
                binaryField.setText("0b" + Integer.toBinaryString(decimal));
            } else {
                decimalField.setText("");
                hexField.setText("");
                binaryField.setText("");
            }
        } catch (Exception ex) {
            decimalField.setText("");
            hexField.setText("");
            binaryField.setText("");
        }
        updating = false;
    }
}