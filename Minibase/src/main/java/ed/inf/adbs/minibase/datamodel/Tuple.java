package ed.inf.adbs.minibase.datamodel;

import ed.inf.adbs.minibase.Utils;

import java.util.ArrayList;

/**
 * Tuple class for representing one data record in a table
 */
public class Tuple {

    private final String tableName; // the table this tuple belongs to
    private final ArrayList<Item> items; // value items in this tuple

    /**
     * Constructor for tuple class
     * parse the tuple originally in string format, referring to the table scheme
     * @param tupleStr the string representation of this tuple
     * @param tableName the table this tuple belongs to
     */
    public Tuple(String tupleStr, String tableName) {
        this.tableName = tableName;
        this.items = parseTupleStr(tupleStr);
    }

    /**
     * Constructor for tuple class
     * generate a tuple with assigned value items and table name, no check
     * @param tableName the table this tuple belongs to
     * @param items value items directly assigned to the tuple
     */
    public Tuple(String tableName, ArrayList<Item> items) {
        this.tableName = tableName;
        this.items = items;
    }

    /**
     * Constructor for tuple class
     * generate a new tuple from a old tuple with a reorder mapping
     * @param oldTuple the old tuple to be referred to
     * @param reorderArray reorder mapping
     */
    public Tuple(Tuple oldTuple, Integer[] reorderArray) {
        this.tableName = oldTuple.tableName;
        this.items = reorderTupleItems(oldTuple.getItems(), reorderArray);
    }

    /**
     * Constructor for tuple class
     * Combine two old tuples to a new tuple with a reorder mapping
     * @param tuple1 the old tuple 1
     * @param tuple2 the old tuple 2
     * @param tableName  the table this tuple belongs to
     * @param reorderArray reorder mapping
     */
    public Tuple(Tuple tuple1, Tuple tuple2, String tableName, Integer[] reorderArray) {
        this.tableName = tableName;
        ArrayList<Item> items = new ArrayList<>();
        items.addAll(tuple1.getItems());
        items.addAll(tuple2.getItems());
        this.items = reorderTupleItems(items, reorderArray);
    }

    /**
     * Parse the tuple from a string
     * @param tupleStr tuple string
     * @return list of value items
     */
    private ArrayList<Item> parseTupleStr(String tupleStr) {
        // split the tuple string to values in string format
        String[] itemContentStrList = tupleStr.split(", ");
        String[] itemSchemaStrList = null;
        try {
            // get the schema of the table from Catalog
            itemSchemaStrList = Catalog.INSTANCE.queryTableSchemaStrList(tableName);
            if (itemSchemaStrList == null) {
                throw new Exception("Table not found in Catalog");
            }
            // check the length of schema and tuple values
            if (itemSchemaStrList.length != itemContentStrList.length + 1) {
                throw new Exception("Tuple length and Table schema length mismatch");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<Item> items = new ArrayList<>(itemContentStrList.length);
        for (int i = 0; i < itemContentStrList.length; i++) {
            // build item according to value and type
            items.add(Item.itemBuilder(itemContentStrList[i], itemSchemaStrList[i+1]));
        }
        return items;
    }

    /**
     * Reorder the sequence of items in a tuple
     * @param oldItems the item list in the old order
     * @param reorderArray for each tuple item, the positions in old item list
     * @return a new list of items after reordering
     */
    private ArrayList<Item> reorderTupleItems(ArrayList<Item> oldItems, Integer[] reorderArray) {
        ArrayList<Item> items = new ArrayList<>();
        for (Integer oldItemPos: reorderArray) {
            items.add(oldItems.get(oldItemPos));
        }
        return items;
    }

    public String getTableName() {
        return tableName;
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    @Override
    public java.lang.String toString() {
        return Utils.join(items, ", ");
    }


}
