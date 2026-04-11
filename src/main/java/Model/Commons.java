package Model;

public class Commons {
    public static int getBit(int n, int k) {
        return (n >> k) & 1;
    }

    public static int setBit(int n, int k, boolean on) {
        return on ? n | (1 << k) : (n & ~(1 << k));
    }

    public static boolean isBetween(int a, int low, int high) {
        return a >= low && a <= high;
    }
}
