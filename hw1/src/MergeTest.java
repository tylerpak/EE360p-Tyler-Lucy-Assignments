import java.util.Arrays;
import java.util.Random;

public class MergeTest {

    public static void main(String[] args) {
        final int NUM_THREADS = 4; // Change this to however many threads you want to test with
        testRandom(NUM_THREADS);
        System.out.println("Congrats! Your implementation passes the randomly generated test cases!");
    }

    /**
     * Generates and tests PMerge 10 times with random array inputs.
     * @param numThreads number of threads to run PMerge with
     */
    public static void testRandom(int numThreads) {
        Random rand = new Random();
        for(int i = 0; i<10; ++i) {
            int aSize = rand.nextInt(200);
            int bSize = rand.nextInt(200);
            int[] a = new int[aSize], b = new int[bSize];
            for(int j = 0; j<a.length; ++j)
                a[j] = rand.nextInt();
            for(int j = 0; j<b.length; ++j)
                b[j] = rand.nextInt();
            int[] c = new  int[aSize + bSize];

            Arrays.sort(a);
            Arrays.sort(b);
            PMerge.parallelMerge(a, b, c, numThreads);
            verify(a, b, c);
        }
    }

    static void verify(int[] a, int[] b, int[] c) {
        int[] expected = new int[a.length + b.length];
        System.arraycopy(a, 0, expected, 0, a.length);
        System.arraycopy(b, 0, expected, a.length, b.length);
        Arrays.sort(expected);
        reverse(expected);
        if(!Arrays.equals(expected, c)) {
            throw new AssertionError(String.format("\nExpected: %s\nReceived: %s\n", Arrays.toString(expected), Arrays.toString(c)));
        }
    }

    static void reverse(int[] a) {
        for(int i = 0; i<a.length/2; ++i) {
            a[i] ^= a[a.length-1-i];
            a[a.length-1-i] ^= a[i];
            a[i] ^= a[a.length-1-i];
        }
    }

}
