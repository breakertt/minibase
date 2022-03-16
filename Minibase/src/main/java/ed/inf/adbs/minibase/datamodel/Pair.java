package ed.inf.adbs.minibase.datamodel;

/**
 * A Pair class which stores two int values
 */
public class Pair {
    public int a, b;

    public Pair(int a, int b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public String toString() {
        return "Pair{" + a + ", " + b + "}";
    }
}
