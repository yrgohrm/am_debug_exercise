package se.yrgo.debugex;

import java.io.*;
import java.util.*;

/**
 * Keep track of the replacements done encoding byte pairs.
 * 
 */
public class LookupTable {
    private List<Lookup> table = new ArrayList<>();

    /**
     * Adds a new entry to the end of the lookup table.
     * 
     * @param replacement the replacement character
     * @param pair the pair of bytes replaced by replacement
     */
    public void add(byte replacement, BytePair pair) {
        table.add(new Lookup(replacement, pair));
    }

    /**
     * Removes and returns the last entry in the lookup table.
     *
     * @return empty if the table is empty otherwise the last entry
     */
    public Optional<Lookup> remove() {
        if (table.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(table.remove(table.size()-1));
    }

    /**
     * Returns the number of entries in the lookup table.
     * 
     * @return entry count
     */
    public int size() {
        return table.size();
    }

    /**
     * Writes the lookup table to an output stream.
     * 
     * The size of the data written (in bytes) will be: 1 + 3 * number of entries.
     * 
     * The format is: 
     *      <number of entries (unsigned byte)> 
     *      <replacement character 1 (byte)>
     *      <first byte of pair 1 (byte)>
     *      <second byte of pair 1 (byte)>
     *      ...
     *      <replacement character N (byte)>
     *      <first byte of pair N (byte)>
     *      <second byte of pair N (byte)>
     *      
     * @param os
     * @throws IOException
     */
    public void write(OutputStream os) throws IOException {
        os.write(table.size());
        
        for (Lookup lookup : table) {
            BytePair bp = lookup.getPair();
            os.write(lookup.getReplacement());
            os.write(bp.getPair());
        }
    }

    /**
     * Reads data written using {@link #write(OutputStream) write} into
     * a new lookup table.
     * 
     * @param is the input stream to read from
     * @return a new lookup table
     * @throws IOException
     */
    public static LookupTable read(InputStream is) throws IOException {
        final LookupTable lookupTable = new LookupTable();
        final byte[] data = new byte[3];
        final int size = is.read();

        for (int i = 0; i < size; i++) {
            if (is.read(data) != 3) {
                throw new IOException("malformed data, unable to read lookup table");
            }

            lookupTable.add(data[0], new BytePair(data[1], data[2]));
        }

        return lookupTable;
    }
}
