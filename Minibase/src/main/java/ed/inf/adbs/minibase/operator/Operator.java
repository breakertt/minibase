package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.datamodel.Tuple;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * An abstract Operator for all operators to follow and inherit, with a standard Iterator model
 */
public abstract class Operator {
    abstract public Tuple getNextTuple();
    abstract public void reset();

    /**
     * Dump all tuples into a print stream
     * @param ps the print stream for tuples written to
     */
    public void dump(PrintStream ps) {
        Tuple tuple;
        while(true) {
            tuple = getNextTuple();
            if (tuple != null) {
                ps.println(tuple);
            } else {
                break;
            }
        }
    }

    /**
     * Dump all tuples can be produced in one operator
     * @return a list of tuples
     */
    public List<Tuple> dump() {
        ArrayList<Tuple> tuples = new ArrayList<>();
        Tuple tuple;
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
