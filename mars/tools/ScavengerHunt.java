package mars.tools;

import mars.Globals;
import mars.mips.hardware.AccessNotice;
import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.MemoryAccessNotice;
import mars.util.Binary;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

/**
 * Demo of Mars tool capability.    Ken Vollmar, 27 Oct 2006  KenVollmar@missouristate.edu
 * This tool displays movements by a series of players in a game of ScavengerHunt.
 * Players will read and write MIPS memory-mapped locations to move and regain energy.
 * See accompanying documentation for memory-mapped addresses, rules of the game, etc.
 */

public class ScavengerHunt implements Observer, MarsTool {
    private static final int GRAPHIC_WIDTH = 712;
    private static final int GRAPHIC_HEIGHT = 652;

    private static final int NUM_PLAYERS = 22;    // Number of players in the game, including "baseline" player
    private static final int MAX_X_MOVEMENT = 2;  // Max. movement in X direction
    private static final int MAX_Y_MOVEMENT = 2;  // Max. movement in X direction
    private static final double MAX_MOVE_DISTANCE = 2.5;  // Max. distance (Euclidean measure)
    private static final int ENERGY_AWARD = 20;    // Energy awarded for each task completion
    private static final int ENERGY_PER_MOVE = 1;  // Energy used in making each move (regardless of distance)
    private static final int SIZE_OF_TASK = 20;   // Number of elements in the task

    private static final int NUM_LOCATIONS = 7;  // Number of locations to which ScavengerHunt players travel.
    // The first (n-1) locations are "random" locations, the last is START_AND_END_LOCATION

    private static final int START_AND_END_LOCATION = 255; // Start and end location of the ScavengerHunt
    private static final int ADMINISTRATOR_ID = 999;  // Special ID for administrator


    // MIPS addresses of administrative memory space.
    // The administrator MIPS program writes a value to the Authentication field prior to writing a new
    // value to the PlayerID field. The value of the Authentication field is not itself verified until
    // the PlayerID field changes. Each new value of the Authentication field is one of some sequence
    // known to the Tool program (one-time pad system).
    // Each change of data in the PlayerID field is a signal for the Tool to check the value of the Authentication
    // field. If the value of the Authentication field is correct, the value of the PlayerID field is used
    // for memory bounds checking. A new value of the Authentication field is expected at each change of the
    // PlayerID field (one-time pad system).
    private static final int ADDR_AUTHENTICATION = 0xffffe000;  // MIPS byte address of Authentication field
    private static final int ADDR_PLAYER_ID = 0xffffe004;  // MIPS byte address of PlayerID field
    private static final int ADDR_GAME_ON = 0xffffe008;  // MIPS byte address of signal that administration has initialized data
    private static final int ADDR_NUM_TURNS = 0xffffe00c;  // MIPS byte address of number of turns remaining in the game


    // MIPS addresses of various data in each player's memory space.
    // Each player's assigned memory is the MEM_PER_PLAYER bytes which begin at
    // location ADDR_BASE + (ID *  MEM_PER_PLAYER)
    private static final int ADDR_BASE = 0xffff8000;  // MIPS byte address of memory space for first player
    private static final int MEM_PER_PLAYER = 0x400;  // MIPS bytes of memory space given to each player
    private static final int OFFSET_WHERE_AM_I_X = 0x0;  // MIPS byte offset to this field
    private static final int OFFSET_WHERE_AM_I_Y = 0x4;  // MIPS byte offset to this field
    private static final int OFFSET_MOVE_TO_X = 0x8;  // MIPS byte offset to this field
    private static final int OFFSET_MOVE_TO_Y = 0xc;  // MIPS byte offset to this field
    private static final int OFFSET_MOVE_READY = 0x10;  // MIPS byte offset to this field
    private static final int OFFSET_ENERGY = 0x14;  // MIPS byte offset to this field
    private static final int OFFSET_NUMBER_LOCATIONS = 0x18;  // MIPS byte offset to this field
    private static final int OFFSET_PLAYER_COLOR = 0x1c;  // MIPS byte offset to this field
    private static final int OFFSET_SIZE_OF_TASK = 0x20;  // MIPS byte offset to this field
    private static final int OFFSET_LOC_ARRAY = 0x24;  // MIPS byte offset to this field
    private static final int OFFSET_TASK_COMPLETE = 0x124;  // MIPS byte offset to this field
    private static final int OFFSET_TASK_ARRAY = 0x128;  // MIPS byte offset to this field
    // Other MIPS memory locations are available to the player's use.

    private ScavengerHuntDisplay graphicArea;
    private int authenticationValue = 0;
    private boolean GameOn = false;  // MIPS programs readiness
    private static int SetWordCounter = 0;
    private static int accessCounter = 0;
    private static int playerID = ADMINISTRATOR_ID;   // Range 0...(NUM_PLAYERS-1), plus ADMINISTRATOR_ID
    private boolean KENVDEBUG = false;


    // Used to define (X,Y) coordinate of a location to which ScavengerHunt players
    // will travel.
    private class Location {
        public int X;
        public int Y;
    }

    // private inner class to provide the data on each player needed for display
    private class PlayerData {
        int whereAmIX = START_AND_END_LOCATION;   // Read only. Memory Address:  Base
        int whereAmIY = START_AND_END_LOCATION;   // Read only. Memory Address:  Base + 0x4
        //int moveToX;    //  Memory Address:  Base + 0x8
        //int moveToY;    //  Memory Address:  Base + 0xc
        //int goalX;     // Read only. Memory Address:  Base + 0x10
        //int goalY;     // Read only. Memory Address:  Base + 0x14
        int energy = 20;    // Read only. Memory Address:  Base + 0x18
        int color = 0;    // Memory Address:  Base + 0x1c
        long finishTime;
        //int locID;  // ID of the location to which ScavengerHunt players are headed. Not used by player.
        boolean hasVisitedLoc[] = new boolean[NUM_LOCATIONS];  // boolean: player has visited each location
        boolean finis = false;

        // Class PlayerData has no constructor

        public void setWhereAmI(int gX, int gY) {
            whereAmIX = gX;
            whereAmIY = gY;
        }

        //public void setGoal(int gX, int gY) {  goalX = gX; goalY = gY; }
        public void setEnergy(int e) {
            energy = e;
        }

        public void setColor(int c) {
            color = c;
        }

        public int getWhereAmIX() {
            return whereAmIX;
        }

        public int getWhereAmIY() {
            return whereAmIY;
        }

        public int getColor() {
            return color;
        }

        public boolean hasVisited(int i) {
            return hasVisitedLoc[i];
        }

        public void setVisited(int i) {
            hasVisitedLoc[i] = true;
        }

        public void setFinished() {
            finis = true;
        }

        public boolean isFinished() {
            return finis;
        }

        public long getFinishTime() {
            return finishTime;
        }

        public long getFinishMin() {
            return (finishTime / 60000);
        }   // Minutes portion of finishTime

        public long getFinishSec() {
            return (finishTime % 60000) / 1000;  // Seconds portion of finishTime
        }  // Seconds portion of finishTime

        public long getFinishMillisec() {
            return (finishTime % 1000);
        }  // Millisec portion of finishTime

        public void setFinishTime(long t) {
            finishTime = t;
        }

        //public int getGoalX() {  return goalX; }
        //public int getGoalY() {  return goalY; }
        //public int getMoveToX() {  return moveToX; }
        //public int getMoveToY() {  return moveToY; }
        public int getEnergy() {
            return energy;
        }
        //public int getLocationID() {  return locID; }
    } // end class PlayerData

    private static PlayerData[] pd = new PlayerData[NUM_PLAYERS];
    private static Location[] loc = new Location[NUM_LOCATIONS];
    private Random randomStream;
    private long startTime;


    // private inner class
    private class ScavengerHuntRunnable implements Runnable {
        JPanel panel;

        public ScavengerHuntRunnable() // constructor
        {
            // final JFrame frame = new JFrame("ScavengerHunt");
            // Recommended by Pete Sanderson, 2 Nov. 2006, so that the Tool window and
            // MARS window can be on the screen at the same time.
            final JDialog frame = new JDialog(Globals.getGui(), "ScavengerHunt");

            // System.out.println("ScavengerHuntRunnable.constructor: starting....");

            panel = new JPanel(new BorderLayout());
            graphicArea = new ScavengerHuntDisplay(GRAPHIC_WIDTH, GRAPHIC_HEIGHT);
            JPanel buttonPanel = new JPanel();
            JButton resetButton = new JButton("Reset");
            resetButton.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            graphicArea.clear();

                            // TBD ------- TBD
                            // Reset actions here
                            initializeScavengerData();
                            //JOptionPane.showMessageDialog(null, "Reset needs to be implemented!" );

                        }

                    });
            buttonPanel.add(resetButton);


            panel.add(graphicArea, BorderLayout.CENTER);
            panel.add(buttonPanel, BorderLayout.SOUTH);


            // Snippet by Pete Sanderson, 2 Nov. 2006, to be a window-closing sequence
            frame.addWindowListener(
                    new WindowAdapter() {
                        public void windowClosing(WindowEvent e) {
                            frame.setVisible(false);
                            frame.dispose();
                        }
                    });

            frame.getContentPane().add(panel);
            frame.setLocationRelativeTo(null);
            frame.pack();
            frame.setVisible(true);
            frame.setTitle(" This is the ScavengerHunt");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // TBD --- This should close only the Tool, not the entire MARS
            frame.setPreferredSize(new Dimension(GRAPHIC_WIDTH, GRAPHIC_HEIGHT)); // TBD  SIZE
            frame.setVisible(true); // show();

        } // end ScavengerHuntRunnable() constructor

        public void run() {

            double tempAngle;

            // infinite loop: play the Scavenger Hunt game
            do {

                // Pause to slow down the redisplay of the game. This is separate from
                // the execution speed of the MIPS program, so the display may lag behind
                // the state of the MIPS program.
                try {
                    // System.out.println(" Hello from the ScavengerHuntRunnable runner, sleeping here ...");
                    // System.out.print(".");
                    Thread.sleep(100);   // millisec
                } catch (InterruptedException exception) {// no action
                }

                panel.repaint(); // show new ScavengerHunt position
            } while (true);

        } // end run method of ScavengerHuntRunnable class

    } // end ScavengerHuntRunnable class
   
    /* ------------------------------------------------------------------------- */

    /**
     * ScavengerHuntDisplay does not have access to the same MIPS Memory class object used by
     * the ScavengerHunt class object. Need read-only access to a similar data structure maintained
     * within ScavengerHunt.
     */
    private class ScavengerHuntDisplay extends JPanel {
        private int width;
        private int height;
        private boolean clearTheDisplay = true;


        public ScavengerHuntDisplay(int tw, int th) {
            // System.out.println("ScavengerHuntDisplay.constructor: starting....");
            width = tw;
            height = th;

        }

        public void redraw() {
            repaint();
        }

        public void clear() {
            // clear the graphic display
            clearTheDisplay = true;
            //System.out.println("ScavengerHuntDisplay.clear: called to clear the display");
            repaint();
        }

        /**
         * paintComponent does not have access to the same MIPS Memory class object used by
         * the ScavengerHunt class object. Need read-only access to a similar data structure maintained
         * within ScavengerHunt.
         */
        public void paintComponent(Graphics g) {
            long tempN;
            int xCoord;
            int yCoord;

            // System.out.println("ScavengerHuntDisplay.paintComponent: I'm painting! n is " + n);


            // Recover Graphics2D
            Graphics2D g2 = (Graphics2D) g;

            if (!GameOn)    // Make sure game is ready before continuing
            {
                g2.setColor(Color.lightGray);
                g2.fillRect(0, 0, width - 1, height - 1); // Clear all previous drawn information
                g2.setColor(Color.black);
                g2.drawString(" ScavengerHunt not yet initialized by MIPS administrator program.",
                        100, 200);
                return;
            }

            // Clear all previous drawn information
            g2.setColor(Color.lightGray);
            g2.fillRect(0, 0, width - 1, height - 1);


            // Draw the locations to which the players will be moving
            // All players have the same location data.
            for (int i = 0; i < NUM_LOCATIONS; i++) {
                xCoord = loc[i].X; // toolReadPlayerData(0, OFFSET_LOC_ARRAY + (i*8) + 0);
                yCoord = loc[i].Y; // toolReadPlayerData(0, OFFSET_LOC_ARRAY + (i*8) + 4);
                g2.setColor(Color.blue);
                g2.fillRect(xCoord, yCoord, 20, 20);  // coord is upper left corner of oval
                g2.setColor(Color.white);
                g2.drawString(" " + i, xCoord + 4, yCoord + 15);  // coord is lower left corner of string text box
            
               /*
               System.out.println("ScavengerHuntDisplay.paintComponent: drew loc " + i + " at (" +
                                   xCoord +
                                   ", " +
                                   yCoord +
                                   ")" );
               */
            }

            //System.out.println("ScavengerHuntDisplay.paintComponent: special exit!");
            //System.exit(0);


            // Draw scoreboard
            g2.setColor(Color.black);
            g2.drawString("Player", width - 160, 30);
            g2.drawString("Locations", width - 110, 30);
            g2.drawString("Energy", width - 50, 30);
            g2.drawLine(width - 160, 35, width - 10, 35); // line under column headings
            g2.drawLine(width - 120, 35, width - 120, 35 + (NUM_PLAYERS * 15)); // vertical line for location-visited marks
            g2.drawLine(width - 50, 35, width - 50, 35 + (NUM_PLAYERS * 15)); // vertical line for location-visited marks
            for (int i = 0; i < NUM_PLAYERS; i++) {
                // Draw player's symbol
                g2.setColor(new Color(pd[i].getColor()));
                // g2.setColor(Color.red);  // TBD hardcoded


                xCoord = pd[i].getWhereAmIX();
                yCoord = pd[i].getWhereAmIY();

                // ystem.out.println("paintComponent loop " + i + ": Loc coord is (" + xCoord + ", " + yCoord);

                // Draw player symbol and label it with player ID number.
                // Hardcoded size of location graphic and label.
                g2.drawOval(xCoord, yCoord, 20, 20);  // coord is upper left corner of oval
                g2.drawString(" " + i, xCoord + 4, yCoord + 15);  // coord is lower left corner of string text box

                // Draw player's info on scoreboard
                g2.setColor(Color.black);
                g2.drawString(" " + i, width - 150, 50 + (i * 15));  // Player's ID on scoreboard
                g2.drawString(" " + pd[i].getEnergy(), width - 40, 50 + (i * 15));  // Player's energy on scoreboard

                // Display player's progress or finishing time, whichever is applicable
                if (pd[i].isFinished()) {
                    // Display finishing time
                    // This doesn't display leading zeroes to align time components (e.g. 27 millisec ought to print as 027)
                    g2.drawString(pd[i].getFinishMin() + ":" +  // Minutes
                                    pd[i].getFinishSec() + ":" +   // Seconds
                                    pd[i].getFinishMillisec(),   // Milliseconds
                            width - 115, 50 + (i * 15));
                    // System.out.println("Time is " +  pd[i].getFinishTime());
                    //g2.drawString (" " + pd[i].getFinishTime(),
                    //               width - 255, 50 + (i*15));
                } else  // player either has not finished or is just now finishing
                {
                    int visCount = 0;
                    for (int j = 0; j < NUM_LOCATIONS; j++) {
                        if (pd[i].hasVisited(j))  // count number of locations that player has visited
                        {
                            visCount++;
                        }
                    }
                    if (visCount == NUM_LOCATIONS)  // player has visited every location -- finished!
                    {
                        pd[i].setFinished();
                        pd[i].setFinishTime(System.currentTimeMillis() - startTime);

                    } else // player has not yet visited every location
                    {
                        // Display locations that the player has actually visited
                        for (int j = 0; j < NUM_LOCATIONS; j++) {
                            if (pd[i].hasVisited(j))  // Player has visited this location
                            {
                                g2.fillRect((width - 120) + (j * 10), 42 + (i * 15), 10, 8);
                            }
                        }
                    }
                } // end player had not previously finished

            } // end display score/results for each player

            // System.out.println("paintComponent: Player " + 0 + " is at (" + pd[0].getWhereAmIX() + ", " + pd[0].getWhereAmIY() + ")" );
         
         
         
         
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

    } // end private inner class ScavengerHuntDisplay
    /* ------------------------------------------------------------------------- */


    // A constructor for ScavengerHunt would be called immediately on MARS startup, perhaps
    // before all MARS classes have been created. Do not include any action in a constructor
    // but rather postpone any action until the action() class,
    // at which time all MARS classes will be ready for use.
    public ScavengerHunt() {
        // System.out.println("ScavengerHunt.constructor: starting....");
    }

    public String getName() {
        return "ScavengerHunt";
    }

    /*
     * This will set up the ScavengerHunt's GUI.  Invoked when ScavengerHunt menu item selected.
     */
    public void action() {


        ScavengerHuntRunnable shr = new ScavengerHuntRunnable();
        Thread t1 = new Thread(shr);
        t1.start();


        // Register as observer for a particular MIPS data range. Other ranges
        // are not used by this Tool.
        try {
            Globals.memory.addObserver(this, 0xffff8000, 0xfffffff0);  // must be on word boundaries
        } catch (AddressErrorException e) {
            System.out.println("\n\nScavengerHunt.action: Globals.memory.addObserver caused AddressErrorException.\n\n");
            System.exit(0);
        }

    } // end ScavengerHunt.action()

    /*
     * This method observes MIPS memory for directives to modify ScavengerHunt activity (that is,
     * MIPS program write to MMIO) and updates instance variables to reflect that directive.
     * This method takes action when it "observes" MIPS memory changes -- but this method
     * must not write to those memory locations in order to prevent an infinite cycle of events.
     * This method observes certain locations and then may (and does) write to OTHER locations.
     */
    public void update(Observable o, Object arg) {
        MemoryAccessNotice notice;
        int address;
        int data;
        boolean isWrite;
        boolean isRead;
        int energyLevel;

        // Here we are only interested in MemoryAccessNotice. For anything else, just return.
        if (!(arg instanceof MemoryAccessNotice))
            return;

        // Get pertinent information about this MemoryAccessNotice.
        notice = (MemoryAccessNotice) arg;
        address = notice.getAddress();
        data = notice.getValue();
        isWrite = (notice.getAccessType() == AccessNotice.WRITE);
        isRead = !isWrite;


        // If we are only interested in MIPS memory WRITES, then just return on READS.
        // That's a matter of policy: perhaps players should be prohibited from
        // reading each other's memory spaces.
        if (!isWrite)
            return;

        //System.out.println("ScavengerHunt.update: observed write access by player " + playerID + " on Mem[ " +
        //            Binary.intToHexString(address) + " ]");

        // TBD TBD DEBUGGING SPECIAL
        /*
            accessCounter++;
            if (accessCounter > 100000)
            {
              System.out.println("\n\nScavengerHunt.update: hardcoded exit to prevent runaway" );
              System.exit(0);
            }
            */
        // TBD TBD DEBUGGING SPECIAL


        // Take the appropriate action, depending on data written and priority of user.
        if (isWrite && playerID == ADMINISTRATOR_ID && address == ADDR_GAME_ON) {

            // ADMINISTRATOR_ID can write to any location, because it's trusted software
            //System.out.println( "ScavengerHunt.update(): Administrator wrote to  Mem[ " +
            //   Binary.intToHexString(address) + " ] == " + Binary.intToHexString(data) );

            // No need to authenticate since administrator runs first, then
            // this location has no effect thereafter.
            GameOn = true;
            // System.out.println( "ScavengerHunt.update(): Administrator wrote GAME_ON!" );

            initializeScavengerData();
        } else if (isWrite && address == ADDR_AUTHENTICATION) {
            // Anyone is allowed to write to the authentication location -- but if that value is not
            // correct (authentic) then action can be taken.
            // NO ACTION HERE
        } else if (isWrite && address == ADDR_NUM_TURNS) {
            // Anyone is allowed to write to the "number of turns" location
            // NO ACTION HERE
        } else if (isWrite && address == ADDR_PLAYER_ID)   // if the data written will change the PlayerID, authenticate the write
        {
            // 2006 Oct 31  dummy validation scheme, suitable for distribution
            // to students for development: Initial authentication value is zero.
            // Each successive authentication value is one greater
            // than the preceding value, modulo 0xffffffff.
            authenticationValue += 1;  // "server's" updated version of the authenticationValue
            if (toolGetWord(ADDR_AUTHENTICATION) == authenticationValue) // Compare to "client's" version of the authenticationValue
            {
                playerID = toolGetWord(ADDR_PLAYER_ID); // Use the new player ID
                //System.out.println( "ScavengerHunt.update(): New playerID of " + playerID);
            } else {
                System.out.println("ScavengerHunt.update(): Invalid write of player ID! \nPlayer " +
                        playerID + " tried to write.  Expected:   " +
                        Binary.intToHexString(authenticationValue) +
                        ", got:  " + Binary.intToHexString(toolGetWord(ADDR_AUTHENTICATION)) + "\n");
            }
        } else if (isWrite &&
                address == (ADDR_BASE + (playerID * MEM_PER_PLAYER) + OFFSET_MOVE_READY) &&
                data != 0)  //  Player wrote data to his/her assigned MoveReady location
        {
            /*
         System.out.println(" ******** ScavengerHunt.update: Player " + playerID + " requests move to (" +
                        toolReadPlayerData(playerID, OFFSET_MOVE_TO_X) + ", " +
                        toolReadPlayerData(playerID, OFFSET_MOVE_TO_Y) + ")" );
            */

            energyLevel = toolReadPlayerData(playerID, OFFSET_ENERGY); // find if player has energy
            if (energyLevel <= 0)  // No energy. Player not allowed to move
            {
                //JOptionPane.showMessageDialog(null, "Player " + playerID + " can't move -- no energy.\n" +
                //                                    "(This msg. in ScavengerHunt.update()" );
                // System.out.println("Player " + playerID + " can't move -- no energy.");
                return;
            }

            if (toolReadPlayerData(playerID, OFFSET_MOVE_TO_X) < 0 ||
                    toolReadPlayerData(playerID, OFFSET_MOVE_TO_X) > GRAPHIC_WIDTH ||
                    toolReadPlayerData(playerID, OFFSET_MOVE_TO_Y) < 0 ||
                    toolReadPlayerData(playerID, OFFSET_MOVE_TO_Y) > GRAPHIC_HEIGHT
                    )  // Out of bounds. Player not allowed to move
            {
                //JOptionPane.showMessageDialog(null, "Player " + playerID + " can't move -- out of bounds.\n" +
                //                                    "(This msg. in ScavengerHunt.update()" );
                System.out.println("Player " + playerID + " can't move -- out of bounds.");
                return;
            }


            // Verify movement is allowed (does not exceed maximum movement)
            if (Math.sqrt(
                    Math.pow(toolReadPlayerData(playerID, OFFSET_WHERE_AM_I_X) -
                            toolReadPlayerData(playerID, OFFSET_MOVE_TO_X), 2.0)
                            +
                            Math.pow(toolReadPlayerData(playerID, OFFSET_WHERE_AM_I_Y) -
                                    toolReadPlayerData(playerID, OFFSET_MOVE_TO_Y), 2.0))
                    <= MAX_MOVE_DISTANCE) {

                // Write the new position of the player
                toolWritePlayerData(playerID, OFFSET_WHERE_AM_I_X,
                        toolReadPlayerData(playerID, OFFSET_MOVE_TO_X));
                toolWritePlayerData(playerID, OFFSET_WHERE_AM_I_Y,
                        toolReadPlayerData(playerID, OFFSET_MOVE_TO_Y));
                pd[playerID].setWhereAmI(toolReadPlayerData(playerID, OFFSET_WHERE_AM_I_X),
                        toolReadPlayerData(playerID, OFFSET_WHERE_AM_I_Y));

                // Write the new (reduced) energy of the player
                // Policy: Constant ENERGY_PER_MOVE for any move regardless of length.
                toolWritePlayerData(playerID, OFFSET_ENERGY,
                        toolReadPlayerData(playerID, OFFSET_ENERGY) - ENERGY_PER_MOVE);
                pd[playerID].setEnergy(toolReadPlayerData(playerID, OFFSET_ENERGY));


                // TBD FUTURE --- need to keep track of locations that the player has actually got to
                // -- be able to tell that the player has reached a certain location
                // -- be able to tell that the player has reached every location
                for (int i = 0; i < NUM_LOCATIONS; i++) {
                    if (toolReadPlayerData(playerID, OFFSET_WHERE_AM_I_X) == loc[i].X &&
                            toolReadPlayerData(playerID, OFFSET_WHERE_AM_I_Y) == loc[i].Y) {
                        pd[playerID].setVisited(i);  // Player has visited this location
                    }
                }


                // Write 0 to "move ready" location, signifying that the move request was processed
                // Here we must write to the same location that we're now reading from, and we
                // can't cause an infinite loop. Temporarily switch player ID to that of the administrator,
                // and restore ID after the write. With the playerID set to administrator, the event
                // caused by the write will not go through this same logic.
                int tempPlayerID = playerID;
                playerID = ADMINISTRATOR_ID;
                toolWritePlayerData(tempPlayerID, OFFSET_MOVE_READY, 0);
                playerID = tempPlayerID;

            } else {
                System.out.println("Player " + playerID + " can't move -- exceeded max. movement.");
                System.out.println("    Player is at (" +
                        toolReadPlayerData(playerID, OFFSET_WHERE_AM_I_X) + ", " +
                        toolReadPlayerData(playerID, OFFSET_WHERE_AM_I_Y) + "), wants to go to (" +
                        toolReadPlayerData(playerID, OFFSET_MOVE_TO_X) + "," +
                        toolReadPlayerData(playerID, OFFSET_MOVE_TO_Y) + ")");

                return;
            }

        } // end if Player wrote nonzero data to his/her assigned MoveReady location


        else if (isWrite &&
                address == (ADDR_BASE + (playerID * MEM_PER_PLAYER) + OFFSET_TASK_COMPLETE) &&
                data != 0)  //  Player wrote data to his/her assigned TaskComplete location
        {

            //  System.out.println(" ******** ScavengerHunt.update: Player " + playerID + " requests more energy (task complete)" );

            // Player indicates he/she has completed a task. Check to see if task is completed correctly.
            // Task for this assignment: Numbers are sorted in ascending order.
            int prevData, currentData;
            prevData = toolReadPlayerData(playerID, OFFSET_TASK_ARRAY);
            for (int i = 1; i < SIZE_OF_TASK; i++) {
                currentData = toolReadPlayerData(playerID, OFFSET_TASK_ARRAY + (i * 4));
                if (prevData > currentData) {
                    // Task failure! Task not completed correctly!
                    System.out.println("Whoops! Player has NOT completed task correctly");
                    return;
                }
                prevData = currentData; // update for next iteration

            }

            // If program flow has reached this point, the task is completed correctly.
            // Award energy, reset TaskComplete, and set new task values for future use.
            toolWritePlayerData(playerID, OFFSET_ENERGY, ENERGY_AWARD);
            toolWritePlayerData(playerID, OFFSET_TASK_COMPLETE, 0);
            for (int j = 0; j < SIZE_OF_TASK; j++)   // Initialize the task data for this player
            {
                toolWritePlayerData(playerID, OFFSET_TASK_ARRAY + (j * 4),
                        (int) (randomStream.nextDouble() * Integer.MAX_VALUE));   // Set a random number for the task (sort them)
            }
            // System.out.println("Player has  completed task correctly and been awarded energy");
            pd[playerID].setEnergy(ENERGY_AWARD);

        } // end if Player wrote nonzero data to his/her assigned TaskComplete location


        else if (isWrite &&
                address == (ADDR_BASE + (playerID * MEM_PER_PLAYER) + OFFSET_PLAYER_COLOR))
        //  Player wrote data to his/her assigned PlayerColor location
        {
            // PPlayer indicates he/she has changed the color of display
            pd[playerID].setColor(toolReadPlayerData(playerID, OFFSET_PLAYER_COLOR));
        }


        // TBD TBD TBD
        // TBD TBD TBD
        // TBD TBD TBD
        // Yet to be implemented: Enforce only one write of MoveRequest per player per turn


        else if (isWrite &&
                address >= (ADDR_BASE + (playerID * MEM_PER_PLAYER)) &&
                address < (ADDR_BASE + ((playerID + 1) * MEM_PER_PLAYER)))
        //  Player wrote data elsewhere within his/her assigned location
        {
            // Player can write to any location within his/her assigned location
            //System.out.println( "ScavengerHunt.update(): Player " + playerID + " wrote to valid location");
        } else if (isWrite && playerID == ADMINISTRATOR_ID) {
            // ADMINISTRATOR_ID can write to any location, because it's trusted software
            //System.out.println( "ScavengerHunt.update(): Administrator wrote to  Mem[ " +
            //     Binary.intToHexString(address) + " ] == " + Binary.intToHexString(data) );
        } else if (isWrite) {
            // This player is writing outside his/her assigned memory location
                  /*
            System.out.println("ScavengerHunt.update(): Player " + playerID + " writing outside assigned mem. loc. at address " +
                                Binary.intToHexString(address) +
                                " -- not implemented!");
                    */

            JOptionPane.showMessageDialog(null,
                    "ScavengerHunt.update(): Player " + playerID + " writing outside assigned mem. loc. at address " +
                            Binary.intToHexString(address) +
                            " -- not implemented!");
        } else if (isRead) {
            // Policy: anyone can read any location.
        }


    } // end ScavengerHunt.update()


    // Write one word to MIPS memory. This is a wrapper to isolate the try..catch blocks.
    private void toolSetWord(int address, int data) {

        if (KENVDEBUG) {
            System.out.println("   ScavengerHunt.toolSetWord: Setting MIPS Memory[" +
                    Binary.intToHexString(address) + "] to " + Binary.intToHexString(data) + " = " + data);
        }
        SetWordCounter++;

        try {
            Globals.memory.setWord(address, data); // Write
        } catch (AddressErrorException e) {
            System.out.println("ScavengerHunt.toolSetWord: deliberate exit on AEE exception.");
            System.out.println("     SetWordCounter = " + SetWordCounter);
            System.out.println("     address = " + Binary.intToHexString(address));
            System.out.println("     data = " + data);
            System.exit(0);
        } catch (Exception e) {
            System.out.println("ScavengerHunt.toolSetWord: deliberate exit on " + e.getMessage() + " exception.");
            System.out.println("     SetWordCounter = " + SetWordCounter);
            System.out.println("     address = " + Binary.intToHexString(address));
            System.out.println("     data = " + data);
            System.exit(0);
        }

        if (KENVDEBUG) {
            // Verify data written correctly
            int verifyData = toolGetWord(address);
            if (verifyData != data) {
                System.out.println("\n\nScavengerHunt.toolSetWord: Can't verify data! Special exit.");
                System.out.println("     address = " + Binary.intToHexString(address));
                System.out.println("     data = " + data);
                System.out.println("     verifyData = " + verifyData);
                System.exit(0);
            } else
                System.out.println("  ScavengerHunt.toolSetWord: Mem[" +
                        Binary.intToHexString(address) +
                        " verified as " + Binary.intToHexString(data));
        }

    } // end toolSetWord


    // Read one word from MIPS memory. This is a wrapper to isolate the try..catch blocks.
    private int toolGetWord(int address) {

        int returnValue;

        //System.out.println("ScavengerHunt.toolGetWord: called with address " +
        //   Binary.intToHexString(address) );

        try {
         /*
         System.out.println("ScavengerHunt.toolGetWord: returning " +
           Binary.intToHexString(Globals.memory.getWord(address)) +
           " which is at MIPS Memory[" + Binary.intToHexString(address) + "]" );
         */
            returnValue = Globals.memory.getWord(address);
         
         /*
         System.out.println("ScavengerHunt.toolGetWord: Mem[" +
          Binary.intToHexString(address) + "] = " +
          Binary.intToHexString(returnValue) + " --- returning normally");
         */

            return returnValue;
        } catch (AddressErrorException e) {
            System.out.println("ScavengerHunt.toolGetWord: deliberate exit on AEE exception.");
            System.out.println("     SetWordCounter = " + SetWordCounter);
            System.out.println("     address = " + Binary.intToHexString(address));
            System.exit(0);
        } catch (Exception e) {
            System.out.println("ScavengerHunt.toolGetWord: deliberate exit on " + e.getMessage() + " exception.");
            System.out.println("     SetWordCounter = " + SetWordCounter);
            System.out.println("     address = " + Binary.intToHexString(address));
            System.exit(0);
        }

        return 0; // Must have return statement
    } // end toolGetWord


    // Read player's data field.
    private int toolReadPlayerData(int p, int offset) {

        if (KENVDEBUG) {
            System.out.println("ScavengerHunt.toolReadPlayerData: called with player " + p +
                    ", offset = " +
                    Binary.intToHexString(offset) + " ---> address " +
                    Binary.intToHexString(ADDR_BASE + (p * MEM_PER_PLAYER) + offset)
            );
        }

        int returnValue = toolGetWord(ADDR_BASE + (p * MEM_PER_PLAYER) + offset);

        if (KENVDEBUG) {
            //if ((ADDR_BASE + (p * MEM_PER_PLAYER) + offset) >= 0xffff8000 &&
            //     (ADDR_BASE + (p * MEM_PER_PLAYER) + offset) < 0xffff8800)  // Show debug for player 0 and 1 only
            //{
            //System.out.println("   ScavengerHunt.toolReadPlayerData: Reading MIPS Memory[" +
            //    Binary.intToHexString(ADDR_BASE + (p * MEM_PER_PLAYER) + offset) +
            //    "] which is " + Binary.intToHexString(returnValue) +
            //   " = " + Binary.intToHexString( returnValue) );
            //}

            System.out.println("ScavengerHunt.toolReadPlayerData: Mem[" +
                    Binary.intToHexString(ADDR_BASE + (p * MEM_PER_PLAYER) + offset) + "] = " +
                    Binary.intToHexString(returnValue) + " --- returning normally");
        }

        return returnValue;
    } // end toolReadPlayerData

    // Write player's data field.
    private void toolWritePlayerData(int p, int offset, int data) {

        int address = ADDR_BASE + (p * MEM_PER_PLAYER) + offset;

        if (KENVDEBUG) {
            System.out.println("ScavengerHunt.toolWritePlayerData: called with player " + p +
                    ", offset = " + Binary.intToHexString(offset) +
                    ", data = " + Binary.intToHexString(data));
        }

        toolSetWord(address, data);

        if (KENVDEBUG) {
            int verifyData = toolGetWord(address);
            if (data != verifyData) {
                System.out.println("\n\nScavengerHunt.toolWritePlayerData: MAYDAY data not verified !");
                System.out.println("      requested data to be written was " + Binary.intToHexString(data));
                System.out.println("      actual data at that loc is " + Binary.intToHexString(toolGetWord(address)));
                System.exit(0);
            } else
                System.out.println("  ScavengerHunt.toolWritePlayerData: Mem[" +
                        Binary.intToHexString(address) +
                        " verified as " + Binary.intToHexString(data));
        }

    } // end toolWritePlayerData


    private void initializeScavengerData() {
        //GameOn = false;  // MIPS programs readiness
        authenticationValue = 0;
        playerID = ADMINISTRATOR_ID;
        startTime = System.currentTimeMillis();  // Clock time for program run

        // This is a dubious use of the tool (this Java program) to initialize data values for the game.
        // The administrator portion of the MIPS program should initialize all data for each
        // player -- but it's easier to work with random numbers in the Java program
        // Initialize the locations and task data for each player
        randomStream = new Random(42);  // TBD Use a seed for development. Remove seed for randomizing.

        for (int j = 0; j < NUM_LOCATIONS - 1; j++) // The first (n-1) locations are "random"
        {  // Two coordinates (x and y) for each location
            loc[j] = new Location(); // Initialize each location element
            loc[j].X = (int) (randomStream.nextDouble() * GRAPHIC_WIDTH); // X coord.
            loc[j].Y = (int) (randomStream.nextDouble() * (GRAPHIC_HEIGHT - 50)); // Y coord. (leave room for buttons in window)
         
                    /*
                    System.out.println("ScavengerHunt.update(): set up a location at (" +
                                        loc[j].X +
                                        ", " +
                                        loc[j].Y +
                                        ")" );
                    */

        }
        loc[NUM_LOCATIONS - 1] = new Location();  // The last location is a return to the starting position
        loc[NUM_LOCATIONS - 1].X = START_AND_END_LOCATION;
        loc[NUM_LOCATIONS - 1].Y = START_AND_END_LOCATION;

        for (int i = 0; i < NUM_PLAYERS; i++)  // Initialize data for each player
        {

            //System.out.println("ScavengerHunt.update(): Player loop " + i);

            pd[i] = new PlayerData();  // Initialize each player data structure.

            for (int j = 0; j < NUM_LOCATIONS; j++)  // Initialize the locations this player goes to
            {
                toolWritePlayerData(i, OFFSET_LOC_ARRAY + (j * 8) + 0, loc[j].X);  // Set the same locations for each player
                toolWritePlayerData(i, OFFSET_LOC_ARRAY + (j * 8) + 4, loc[j].Y);  // Set the same locations for each player
            }

            for (int j = 0; j < SIZE_OF_TASK; j++)   // Initialize the task data for this player
            {
                toolWritePlayerData(i, OFFSET_TASK_ARRAY + (j * 4),
                        (int) (randomStream.nextDouble() * Integer.MAX_VALUE));   // Set a random number for the task (sort them)
            }
        }

    } // end initializeScavengerData

} // end ScavengerHunt

