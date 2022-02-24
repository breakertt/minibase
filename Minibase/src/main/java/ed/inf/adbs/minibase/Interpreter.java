package ed.inf.adbs.minibase;

import ed.inf.adbs.minibase.base.Query;
import ed.inf.adbs.minibase.datamodel.Catalog;
import ed.inf.adbs.minibase.operator.Operator;
import ed.inf.adbs.minibase.operator.ScanOperator;
import ed.inf.adbs.minibase.parser.QueryParser;

import java.io.PrintStream;

public class Interpreter {

    Operator root;
    Query query;

    public Interpreter(String databaseDir, String inputFile) throws Exception {
        Catalog.INSTANCE.loadCatalog(databaseDir);
        query = QueryParser.parse(inputFile);
        root = planQuery(query);
    }

    private Operator planQuery(Query query) {
        return new ScanOperator(query.getHead().getName()); // dummy
    }

    public void dump(PrintStream ps) {
        root.dump(ps);
    }
}
