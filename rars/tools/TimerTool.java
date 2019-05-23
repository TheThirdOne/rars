package rars.tools;

import rars.Globals;
import rars.riscv.hardware.AccessNotice;
import rars.riscv.hardware.MemoryAccessNotice;
import rars.riscv.hardware.Memory;
import rars.riscv.hardware.InterruptController;
import rars.riscv.hardware.ControlAndStatusRegisterFile;
import rars.riscv.hardware.AddressErrorException;

import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class TimerTool extends AbstractToolAndApplication {
    private static String heading = "Timer Tool";
    private static String version = "Version 1.0 (Zachary Selk)";
    private static final int TIME_ADDRESS = Memory.memoryMapBaseAddress + 0x18;
    private static final int TIME_CMP_ADDRESS = Memory.memoryMapBaseAddress + 0x20;

    // GUI Interface
    private static JPanel panelTools;
    private TimePanel timePanel;

    // Internal time values
    private static long time = 0L;
    private static TimeCmpDaemon timeCmp = null;
    private static long startTime = 0L;
    private static long savedTime = 0L;
    private Timer timer = new Timer();
    private Tick tick = new Tick();

    // Internal timing flags
    private static boolean postInterrupt = false;
    private static boolean updateTime = false;
    private static boolean running = false;

    public TimerTool() {
        super(heading + ", " + version, heading);
        startTimeCmpDaemon();
    }

    public TimerTool(String title, String heading) {
        super(title, heading);
        startTimeCmpDaemon();
    }

    public static void main(String[] args) {
        new TimerTool(heading + ", " + version, heading);
    }

    @Override
    public String getName() {
        return "Timer Tool";
    }

    protected JComponent buildMainDisplayArea() {
        JPanel panelTools = new JPanel(new GridLayout(1, 2));
        timePanel = new TimePanel();
        panelTools.add(timePanel);
        return panelTools;
    }

    // A daemon that watches the timecmp MMIO for any changes
    private void startTimeCmpDaemon() {
        if (timeCmp == null) {
            timeCmp = new TimeCmpDaemon();
        }
    }

    public void start() {
        updateTime = true;
        startTime = System.currentTimeMillis();
        if (!running) {
            // Start a timer that checks to see if a timer interupt needs to be raised
            // every millisecond
            timer.schedule(tick, 0, 1);
            running = true;
        }
    }

    public void puase() {
        updateTime = false;
        time = savedTime + System.currentTimeMillis() - startTime;
        savedTime = time;
    }

    public void stop() {
        updateTime = false;
        timer.cancel();
        running = false;
        reset();
    }

    protected void reset() {
        time = 0L;
        savedTime = 0L;
        startTime = System.currentTimeMillis();
        tick.updateTimecmp = true;
        timePanel.updateTime();
    }

    protected void performSpecialClosingDuties() {
        stop();
    }

    /*****************************  Timer Classes  *****************************/

    public class TimeCmpDaemon implements Observer {
        public boolean postInterrupt = false;
        public long value = 0L;

        public TimeCmpDaemon() {
            addAsObserver();
        }

        public void addAsObserver() {
            try {
                Globals.memory.addObserver(this, TIME_CMP_ADDRESS, TIME_CMP_ADDRESS+8);
            } catch (AddressErrorException aee) {
                System.out.println("Error while adding observer in Timer Tool");
                System.exit(0);
            }
        }

        public void update(Observable ressource, Object accessNotice) {
            MemoryAccessNotice notice = (MemoryAccessNotice) accessNotice;
            int accessType = ((AccessNotice)accessNotice).getAccessType();
            // If is was a WRITE operation
            if (accessType == 1) {
                int address = notice.getAddress();
                int value = notice.getValue();
                if (address == TIME_CMP_ADDRESS) {
                    this.value = ((this.value >> 32) << 32) + value;
                    postInterrupt = true;
                }
                else if (address == TIME_CMP_ADDRESS+4) {
                    this.value = (this.value & 0xFFFFFFFF) + (value << 32);
                    postInterrupt = true;
                }
            }
        }
    }

    private class Tick extends TimerTask {
        public volatile boolean updateTimecmp = true;
        public void run() {
            if (connectButton != null && connectButton.isConnected()) {
                if (updateTime) {
                    time = savedTime + System.currentTimeMillis() - startTime;

                    updateMMIOControlAndData(TIME_ADDRESS, (int)(time & 0xFFFFFFFF));
                    updateMMIOControlAndData(TIME_ADDRESS+4, (int)(time >> 32));
                    if (time >= timeCmp.value && timeCmp.postInterrupt) {
                        InterruptController.registerTimerInterrupt(ControlAndStatusRegisterFile.TIMER_INTERRUPT);
                        timeCmp.postInterrupt = false;
                    }
                    timePanel.updateTime();
                }
            }
            else {
                time = savedTime + System.currentTimeMillis() - startTime;
                //savedTime = time;
                startTime = System.currentTimeMillis();
            }
        }
    }

    private synchronized void updateMMIOControlAndData(int dataAddr, int dataValue) {
        //if (!this.isBeingUsedAsATool || (this.isBeingUsedAsATool && connectButton.isConnected())) {
        synchronized (Globals.memoryAndRegistersLock) {
            try {
                Globals.memory.setRawWord(dataAddr, dataValue);
            } catch (AddressErrorException aee) {
                System.out.println("Tool author specified incorrect MMIO address!" + aee);
                System.exit(0);
            }
        }
    }


    /*****************************  GUI Classes  *******************************/

    public class TimePanel extends JPanel {
        JLabel currentTime = new JLabel("Hello world");
        public TimePanel() {
            FlowLayout fl = new FlowLayout();
            this.setLayout(fl);
            this.add(currentTime);
            updateTime();
            start();
        }

        public void updateTime() {
            currentTime.setText(String.format("%02d:%02d.%02d", time/60000, (time/1000)%60, time%100));
        }
    }

    protected JComponent getHelpComponent() {
        final String helpContent =
            "Use this tool to simulate the Memory Mapped IO (MMIO) for a timing device allowing the program to utalize timer interupts. " +
            "While this tool is connected to the program it runs a clock (starting from time 0), storing the time in milliseconds. " +
            "The time is stored as a 64 bit integer and can be accessed (using a lw instruction) at 0xFFFF0018 for the lower 32 bits and 0xFFFF001B for the upper 32 bits.\n\n" +
            "Three things must be done before an interrupt can be set:\n" +
            " The address of your interrupt handler must be stored in the utvec CSR\n" +
            " The fourth bit of the uie CSR must be set to 1 (ie. ori uie, uie, 0x10)\n" +
            " The zeroth bit of the ustatus CSR must be set to 1 (ie. ori ustatus, ustatus, 0x1)\n" +
            "To set the timer you must write the time that you want the timer to go off (called timecmp) as a 64 bit integer at the address of 0xFFFF0020 for the lower 32 bits and 0xFFFF0024 for the upper 32 bits. " +
            "An interrupt will occur when the time is greater than or equal to timecmp which is a 64 bit integer (interpreted as milliseconds) stored at 0xFFFF0020 for the lower 32 bits and 0xFFFF0024 for the upper 32 bits. " +
            "To set the timer you must set timecmp (using a sw instruction) to be the time that you want the timer to go off at.\n\n" +
            "Note: the timer will only go off once after the time is reached and is not rearmed until timecmp is writen to again. " +
            "So if you are writing 64 bit values (opposed to on 32) then to avoid spuriously triggering a timer interrupt timecmp should be written to as such\n" +
            "    # a0: lower 32 bits of time\n" +
            "    # a1: upper 32 bits of time\n" +
            "    li  t0, -1\n" +
            "    la t1, timecmp\n" +
            "    sw t0, 0(t1)\n" +
            "    sw a1, 4(t1)\n" +
            "    sw a0, 0(t0)\n\n\n" +
            "(contributed by Zachary Selk, zrselk@gmail.com)";
        JButton help = new JButton("Help");
        help.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JTextArea ja = new JTextArea(helpContent);
                        ja.setRows(20);
                        ja.setColumns(60);
                        ja.setLineWrap(true);
                        ja.setWrapStyleWord(true);
                        JOptionPane.showMessageDialog(theWindow, new JScrollPane(ja),
                                "Simulating a timing device", JOptionPane.INFORMATION_MESSAGE);
                    }
                });
        return help;
    }
}
