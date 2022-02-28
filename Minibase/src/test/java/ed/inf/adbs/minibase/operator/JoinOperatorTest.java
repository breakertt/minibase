package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.base.Query;
import ed.inf.adbs.minibase.base.RelationalAtom;
import ed.inf.adbs.minibase.datamodel.Catalog;
import ed.inf.adbs.minibase.parser.QueryParser;
import org.junit.Test;

import static org.junit.Assert.assertNull;

public class JoinOperatorTest {

    @Test
    public void joinOpAnalysisTest() {
        try {
            Catalog.INSTANCE.loadCatalog("data/evaluation/db");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Query query = QueryParser.parse("Q() :- R(a, b, c, d, e), S(e, a, c, x)");
        RelationalAtom atom1 = (RelationalAtom) query.getBody().get(0);
        RelationalAtom atom2 = (RelationalAtom) query.getBody().get(1);
        JoinOperator joinOperator = new JoinOperator(null, null, atom1, atom2);
        System.out.println(joinOperator);
    }

    @Test
    public void joinOpSimpleNonNullDumpTest() {
        try {
            Catalog.INSTANCE.loadCatalog("data/evaluation/db");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Query query = QueryParser.parse("Q() :- R(a, b, c), S(a, d, e)");
        RelationalAtom atom1 = (RelationalAtom) query.getBody().get(0);
        RelationalAtom atom2 = (RelationalAtom) query.getBody().get(1);
        Operator child1 = new ScanOperator("R");
        Operator child2 = new ScanOperator("S");
        JoinOperator joinOperator = new JoinOperator(child1, child2, atom1, atom2);
        System.out.println(joinOperator);
        joinOperator.dump(System.out);
    }

    @Test
    public void joinOpSimpleNullDumpTest() {
        try {
            Catalog.INSTANCE.loadCatalog("data/evaluation/db");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Query query = QueryParser.parse("Q() :- R(a, b, c), S(d, e, f)");
        RelationalAtom atom1 = (RelationalAtom) query.getBody().get(0);
        RelationalAtom atom2 = (RelationalAtom) query.getBody().get(1);
        Operator child1 = new ScanOperator("R");
        Operator child2 = new ScanOperator("S");
        JoinOperator joinOperator = new JoinOperator(child1, child2, atom1, atom2);
        System.out.println(joinOperator);
        joinOperator.dump(System.out);
    }
}