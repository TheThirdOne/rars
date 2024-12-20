package rars.riscv.dump;

import rars.riscv.hardware.AddressErrorException;
import rars.riscv.hardware.Memory;

import java.io.File;
import java.io.IOException;

/**
 * Interface for memory dump file formats.  All RARS needs to be able
 * to do is save an assembled program or data in the specified manner for
 * a given format.  Formats are specified through classes
 * that implement this interface.
 *
 * @author Pete Sanderson
 * @version December 2007
 */


public interface DumpFormat {

    /**
     * Get the file extension associated with this format.
     *
     * @return String containing file extension -- without the leading "." -- or
     * null if there is no standard extension.
     */
    String getFileExtension();

    /**
     * Get a short description of the format, suitable
     * for displaying along with the extension, if any, in the file
     * save dialog and also for displaying as a tool tip.
     *
     * @return String containing short description to go with the extension
     * or as tool tip when mouse hovers over GUI component representing
     * this format.
     */
    String getDescription();

    /**
     * A short one-word descriptor that will be used by the RARS
     * command line parser (and the RARS command line user) to specify
     * that this format is to be used.
     */
    String getCommandDescriptor();

    /**
     * Descriptive name for the format.
     *
     * @return Format name.
     */
    String toString();

    /**
     * Write memory contents according to the
     * specification for this format.
     *
     * @param file         File in which to store memory contents.
     * @param firstAddress first (lowest) memory address to dump.  In bytes but
     *                     must be on word boundary.
     * @param lastAddress  last (highest) memory address to dump.  In bytes but
     *                     must be on word boundary.  Will dump the word that starts at this address.
     * @throws AddressErrorException if firstAddress is invalid or not on a word boundary.
     * @throws IOException           if error occurs during file output.
     */
    void dumpMemoryRange(File file, int firstAddress, int lastAddress, Memory memory)
            throws AddressErrorException, IOException;

}