package ed.inf.adbs.minibase.datamodel;

/**
 * An Item object which stores integer value
 */
public class ItemInteger extends Item {
    public final Integer i;

    ItemInteger(String s) {
        this.i = Integer.valueOf(s);
    }

    ItemInteger(int i) {
        this.i = i;
    }

    @SuppressWarnings("unchecked")
    public <T extends Comparable<T>> T getValue() {
        return (T) i;
    }

    @Override
    public String toString() {
        return i.toString();
    }
}
