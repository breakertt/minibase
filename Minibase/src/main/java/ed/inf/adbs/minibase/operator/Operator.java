package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.datamodel.Tuple;

import java.io.PrintStream;

public abstract class Operator {
    abstract public Tuple getNextTuple();
    abstract public void reset();

    public void dump(PrintStream ps) {
        Tuple tuple = null;
        while(true) {
            tuple = getNextTuple();
            if (tuple != null) {
                ps.println(tuple);
            } else {
                break;
            }
        }
    }
}
