package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.Utils;
import ed.inf.adbs.minibase.base.RelationalAtom;
import ed.inf.adbs.minibase.datamodel.Item;
import ed.inf.adbs.minibase.datamodel.ItemInteger;
import ed.inf.adbs.minibase.datamodel.Pair;
import ed.inf.adbs.minibase.datamodel.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AvgOperator extends AggOperator<Pair> {

    public AvgOperator(Operator child, RelationalAtom body, RelationalAtom head) throws Exception {
        super(child, body, head);
    }

    @Override
    protected Pair getInitValue(Item item) {
        return new Pair((Integer) item.getValue(), 1);
    }

    @Override
    protected Pair getAppendValue(Pair pair, Item item) {
        return new Pair((Integer) item.getValue() + pair.a, 1 + pair.b);
    }

    @Override
    protected Integer calcValueToInteger(Pair pair) {
        double avg = (double) pair.a / (double) pair.b;
        return (int) Math.round(avg);
    }
}
