package ed.inf.adbs.minibase.operator;


import ed.inf.adbs.minibase.datamodel.Catalog;
import ed.inf.adbs.minibase.datamodel.Tuple;

import java.io.*;

public class ScanOperator extends Operator {
    private final String tableName;
    private FileInputStream tableFileInputStream;
    private BufferedReader tableBufReader;

    public ScanOperator(String tableName) {
        this.tableName = tableName;
        try {
            String tablePath = Catalog.INSTANCE.queryTablePath(tableName);
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
            tupleStr = tableBufReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (tupleStr != null) {
            return new Tuple(tupleStr, tableName);
        } else {
            return null;
        }
    }

    @Override
    public void reset() {
        try {
            tableFileInputStream.getChannel().position(0);
            tableBufReader = new BufferedReader(new InputStreamReader(tableFileInputStream));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
