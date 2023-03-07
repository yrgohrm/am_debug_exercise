package se.yrgo.debugex;

/**
 * A class representing a byte pair replacement.
 * 
 */
public class Lookup {
    private byte replacement;
    private BytePair pair;

    /**
     * Create a lookup given a replacement character and the 
     * byte pair it replaces.
     * 
     * @param replacement the replacement character
     * @param pair byte pair replaced by replacement character
     */
    public Lookup(byte replacement, BytePair pair) {
        this.replacement = replacement;
        this.pair = pair;
    }

    public byte getReplacement() {
        return replacement;
    }

    public BytePair getPair() {
        return pair;
    }
}