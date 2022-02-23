package ed.inf.adbs.minibase.datamodel;

import java.util.HashMap;

// Singleton
public enum Catalog {
    INSTANCE;

    private final HashMap<String, Table> tableMetadata = new HashMap<>();

    public boolean addTable(String name, String path, String schema) {
        Table newTable = new Table(name, path, schema);
        if (tableMetadata.containsKey(name)) {
            Table oldTable = tableMetadata.get(name);
            if (!oldTable.getName().equals(name)) return false;
            if (!oldTable.getPath().equals(path)) return false;
            if (!oldTable.getSchema().equals(schema)) return false;
        } else {
            tableMetadata.put(name, newTable);
        }
        return true;
    }

    public String queryTablePath(String name) {
        if (tableMetadata.containsKey(name)) {
            return tableMetadata.get(name).getPath();
        } else {
            return null;
        }
    }

    public String queryTableSchema(String name) {
        if (tableMetadata.containsKey(name)) {
            return tableMetadata.get(name).getSchema();
        } else {
            return null;
        }
    }

    private class Table {
        private final String name;
        private final String path;
        private final String schema;

        Table(String name, String path, String schema) {
            this.name = name;
            this.path = path;
            this.schema = schema;
        }

        public String getName() {
            return name;
        }

        public String getPath() {
            return path;
        }

        public String getSchema() {
            return schema;
        }
    }
}