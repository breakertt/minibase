package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.datamodel.Catalog;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ScanOperatorTest {

    @Test
    public void scanOpTest() {
        Catalog.INSTANCE.addTable(
                "R",
                "data/evaluation/db/files/R.csv",
                "R int int string"
        );
        ScanOperator scanOperator = new ScanOperator("R");
        System.out.println(scanOperator.getNextTuple());
        System.out.println();
        scanOperator.dump(System.out);
        assertNull(scanOperator.getNextTuple());
        assertNull(scanOperator.getNextTuple());
        System.out.println();
        scanOperator.reset();
        scanOperator.dump(System.out);
    }
}
