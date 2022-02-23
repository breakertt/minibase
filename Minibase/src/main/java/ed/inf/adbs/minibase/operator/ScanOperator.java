package ed.inf.adbs.minibase.operator;


import ed.inf.adbs.minibase.datamodel.Catalog;

import java.io.*;
import java.util.ArrayList;

public class ScanOperator extends Operator {
    private FileInputStream tableFileInputStream;
    private BufferedReader tableBufReader;

    public ScanOperator(String tableName) {
        super(new ArrayList<>()); // no child
        try {
            String tablePath = Catalog.INSTANCE.queryTablePath(tableName);
            if (tablePath == null) {
                throw new Exception("invalid table path");
            }
            tableFileInputStream = new FileInputStream(tablePath);
            tableBufReader = new BufferedReader(new InputStreamReader(tableFileInputStream));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getNextTuple() {
        String line = null;
        try {
            line = tableBufReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return line;
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
