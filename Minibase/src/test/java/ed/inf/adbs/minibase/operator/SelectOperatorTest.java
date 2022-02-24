package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.base.ComparisonAtom;
import ed.inf.adbs.minibase.base.Query;
import ed.inf.adbs.minibase.base.RelationalAtom;
import ed.inf.adbs.minibase.datamodel.Catalog;
import ed.inf.adbs.minibase.parser.QueryParser;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNull;

public class SelectOperatorTest {

    @Test
    public void selectOpTestQuery2() throws IOException {
        try {
            Catalog.INSTANCE.loadCatalog("data/evaluation/db");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Query query = QueryParser.parse(Paths.get("data/evaluation/input/query2.txt"));
        ArrayList<ComparisonAtom> comparisonAtoms = new ArrayList<>();
        comparisonAtoms.add((ComparisonAtom) query.getBody().get(1));
        SelectOperator selectOp = new SelectOperator("R", (RelationalAtom) query.getBody().get(0), comparisonAtoms);
        selectOp.dump(System.out);
    }

    @Test
    public void selectOpTestQuery10() throws IOException {
        try {
            Catalog.INSTANCE.loadCatalog("data/evaluation/db");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Query query = QueryParser.parse(Paths.get("data/evaluation/input/query10.txt"));
        ArrayList<ComparisonAtom> comparisonAtoms = new ArrayList<>();
        SelectOperator selectOp = new SelectOperator("R", (RelationalAtom) query.getBody().get(0), comparisonAtoms);
        selectOp.dump(System.out);
    }

    @Test
    public void selectOpTestQuery11() throws IOException {
        try {
            Catalog.INSTANCE.loadCatalog("data/evaluation/db");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Query query = QueryParser.parse(Paths.get("data/evaluation/input/query11.txt"));
        ArrayList<ComparisonAtom> comparisonAtoms = new ArrayList<>();
        comparisonAtoms.add((ComparisonAtom) query.getBody().get(1));
        SelectOperator selectOp = new SelectOperator("R", (RelationalAtom) query.getBody().get(0), comparisonAtoms);
        selectOp.dump(System.out);
    }
}
