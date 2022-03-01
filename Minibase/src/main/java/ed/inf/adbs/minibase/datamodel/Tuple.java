package ed.inf.adbs.minibase.datamodel;

import ed.inf.adbs.minibase.Utils;

import java.util.ArrayList;

public class Tuple {
    private final String tableName;
    private final ArrayList<Item> items;

    public Tuple(String tupleStr, String tableName) {
        this.tableName = tableName;
        this.items = parseTupleStr(tupleStr);
    }

    public Tuple(String tableName, ArrayList<Item> items) {
        this.tableName = tableName;
        this.items = items;
    }

    public Tuple(Tuple oldTuple, Integer[] reorderArray) {
        this.tableName = oldTuple.tableName;
        this.items = reorderTupleItems(oldTuple.getItems(), reorderArray);
    }

    // Combine two tuples and shrink
    public Tuple(Tuple tuple1, Tuple tuple2, String tableName, Integer[] reorderArray) {
        this.tableName = tableName;
        ArrayList<Item> items = new ArrayList<>();
        items.addAll(tuple1.getItems());
        items.addAll(tuple2.getItems());
        this.items = reorderTupleItems(items, reorderArray);
    }

    private ArrayList<Item> parseTupleStr(String tupleStr) {
        String[] itemContentStrList = tupleStr.split(", ");
        String[] itemSchemaStrList = null;
        try {
            itemSchemaStrList = Catalog.INSTANCE.queryTableSchemaStrList(tableName);
            if (itemSchemaStrList == null) {
                throw new Exception("Table not found in Catalog");
            }
            if (!itemSchemaStrList[0].equals(tableName)) {
                throw new Exception("Table schema and name mismatch");
            }
            if (itemSchemaStrList.length != itemContentStrList.length + 1) {
                throw new Exception("Tuple length and Table schema length mismatch");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<Item> items = new ArrayList<>(itemContentStrList.length);
        for (int i = 0; i < itemContentStrList.length; i++) {
            items.add(Item.itemBuilder(itemContentStrList[i], itemSchemaStrList[i+1]));
        }
        return items;
    }

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
