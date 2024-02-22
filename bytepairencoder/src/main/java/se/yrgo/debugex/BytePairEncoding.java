package se.yrgo.debugex;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Class for encoding and decoding using byte pairs.
 * 
 * Byte pair encoding (BPE) is a simple form of data compression in which the most common pair of
 * contiguous bytes of data in a sequence are replaced with a byte that does not occur within the
 * sequence.
 * 
 * A lookup table of the replacements is required to rebuild the original data.
 * 
 * First described by Philip Gage, "A New Algorithm for Data Compression", in the C Users Journal.
 * 
 */
public final class BytePairEncoding {
    private BytePairEncoding() {
    }

    /**
     * Encode the given file using BPE writing a new file using the same filename, but adding the
     * ending .bpe.
     * 
     * The file will consist of first the lookup table followed by the data. The lookup table is
     * stored in the order the replacements were applied and decoding should thus occur from last to
     * first.
     * 
     * File format: <number of lookup table entries (1 byte)> <replacement 1 (1 byte)> <replacement
     * pair 1 (2 bytes)> ... <replacement N (1 byte)> <replacement pair N (2 bytes)> <data>
     * 
     * @param input the file to read and encode
     * @throws IOException
     */
    public static void encodeFile(Path input) throws IOException {
        final Path output = input.getFileSystem().getPath(input + ".bpe");

        final LookupTable lookups = new LookupTable();

        try (InputStream file = Files.newInputStream(input)) {
            byte[] data = file.readAllBytes();

            while (true) {
                BitSet symbolSet = findSymbols(data);
                var optBytePair = findMostCommonBytePair(data);
                var optUnusedByte = nextUnusedSymbol(symbolSet);

                // if we don't have any more byte pairs to encode
                // or no more symbol to use, we're done encoding
                if (optBytePair.isEmpty() || optUnusedByte.isEmpty()) {
                    break;
                }

                BytePair bytePair = optBytePair.get();
                byte unusedSymbol = optUnusedByte.get();

                lookups.add(unusedSymbol, bytePair);

                data = replacePair(data, bytePair, unusedSymbol);
            }

            writeEncodedFile(output, lookups, data);
        }
    }

    /**
     * Decode a byte pair encoded file and write it to a new file.
     * 
     * @param input the file to decode
     * @throws IOException
     */
    public static void decodeFile(Path input) throws IOException {
        final String filename = input.getFileName().toString();
        final String path = input.getParent().toString();
        final Path output = input.getFileSystem().getPath(path, "decoded-" + filename.substring(0, filename.length() - 4));

        try (InputStream file = Files.newInputStream(input)) {
            LookupTable lookupTable = LookupTable.read(file);

            byte[] data = file.readAllBytes();

            while (true) {
                // process one translation at a time until we have no more
                // start at the end (the last translation done)
                var optLookup = lookupTable.remove();
                if (optLookup.isEmpty()) {
                    break;
                }

                Lookup lookup = optLookup.get();
                data = replaceByte(data, lookup.getReplacement(), lookup.getPair());
            }

            writeDecodedFile(output, data);
        }
    }

    private static void writeDecodedFile(Path file, byte[] data) throws IOException {
        try (OutputStream os = Files.newOutputStream(file)) {
            os.write(data);
        }
    }

    private static byte[] replaceByte(byte[] data, byte replacement, BytePair pair)
            throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        for (byte b : data) {
            if (b == replacement) {
                bos.write(pair.getPair());
            }
            else {
                bos.write(b);
            }
        }

        return bos.toByteArray();
    }

    private static void writeEncodedFile(Path file, LookupTable lookups, byte[] data)
            throws IOException {
        try (OutputStream os = Files.newOutputStream(file)) {
            lookups.write(os);
            os.write(data);
        }
    }

    private static byte[] replacePair(byte[] data, BytePair pair, byte symbol) {
        final ByteArrayOutputStream bao = new ByteArrayOutputStream();
        final byte[] bytes = pair.getPair();

        // loop through all the data but the last character to enable
        // us to look ahead for matching byte pairs

        int i = 0;
        do {
            if (data[i] == bytes[0] && data[i + 1] == bytes[1]) {
                bao.write(symbol);
                i += 2;
            }
            else {
                bao.write(data[i]);
                i += 1;
            }
        } while (i < data.length - 1);

        return bao.toByteArray();
    }

    private static BitSet findSymbols(byte[] data) {
        BitSet symbolSet = new BitSet(256);
        for (byte b : data) {
            symbolSet.set(b & 0xff);
        }
        return symbolSet;
    }

    private static Optional<Byte> nextUnusedSymbol(BitSet symbolSet) {
        for (int i = 0; i < symbolSet.size(); ++i) {
            if (!symbolSet.get(i)) {
                symbolSet.set(i);
                return Optional.of((byte) i);
            }
        }

        return Optional.empty();
    }

    private static Optional<BytePair> findMostCommonBytePair(byte[] data) {
        int maxCount = 0;
        BytePair maxPair = null;

        final Map<BytePair, Integer> counter = new HashMap<>();
        for (int i = 0; i < data.length - 1; i++) {
            BytePair bp = new BytePair(data[i], data[i + 1]);
            int count = counter.merge(bp, 1, Integer::sum);
            if (count > maxCount) {
                maxCount = count;
                maxPair = bp;
            }
        }

        // we need at least three pairs for the encoding to be useful
        return maxCount > 2 ? Optional.of(maxPair) : Optional.empty();
    }
}
