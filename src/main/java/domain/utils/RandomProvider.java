package domain.utils;

import java.util.Random;

public final class RandomProvider {
    private static Random random = new Random();
    /* starts truly random by default */
    private static Long seed = null;

    private RandomProvider() {}

    public static void setSeed(long newSeed) {
        seed = newSeed;
        random = new Random(newSeed);
    }

    public static boolean chance(double probability) {
        return random.nextDouble() < probability;
    }

    public static int nextInt(int bound) {
        return random.nextInt(bound);
    }

    public static double nextDouble() {
        return random.nextDouble();
    }

    public static Long getSeed() {
        return seed;
    }
}