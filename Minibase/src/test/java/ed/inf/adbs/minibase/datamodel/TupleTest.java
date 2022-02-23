package ed.inf.adbs.minibase.datamodel;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TupleTest {
    @Test
    public void parseTest() {
        try {
            Catalog.INSTANCE.loadCatalog("data/evaluation/db");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Tuple tuple = new Tuple("1, 9, 'adbs'", "R");
        assertEquals(tuple.getItems().toString(), "[1, 9, 'adbs']");
    }

    @Test
    public void rebuildStrTest() {
        try {
            Catalog.INSTANCE.loadCatalog("data/evaluation/db");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Tuple tuple = new Tuple("1, 9, 'adbs'", "R");
        assertEquals(tuple.toString(), tuple.getRawStr());
    }
}
