package mars.tools;

import mars.Globals;
import mars.mips.hardware.AccessNotice;
import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.MemoryAccessNotice;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

/**
 * Simple Demo of Mars tool capability
 */

public class MarsBot implements Observer, MarsTool {
    private static final int GRAPHIC_WIDTH = 512;
    private static final int GRAPHIC_HEIGHT = 512;
    private static final int ADDR_HEADING = 0xffff8010;
    private static final int ADDR_LEAVETRACK = 0xffff8020;
    private static final int ADDR_WHEREAREWEX = 0xffff8030;
    private static final int ADDR_WHEREAREWEY = 0xffff8040;
    private static final int ADDR_MOVE = 0xffff8050;
    private MarsBotDisplay graphicArea;
    private int MarsBotHeading = 0; // 0 --> North (up), 90 --> East (right), etc.
    private boolean MarsBotLeaveTrack = false; // true --> leave track when moving, false --> do not ...
    private double MarsBotXPosition = 0; // X pixel position of MarsBot
    private double MarsBotYPosition = 0; // Y pixel position of MarsBot
    private boolean MarsBotMoving = false; // true --> MarsBot is moving, false --> MarsBot not moving

    // The begin and end points of a "track" segment are kept in neighboring pairs
    // of elements of the array. arrayOfTrack[i] is the start pt, arrayOfTrack[i+1] is
    // the end point of a path that should leave a track.
    private final int trackPts = 256;  // TBD Hardcoded. Array contains start-end points for segments in track.
    private Point[] arrayOfTrack = new Point[trackPts];
    private int trackIndex = 0;

    // private inner class
    private class BotRunnable implements Runnable {
        JPanel panel;

        public BotRunnable() // constructor
        {
            final JFrame frame = new JFrame("Bot");
            panel = new JPanel(new BorderLayout());
            graphicArea = new MarsBotDisplay(GRAPHIC_WIDTH, GRAPHIC_HEIGHT);
            JPanel buttonPanel = new JPanel();
            JButton clearButton = new JButton("Clear");
            clearButton.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            graphicArea.clear();
                            MarsBotLeaveTrack = false; // true --> leave track when moving, false --> do not ...
                            MarsBotXPosition = 0; // X pixel position of MarsBot
                            MarsBotYPosition = 0; // Y pixel position of MarsBot
                            MarsBotMoving = false; // true --> MarsBot is moving, false --> MarsBot not moving

                            trackIndex = 0;

                        }

                    });
            buttonPanel.add(clearButton);
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            frame.setVisible(false);

                        }

                    });
            buttonPanel.add(closeButton);
            panel.add(graphicArea, BorderLayout.CENTER);
            panel.add(buttonPanel, BorderLayout.SOUTH);
            frame.getContentPane().add(panel);
            frame.pack();
            frame.setVisible(true);
            frame.setTitle(" This is the MarsBot");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // changed 12/12/09 DPS (was EXIT)
            frame.setSize(GRAPHIC_WIDTH + 200, GRAPHIC_HEIGHT + 100); // TBD  SIZE
            frame.setVisible(true); // show();

        } // end BotRunnable() constructor

        public void run() {

            double tempAngle;
            // infinite loop: move the bot according to the current directives
            // (which may be to NOT move)
            do {
                if (MarsBotMoving) {
                    //System.out.println("BotRunnable.run: bot IS moving.");
                    // TBD This is an arbitrary distance for bot movement. This could just
                    // as easily be a random distance to simulate terrain, etc.
                    // adjust bot position.
                    // The "mathematical angle" is zero at east, 90 at north, etc.
                    // The "heading" is 0 at north, 90 at east, etc.
                    // Conversion: MathAngle = [(360 - heading) + 90] mod 360
                    tempAngle = ((360 - MarsBotHeading) + 90) % 360;
                    MarsBotXPosition += Math.cos(Math.toRadians(tempAngle)); // Math.cos parameter unit is radians
                    MarsBotYPosition += -Math.sin(Math.toRadians(tempAngle)); // Negate value because Y coord grows down

                    // Write this new information to MARS memory area
                    try {
                        Globals.memory.setWord(ADDR_WHEREAREWEX, (int) MarsBotXPosition);
                        Globals.memory.setWord(ADDR_WHEREAREWEY, (int) MarsBotYPosition);

                    } catch (AddressErrorException e) {
                        // TBD TBD TBD No action
                    }

                    //System.out.println(" ------- Heading is " + MarsBotHeading + ", angle is " + tempAngle);
                    //System.out.println(" ------- New X,Y is (" + MarsBotXPosition + "," + MarsBotYPosition + ")" );

                    // Whether or not we're leaving a track, write the current point to the
                    // current position in the array.
                    //   -- If we are not leaving a track now, we will need the current point to
                    //      start a future track, and that goes into the array.
                    //   -- If we are leaving a track now, the current point may end the track,
                    //      and that goes into the array.
                    arrayOfTrack[trackIndex] = new Point((int) MarsBotXPosition, (int) MarsBotYPosition);

                } else {
                    // Action for if the MarsBot isn't moving
                    // System.out.println("BotRunnable.run: bot is not moving.");
                }

                // TBD Pause whether the bot is or is not moving. This gives the MIPS program
                // opportunity to consider results of movement, or to make the bot move.
                // ??? What is relationship of robot speed to MARS's
                // execution time for a single instruction? Does the robot speed have to
                // be slow enough to allow a MARS busy loop to detect the bot position
                // at a specific pixel?
                try {
                    //System.out.println(" Hello from the bot runner");
                    Thread.sleep(40);
                } catch (InterruptedException exception) {// no action
                }

                panel.repaint(); // show new bot position
            } while (true);

        } // end run method of BotRunnable class

    } // end BotRunnable class

    /* ------------------------------------------------------------------------- */
    private class MarsBotDisplay extends JPanel {
        private int width;
        private int height;
        private boolean clearTheDisplay = true;


        public MarsBotDisplay(int tw, int th) {
            width = tw;
            height = th;

        }

        public void redraw() {
            repaint();
        }

        public void clear() {
            // clear the graphic display
            clearTheDisplay = true;
            //System.out.println("MarsBotDisplay.clear: called to clear the display");
            repaint();
        }

        public void paintComponent(Graphics g) {
            long tempN;
            // System.out.println("MarsBotDisplay.paintComponent: I'm painting! n is " + n);


            // Recover Graphics2D
            Graphics2D g2 = (Graphics2D) g;
            
            /*
            if (clearTheDisplay)
            {
                g2.setColor(Color.lightGray);
                g2.fillRect(0, 0, width - 1, height - 1); // Clear all previous drawn information
                clearTheDisplay = false;
            }
            */

            // Draw the track left behind, for each segment of the path
            g2.setColor(Color.blue);
            for (int i = 1; i <= trackIndex; i += 2) // Index grows by two (begin-end pair)
            {
                //System.out.print(".");
                try {
                    g2.drawLine((int) arrayOfTrack[i - 1].getX(), (int) arrayOfTrack[i - 1].getY(),
                            (int) arrayOfTrack[i].getX(), (int) arrayOfTrack[i].getY());
                } catch (ArrayIndexOutOfBoundsException e) {
                    // No action   TBD sloppy
                } catch (NullPointerException e) {
                    // No action   TBD sloppy
                }
            }

            g2.setColor(Color.black);
            g2.fillRect((int) MarsBotXPosition, (int) MarsBotYPosition, 20, 20); // Draw bot at its current position
         
            /*
             g2.setColor(Color.blue);
             g2.setFont(new Font(g2.getFont().getName(), g2.getFont().getStyle(), 20) );  // same font and style in larger size
             g2.drawOval( width/2 - 30,  // TBD Hardcoded oval size
             height/2 - 30,
             60,
             60);
             g2.drawString(" " + n, width/2, height/2);
             */


        }

    } // end private inner class MarsBotDisplay
   
    /* ------------------------------------------------------------------------- */


    public String getName() {
        return "Mars Bot";
    }

    /*
     * This will set up the Bot's GUI.  Invoked when Bot menu item selected.
     */
    public void action() {
        BotRunnable br1 = new BotRunnable();
        Thread t1 = new Thread(br1);
        t1.start();
        // New: DPS 27 Feb 2006.  Register observer for memory subrange.
        try {
            Globals.memory.addObserver(this, 0xffff8000, 0xffff8060);
        } catch (AddressErrorException aee) {
            System.out.println(aee);
        }
    }

    /*
     * This method observes MIPS program directives to modify Bot activity (that is,
     * MIPS program write to MMIO) and updates instance variables to reflect that
     * directive.
     */
    public void update(Observable o, Object arg) {
        MemoryAccessNotice notice;
        int address;
        if (arg instanceof MemoryAccessNotice) {
            notice = (MemoryAccessNotice) arg;
            address = notice.getAddress();
            if (address < 0 && notice.getAccessType() == AccessNotice.WRITE) {
                String message = "";
                if (address == ADDR_HEADING) {
                    message = "MarsBot.update: got move heading value: ";
                    MarsBotHeading = notice.getValue();
                    //System.out.println(message + notice.getValue() );
                } else if (address == ADDR_LEAVETRACK) {
                    message = "MarsBot.update: got leave track directive value ";

                    // If we HAD NOT been leaving a track, but we should NOW leave
                    // a track, put start point into array.
                    if (MarsBotLeaveTrack == false && notice.getValue() == 1) {
                        MarsBotLeaveTrack = true;
                        arrayOfTrack[trackIndex] = new Point((int) MarsBotXPosition, (int) MarsBotYPosition);
                        trackIndex++;  // the index of the end point
                    }
                    // If we HAD NOT been leaving a track, and get another directive
                    // to NOT leave a track, do nothing (nothing to do).
                    else if (MarsBotLeaveTrack == false && notice.getValue() == 0) {
                        // NO ACTION
                    }
                    // If we HAD been leaving a track, and get another directive
                    // to LEAVE a track, do nothing (nothing to do).
                    else if (MarsBotLeaveTrack == true && notice.getValue() == 1) {
                        // NO ACTION
                    }
                    // If we HAD been leaving a track, and get another directive
                    // to NOT leave a track, put end point into array.
                    else if (MarsBotLeaveTrack == true && notice.getValue() == 0) {
                        MarsBotLeaveTrack = false;
                        arrayOfTrack[trackIndex] = new Point((int) MarsBotXPosition, (int) MarsBotYPosition);
                        trackIndex++;  // the index of the next start point
                    }

                    //System.out.println("MarsBotDisplay.paintComponent: putting point in track array at " + trackIndex);

                    //System.out.println(message + notice.getValue() );
                } else if (address == ADDR_MOVE) {
                    message = "MarsBot.update: got move control value: ";
                    if (notice.getValue() == 0) MarsBotMoving = false;
                    else MarsBotMoving = true;
                    //System.out.println(message + notice.getValue() );
                } else if (address == ADDR_WHEREAREWEX ||
                        address == ADDR_WHEREAREWEY) {
                    // Ignore these memory writes, because the writes originated within
                    // this tool. This tool is being notified of the writes in the usual
                    // manner, but the writes are already known to this tool.
                    // NO ACTION
                } else {
                    //message = "MarsBot.update: HEY!!! unknown address of " + Integer.toString(address) + ", value: ";
                    //System.out.println(message + notice.getValue() );
                }

            }
        }

    }

}

