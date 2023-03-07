package se.yrgo.debugex;

/**
 * Class representing a byte pair, encapsulated to make it useable in 
 * hash-based data structures.
 * 
 */
public class BytePair {
    private byte b1;
    private byte b2;

    public BytePair(byte b1, byte b2) {
        this.b1 = b1;
        this.b2 = b2;
    }

    public byte[] getPair() {
        return new byte[] {b1, b2};
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + b1;
        result = prime * result + b2;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BytePair other = (BytePair) obj;
        if (b1 != other.b1)
            return false;
        return b2 == other.b2;
    }

    @Override
    public String toString() {
        return "BytePair [" + b1 + ", " + b2 + "]";
    }
}
