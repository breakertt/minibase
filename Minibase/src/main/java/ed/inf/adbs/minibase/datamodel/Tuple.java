package ed.inf.adbs.minibase.datamodel;

import ed.inf.adbs.minibase.Utils;

import java.util.ArrayList;

public class Tuple {
    private final String rawStr;
    private final String tableName;
    private final ArrayList<Item> items;

    public Tuple(String tupleStr, String tableName) {
        this.rawStr = tupleStr;
        this.tableName = tableName;
        this.items = parseTupleStr();
    }

    private ArrayList<Item> parseTupleStr() {
        String[] itemContentStrList = rawStr.split(", ");
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

    public String getRawStr() {
        return rawStr;
    }

    public String getTableName() {
        return tableName;
    }

    public ArrayList<Item> getItems() {
        return items;
    }


    @Override
    public java.lang.String toString() {
//        return str;
        return Utils.join(items, ", ");
    }


}
