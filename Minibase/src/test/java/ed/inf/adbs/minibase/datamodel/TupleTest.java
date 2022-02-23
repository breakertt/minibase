package ed.inf.adbs.minibase.datamodel;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TupleTest {
    @Test
    public void parseTest() {
        Catalog.INSTANCE.addTable(
                "R",
                "data/evaluation/db/files/R.csv",
                "R int int string"
        );
        Tuple tuple = new Tuple("1, 9, 'adbs'", "R");
        assertEquals(tuple.getItems().toString(), "[1, 9, 'adbs']");
    }

    @Test
    public void rebuildStrTest() {
        Catalog.INSTANCE.addTable(
                "R",
                "data/evaluation/db/files/R.csv",
                "R int int string"
        );
        Tuple tuple = new Tuple("1, 9, 'adbs'", "R");
        assertEquals(tuple.toString(), tuple.getRawStr());
    }
}
