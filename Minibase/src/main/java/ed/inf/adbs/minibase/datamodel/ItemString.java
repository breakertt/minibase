package ed.inf.adbs.minibase.datamodel;

public class ItemString extends Item {
    public final String s;

    public ItemString(String s) {
        if (s.charAt(0) == '\'' && s.charAt(s.length()-1) == '\'') {
            this.s = s.substring(1, s.length() - 1);
        } else {
            this.s = s;
        }
    }

    public String getValue() {
        return s;
    }

    @Override
    public String toString() {
        return "'" + s + "'";
    }
}