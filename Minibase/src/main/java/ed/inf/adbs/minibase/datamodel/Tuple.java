package ed.inf.adbs.minibase.datamodel;

import ed.inf.adbs.minibase.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Tuple {
    private final String tableName;
    private final ArrayList<Item> items;

    public Tuple(String tupleStr, String tableName) {
        this.tableName = tableName;
        this.items = parseTupleStr(tupleStr);
    }

    public Tuple(Tuple oldTuple, Integer[] reorderList) {
        this.tableName = oldTuple.tableName;
        this.items = reorderTupleItems(oldTuple.getItems(), reorderList);
    }

    private ArrayList<Item> parseTupleStr(String tupleStr) {
        String[] itemContentStrList = tupleStr.split(", ");
        String[] itemSchemaStrList = null;
        try {
            String tableSchema = Catalog.INSTANCE.queryTableSchema(tableName);
            if (tableSchema == null) {
                throw new Exception("Table not found in Catalog");
            }
            itemSchemaStrList = tableSchema.split(" ");
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

    private ArrayList<Item> reorderTupleItems(ArrayList<Item> oldItems, Integer[] reorderList) {
        ArrayList<Item> items = new ArrayList<>();
        for (Integer oldItemPos: reorderList) {
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
//        return rawStr;
        return Utils.join(items, ", ");
    }


}
