package ed.inf.adbs.minibase.datamodel;

import ed.inf.adbs.minibase.base.Constant;
import ed.inf.adbs.minibase.base.IntegerConstant;
import ed.inf.adbs.minibase.base.StringConstant;

/**
 * An abstract class which can be further extended to concrete item with different data types
 */
public abstract class Item {

    /**
     * Build an item with value in string format and the type of the item
     * @param content value in string format
     * @param type the type of the item
     * @return an item object
     */
    public static Item itemBuilder(String content, String type) {
        if (type.equals("int")) {
            return new ItemInteger(content);
        } else if (type.equals("string")) {
            return new ItemString(content);
        } else {
            return null;
        }
    }

    /**
     * Build an ItemInteger object with a integer value
     * @param integer the integer value of the item
     * @return an ItemInteger object
     */
    public static Item itemBuilder(Integer integer) {
        return new ItemInteger(integer);
    }

    /**
     * Build an ItemInteger from a constant term from query atom
     * @param constant a constant term from query atom
     * @return an Item object which extracts value from constant term
     */
    public static Item itemBuilder(Constant constant) {
        if (constant instanceof IntegerConstant) {
            return new ItemInteger(((IntegerConstant) constant).getValue());
        } else if (constant instanceof StringConstant) {
            return new ItemString(((StringConstant) constant).getValue());
        } else {
            return null;
        }
    }

    /**
     * Return a Comparable type value for item comparisons
     * @return a Comparable value
     */
    public abstract Comparable getValue();
}