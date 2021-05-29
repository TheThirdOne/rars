package rars.tools;

import java.awt.GridLayout;
import rars.Globals;
import javax.swing.*;

/*
Copyright (c) 2021, Giancarlo Pernudi Segura & Siva Chowdeswar Nandipati.

Developed by Giancarlo Pernudi Segura (pernudi@ualberta.ca) & Siva Chowdeswar Nandipati (sivachow@ualberta.ca).

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

public class ReservationTablesTool extends AbstractToolAndApplication {
    private static String heading = "Reservation Table Tool";
    private static String version = "Version 1.0";

    JTable reservations;

    public ReservationTablesTool() {
        super(heading + ", " + version, heading);
    }

    protected JComponent buildMainDisplayArea() {
        JPanel panelTools = new JPanel(new GridLayout());
        String[] columns = new String[Globals.reservationTables.processors];
        for (int i = 0; i < columns.length; i++) {
            columns[i] = String.format("Processor %d", i);
        }
        reservations = new JTable(Globals.reservationTables.getAllAddressesAsStrings(), columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        reservations.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panelTools.add(reservations);
        return panelTools;
    }

    @Override
    protected JComponent buildButtonAreaForTool(){
        super.buildButtonAreaForTool();
        JButton clearButton = new JButton("Clear Selected");
        clearButton.setToolTipText("Clear the Selected from the Reserve Table");
        clearButton.addActionListener(l -> {
            if (connectButton.isConnected()) {
                int row = reservations.getSelectedRow();
                int col = reservations.getSelectedColumn();
                if(row < 0 || col < 0)
                    return;
                int address = Integer.parseInt(reservations.getValueAt(row, col)
                    .toString().substring(2), 16);
                Globals.reservationTables.unreserveAddress(col, address);
            }
            reservations.clearSelection();
            updateDisplay();
        });
        clearButton.addKeyListener(new EnterKeyListener(clearButton));
        buttonArea.add(clearButton);
        buttonArea.add(Box.createHorizontalGlue());
        return buttonArea;

    }

    @Override
    public String getName() {
        return heading;
    }

    protected JComponent getHelpComponent() {
        final String helpContent = "Use this tool to simulate atomic operations such as store conditional.\n"
                + "While this tool is connected to the program, the table below shows the\n"
                + "reservation table for each processor. Addresses reserved by a processor\n"
                + "will appear under that processor's column. You can release an address,\n"
                + "which will release that address across all the processor's tables in\n"
                + "order to simulate some other processor performing a store conditional.\n"
                + "(contributed by Giancarlo Pernudi Segura, pernudi@ualberta.ca)";
        JButton help = new JButton("Help");
        help.addActionListener(l -> {
            JOptionPane.showMessageDialog(theWindow, helpContent);
        });
        return help;
    }

    @Override
    protected void updateDisplay() {
        String[][] addresses = Globals.reservationTables.getAllAddressesAsStrings();
        for (int i = 0; i < addresses.length; i++) {
            for (int j = 0; j < addresses[i].length; j++) {
                reservations.setValueAt(addresses[i][j], i, j);
            }
        }
    }

    @Override
    protected void reset() {
            Globals.reservationTables.reset();
            updateDisplay();
    }
}
