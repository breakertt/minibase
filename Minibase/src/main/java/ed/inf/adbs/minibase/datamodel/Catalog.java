package ed.inf.adbs.minibase.datamodel;

import java.io.*;
import java.util.HashMap;
import java.util.Objects;

// A singleton object (implemented with enum) which stores metadata of tables in the database
public enum Catalog {
    INSTANCE;

    // a hashmap which stores the name of table as key, and the table class object as value
    private final HashMap<String, Table> catalog = new HashMap<>();

    /**
     * Load metadata of tables in the database
     * @param databaseDir the path of database
     * @throws Exception
     */
    public void loadCatalog(String databaseDir) throws Exception {
        File dbDirFile = new File(databaseDir);
        File schemaFile = new File(dbDirFile, "schema.txt");
        File tableSubDirFile = new File(dbDirFile, "files");
        // check whether schema file exists
        if (!schemaFile.exists() || !schemaFile.isFile()) {
            throw new Exception("schema not exist or not a file");
        }
        BufferedReader schemaBufReader = new BufferedReader(new FileReader(schemaFile));
        String tableSchema;
        // read schema file line by line, and load table metadata correspondingly
        while ((tableSchema = schemaBufReader.readLine()) != null) {
            loadTable(tableSchema, tableSubDirFile);
        }
    }

    /**
     * Load the metadata of one table
     * @param tableSchema the schema of this table from schema file
     * @param tableSubDirFile the directory of this table
     * @throws Exception
     */
    private void loadTable(String tableSchema, File tableSubDirFile) throws Exception {
        String[] tableSchemaList = tableSchema.split(" ");
        File tableFile = new File(tableSubDirFile, tableSchemaList[0] + ".csv");
        // check whether table file exists
        if (!tableFile.exists() || !tableFile.isFile()) {
            throw new Exception("table not exist or not a file");
        }
        // check duplicate tables in the schema file
        if (!addTable(tableSchemaList[0], tableFile.getPath(), tableSchema)) {
            throw new Exception("duplicate table name with different schema");
        }
    }

    /**
     * Add the table into catalog hashmap
     * @param name name of table
     * @param path path of table
     * @param schema scheme of table
     * @return whether a table is added into catalog successfully
     */
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

    /**
     * Get the path of one table on its name
     * @param name the table name
     * @return the path of the table
     */
    public String queryTablePath(String name) {
        if (catalog.containsKey(name)) {
            return catalog.get(name).getPath();
        } else {
            return null;
        }
    }

    /**
     * Get the schema of one table on its name
     * @param name the table name
     * @return the schema of the table
     */
    public String queryTableSchema(String name) {
        if (catalog.containsKey(name)) {
            return catalog.get(name).getSchema();
        } else {
            return null;
        }
    }

    /**
     * Get the schema of one table on its name, but in a string list format
     * @param name the table name
     * @return the schema of the table, in a string list format
     */
    public String[] queryTableSchemaStrList(String name) {
        if (catalog.containsKey(name)) {
            return catalog.get(name).getSchemaStrList();
        } else {
            return null;
        }
    }

    /**
     * A inner static class which stores metadata of one table.
     */
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