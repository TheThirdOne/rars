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
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JComponent;

public class TimerTool extends AbstractToolAndApplication {
    private static String heading = "Timer Tool";
    private static String version = "Version 1.0";
    private static final int TIME_ADDRESS = Memory.memoryMapBaseAddress + 0x18;
    private static final int TIME_CMP_ADDRESS = Memory.memoryMapBaseAddress + 0x20;

    // GUI Interface
    private static JPanel panelTools;
    private TimePanel timePanel;

    // Internal time values
    private static long time = 0L;
    //private static long timeCmp = 0L;
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
        System.out.println("created");
    }

    public TimerTool(String title, String heading) {
        super(title, heading);
        startTimeCmpDaemon();
        System.out.println("created");
    }

    public static void main(String[] args) {
        new TimerTool(heading + ", " + version, heading);
    }

    @Override
    public String getName() {
        return "Timer Tool";
    }

    protected void addAsObserver() {
        //System.out.println("Added");
        //addAsObserver(TIME_ADDRESS, TIME_ADDRESS+8);
        //addAsObserver(TIME_CMP_ADDRESS, TIME_CMP_ADDRESS+8);
    }

    public void update(Observable ressource, Object accessNotice) {
        // MemoryAccessNotice notice = (MemoryAccessNotice) accessNotice;
        // int accessType = ((AccessNotice)accessNotice).getAccessType();
        // System.out.println("Update");
        // // If is was a WRITE operation
        // if (accessType == 1) {
        //     int address = notice.getAddress();
        //     int value = notice.getValue();
        //     if (address == TIME_CMP_ADDRESS) {
        //         timeCmp = ((timeCmp >> 32) << 32) + value;
        //         postInterrupt = true;
        //     }
        //     else if (address == TIME_CMP_ADDRESS+4) {
        //         timeCmp = (timeCmp & 0xFFFFFFFF) + (value << 32);
        //         postInterrupt = true;
        //     }
        // }
    }

    protected void reset() {
        time = 0L;
        //timeCmp = 0L;
        savedTime = 0L;
        startTime = System.currentTimeMillis();
        tick.updateTimecmp = true;
        timePanel.updateTime();
    }

    protected JComponent buildMainDisplayArea() {
        JPanel panelTools = new JPanel(new GridLayout(1, 2));
        timePanel = new TimePanel();
        panelTools.add(timePanel);
        return panelTools;
    }

    private void startTimeCmpDaemon() {
        if (timeCmp == null) {
            timeCmp = new TimeCmpDaemon();
            System.out.println("Daemon");
        }
    }

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
                System.out.println(value);
            }
        }
    }

    private class Tick extends TimerTask {
        public volatile boolean updateTimecmp = true;
        public void run() {
            // if (connectButton != null) {
            //     System.out.printf("%b, %b\n", connectButton.isConnected(), updateTime);
            // } else {
            //     System.out.printf("%b", updateTime);
            // }
            // Short circuit the arguments because connectButton is instatiated durring runtime
            if (connectButton != null && connectButton.isConnected()) {
                // if (!updateTimecmp) {
                //     try {
                //         timeCmp = Globals.memory.getWord(TIME_CMP_ADDRESS) + Globals.memory.getWord(TIME_CMP_ADDRESS+4) << 32; 
                //         updateTimecmp = false;
                //     } catch (AddressErrorException aee) {
                //         System.out.println("Tool author specified incorrect MMIO address!!!" + aee);
                //         System.exit(0);
                //     }
                // }
                // System.out.println(time);
                if (updateTime) {
                    time = savedTime + System.currentTimeMillis() - startTime;

                    updateMMIOControlAndData(TIME_ADDRESS, (int)(time));
                    updateMMIOControlAndData(TIME_ADDRESS+4, (int)(time >> 32));
                    if (time >= timeCmp.value && timeCmp.postInterrupt) {
                        InterruptController.registerTimerInterrupt(ControlAndStatusRegisterFile.TIMER_INTERRUPT);
                        timeCmp.postInterrupt = false;
                    }
                    timePanel.updateTime();
                }
            }
            else {
                savedTime = time;
                startTime = System.currentTimeMillis();
            }
        }
    }

    public void start() {
        System.out.printf("Starting %b\n", running);
        updateTime = true;
        startTime = System.currentTimeMillis();
        if (!running) {
            timer.schedule(tick, 0, 1);
            running = true;
        }
    }

    public void puase() {
        updateTime = false;
        savedTime = time;
    }

    public void stop() {
        System.out.println("Stopping");
        updateTime = false;
        timer.cancel();
        running = false;
        reset();
    }



    /***************************************************************************
     **************************************************************************/

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
            currentTime.setText(Long.toString(time) + " ms");
        }
    }

    private synchronized void updateMMIOControlAndData(int dataAddr, int dataValue) {
        //if (!this.isBeingUsedAsATool || (this.isBeingUsedAsATool && connectButton.isConnected())) {
        synchronized (Globals.memoryAndRegistersLock) {
            try {
                Globals.memory.setByte(dataAddr, dataValue);
            } catch (AddressErrorException aee) {
                // TODO Write to time MMIO
                System.out.println("Tool author specified incorrect MMIO address!" + aee);
                System.exit(0);
            }
        }
        //if (Globals.getGui() != null && Globals.getGui().getMainPane().getExecutePane().getTextSegmentWindow().getCodeHighlighting()) {
        //Globals.getGui().getMainPane().getExecutePane().getDataSegmentWindow().updateValues();
        //}
        //}
    }
}
