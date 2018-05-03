package org.cloudbus.cloudsim;

import java.util.Comparator;

import org.cloudbus.cloudsim.core.SimEvent;

public interface CQComparator<T extends SimEvent> extends Comparator<T> {
    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Given an entry, return a virtual bin number for the entry. The virtual
     * bin number is a quantized version of whatever key is being used to
     * compare entries. The calculation performed should be something like:
     * <p>
     * <i>(entry - zeroReference) / binWidth</i>,
     * </p>
     * with the result cast to long.
     * <p>
     * Because of the way this is used by CalendarQueue, it is OK to return the
     * low order 64 bits of the result if the result does not fit in 64 bits.
     * The result will be masked anyway to get fewer low order bits that
     * represent the bin number. As a net result, time stamps that differ by
     * exactly 2^64 times the time resolution will appear in the event queue to
     * be occurring at the same time.
     * <p>
     * Classes that implement this interface will in general need to perform a
     * downcast on the arguments (of type Object) to the appropriate user
     * defined classes. If the arguments are not of appropriate type, the
     * implementation should throw a ClassCastException.
     * 
     * @param entry
     *        An object that can be inserted in a calendar queue.
     * @return The index of the bin.
     */
    public long getVirtualBinNumber(T entry);

    /**
     * Given an array of entries, set an appropriate bin width for a calendar
     * queue to hold these entries. This method assumes that the entries
     * provided are all different, and are in increasing order. Ideally, the bin
     * width is chosen so that the average number of entries in non-empty bins
     * is equal to one. If the argument is null set the default bin width.
     * 
     * @param sampleCopy
     * @param e
     * @param d
     */
    public void setBinWidth(SimEvent[] sampleCopy, double inq, double deq);

    /**
     * Set the zero reference, to be used in calculating the virtual bin number.
     * 
     * @param zeroReference
     *        The starting point for bins.
     */
    public void setZeroReference(T zeroReference);
}