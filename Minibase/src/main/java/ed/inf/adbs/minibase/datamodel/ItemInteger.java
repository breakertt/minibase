package ed.inf.adbs.minibase.datamodel;

public class ItemInteger extends Item {
    public final Integer i;

    public ItemInteger(String s) {
        this.i = Integer.valueOf(s);
    }

    public ItemInteger(int i) {
        this.i = i;
    }

    public Integer getValue() {
        return i;
    }

    @Override
    public String toString() {
        return i.toString();
    }
}
