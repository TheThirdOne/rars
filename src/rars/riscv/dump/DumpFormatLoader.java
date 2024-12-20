package rars.riscv.dump;

import rars.util.FilenameFinder;

import java.lang.reflect.Modifier;
import java.util.ArrayList;


/* This class provides functionality to bring external memory dump format definitions
 * into RARS.  This is adapted from the ToolLoader class, which is in turn adapted
 * from Bret Barker's GameServer class from the book "Developing Games In Java".
 */

public class DumpFormatLoader {

    private static final String CLASS_PREFIX = "rars.riscv.dump.";
    private static final String DUMP_DIRECTORY_PATH = "rars/riscv/dump";
    private static final String CLASS_EXTENSION = "class";

    private static ArrayList<DumpFormat> formatList = null;

    static {
        formatList = new ArrayList<>();
        // grab all class files in the dump directory
        ArrayList<String> candidates = FilenameFinder.getFilenameList(DumpFormatLoader.class.getClassLoader(),
                DUMP_DIRECTORY_PATH, CLASS_EXTENSION);
        for (String file : candidates) {
            try {
                // grab the class, make sure it implements DumpFormat, instantiate, add to list
                String formatClassName = CLASS_PREFIX + file.substring(0, file.indexOf(CLASS_EXTENSION) - 1);
                Class clas = Class.forName(formatClassName);
                if (DumpFormat.class.isAssignableFrom(clas) &&
                        !Modifier.isAbstract(clas.getModifiers()) &&
                        !Modifier.isInterface(clas.getModifiers())) {
                    formatList.add((DumpFormat) clas.newInstance());
                }
            } catch (Exception e) {
                System.out.println("Error instantiating DumpFormat from file " + file + ": " + e);
            }
        }
    }


    /**
     * Dynamically loads dump formats into an ArrayList.  This method is adapted from
     * the loadGameControllers() method in Bret Barker's GameServer class.
     * Barker (bret@hypefiend.com) is co-author of the book "Developing Games
     * in Java".
     *
     * @see rars.riscv.SyscallLoader
     * @see rars.venus.ToolLoader
     * @see rars.riscv.InstructionSet
     */

    public static ArrayList<DumpFormat> getDumpFormats() {
        return formatList;
    }

    public static DumpFormat findDumpFormatGivenCommandDescriptor(String formatCommandDescriptor) {
        for (DumpFormat f : formatList) {
            if (f.getCommandDescriptor().equals(formatCommandDescriptor)) {
                return f;
            }
        }
        return null;
    }


}
