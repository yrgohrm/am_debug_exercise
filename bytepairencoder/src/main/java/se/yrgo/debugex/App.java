package se.yrgo.debugex;

import java.io.*;
import java.nio.file.*;

public class App {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: prog <filename>");
            System.exit(-1);
        }

        String filename = args[0];

        try {
            if (filename.toLowerCase().endsWith(".bpe")) {
                BytePairEncoding.decodeFile(Path.of(filename));
            }
            else {
                BytePairEncoding.encodeFile(Path.of(filename));
            }
        }
        catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }
}
