package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.datamodel.Catalog;
import ed.inf.adbs.minibase.datamodel.Tuple;

import java.io.*;

/**
 * An Operator for reading tuples in a database table
 * This operator receives a table name, and read the actual file of table by looking up the Catalog singleton.
 */
public class ScanOperator extends Operator {

    private final String tableName;
    private FileInputStream tableFileInputStream;
    private BufferedReader tableBufReader;

    /**
     * Constructor for scan operator
     * retrieve the filename of given table name, open the table file
     * @param tableName table to scan
     */
    public ScanOperator(String tableName) {
        this.tableName = tableName;
        try {
            String tablePath = Catalog.INSTANCE.queryTablePath(tableName); // retrieve actual file path of table
            if (tablePath == null) {
                throw new Exception("Table not found in Catalog");
            }
            tableFileInputStream = new FileInputStream(tablePath);
            tableBufReader = new BufferedReader(new InputStreamReader(tableFileInputStream));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Tuple getNextTuple() {
        String tupleStr = null;
        try {
            tupleStr = tableBufReader.readLine(); // read one line as one tuple in the file
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (tupleStr != null) {
            return new Tuple(tupleStr, tableName); // parse the string of tuple to a Tuple class object
        } else {
            return null;
        }
    }

    @Override
    public void reset() {
        try {
            // reset the position of input stream, and initialize a new buffer reader
            tableFileInputStream.getChannel().position(0);
            tableBufReader = new BufferedReader(new InputStreamReader(tableFileInputStream));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "ScanOperator{" +
                "tableName='" + tableName + '\'' +
                '}';
    }
}
