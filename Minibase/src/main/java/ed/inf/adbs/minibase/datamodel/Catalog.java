package ed.inf.adbs.minibase.datamodel;

import java.io.*;
import java.util.HashMap;
import java.util.Objects;

// Singleton
public enum Catalog {
    INSTANCE;

    private final HashMap<String, Table> catalog = new HashMap<>();

    public void loadCatalog(String databaseDir) throws Exception {
        File dbDirFile = new File(databaseDir);
        File schemaFile = new File(dbDirFile, "schema.txt");
        File tableSubDirFile = new File(dbDirFile, "files");
        BufferedReader schemaBufReader;

        if (!schemaFile.exists() || !schemaFile.isFile()) {
            throw new Exception("schema not exist or not a file");
        }

        schemaBufReader = new BufferedReader(new FileReader(schemaFile));
        String tableSchema;
        while ((tableSchema = schemaBufReader.readLine()) != null) {
            loadTable(tableSchema, tableSubDirFile);
        }
    }

    private void loadTable(String tableSchema, File tableSubDirFile) throws Exception {
        String[] tableSchemaList = tableSchema.split(" ");
        File tableFile = new File(tableSubDirFile, tableSchemaList[0] + ".csv");

        if (!tableFile.exists() || !tableFile.isFile()) {
            throw new Exception("table not exist or not a file");
        }

        if (!addTable(tableSchemaList[0], tableFile.getPath(), tableSchema)) {
            throw new Exception("duplicate table name with different schema");
        }
    }

    private boolean addTable(String name, String path, String schema) {
        Table newTable = new Table(name, path, schema);
        if (catalog.containsKey(name)) {
            Table oldTable = catalog.get(name);
            return oldTable.equals(newTable);
        } else {
                catalog.put(name, newTable);
            return true;
        }
    }

    public String queryTablePath(String name) {
        if (catalog.containsKey(name)) {
            return catalog.get(name).getPath();
        } else {
            return null;
        }
    }

    public String queryTableSchema(String name) {
        if (catalog.containsKey(name)) {
            return catalog.get(name).getSchema();
        } else {
            return null;
        }
    }

    public String[] queryTableSchemaStrList(String name) {
        if (catalog.containsKey(name)) {
            return catalog.get(name).getSchemaStrList();
        } else {
            return null;
        }
    }

    private static class Table {
        private final String name;
        private final String path;
        private final String schema;
        private final String[] schemaStrList;

        Table(String name, String path, String schema) {
            this.name = name;
            this.path = path;
            this.schema = schema;
            this.schemaStrList = schema.split(" ");
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

        public String[] getSchemaStrList() {
            return schemaStrList;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Table table = (Table) o;
            return Objects.equals(name, table.name) && Objects.equals(path, table.path) && Objects.equals(schema, table.schema);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, path, schema);
        }
    }
}