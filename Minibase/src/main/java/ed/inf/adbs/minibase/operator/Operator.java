package ed.inf.adbs.minibase.operator;

import java.io.PrintStream;
import java.util.List;

public abstract class Operator {
    protected List<Operator> children = null;

    abstract public String getNextTuple();
    abstract public void reset();

    public Operator(List<Operator> children) {
        this.children = children;
    }

    public List<Operator> getChildren() {
        return children;
    }

    public void dump(PrintStream ps) {
        String tuple = null;
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
