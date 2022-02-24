package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.datamodel.Tuple;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public abstract class Operator {
    abstract public Tuple getNextTuple();
    abstract public void reset();

    public void dump(PrintStream ps) {
        for (Tuple tuple: dump()) {
            ps.println(tuple);
        }
    }

    public List<Tuple> dump() {
        ArrayList<Tuple> tuples = new ArrayList<>();
        Tuple tuple = null;
        while(true) {
            tuple = getNextTuple();
            if (tuple != null) {
                tuples.add(tuple);
            } else {
                break;
            }
        }
        return tuples;
    }
}
