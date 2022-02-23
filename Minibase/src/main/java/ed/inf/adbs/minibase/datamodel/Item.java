package ed.inf.adbs.minibase.datamodel;

class Item {
    public static Item itemBuilder(String content, String type) {
        if (type.equals("int")) {
            return new ItemInt(content);
        } else if (type.equals("string")) {
            return new ItemString(content);
        } else {
            return null;
        }
    }
}

class ItemInt extends Item {
    public final int i;

    ItemInt(String s) {
        this.i = Integer.parseInt(s);
    }

    ItemInt(int i) {
        this.i = i;
    }

    @Override
    public String toString() {
        return String.valueOf(i);
    }
}

class ItemString extends Item {
    public final String s;

    ItemString(String s) {
        this.s = s;
    }

    @Override
    public String toString() {
        return s;
    }
}