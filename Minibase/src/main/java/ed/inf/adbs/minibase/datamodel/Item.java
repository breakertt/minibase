package ed.inf.adbs.minibase.datamodel;

import ed.inf.adbs.minibase.base.Constant;
import ed.inf.adbs.minibase.base.IntegerConstant;
import ed.inf.adbs.minibase.base.StringConstant;

public abstract class Item {
    public static Item itemBuilder(String content, String type) {
        if (type.equals("int")) {
            return new ItemInteger(content);
        } else if (type.equals("string")) {
            return new ItemString(content);
        } else {
            return null;
        }
    }

    public static Item itemBuilder(Constant constant) {
        if (constant instanceof IntegerConstant) {
            return new ItemInteger(((IntegerConstant) constant).getValue());
        } else if (constant instanceof StringConstant) {
            return new ItemString(((StringConstant) constant).getValue());
        } else {
            return null;
        }
    }

    public static int compareBetween(Item item1, Item item2) {
        if (item1 instanceof ItemInteger) {
            return ((ItemInteger) item1).getValue().compareTo(((ItemInteger) item2).getValue());
        } else {
            return ((ItemString) item1).getValue().compareTo(((ItemString) item2).getValue());
        }
    }
}