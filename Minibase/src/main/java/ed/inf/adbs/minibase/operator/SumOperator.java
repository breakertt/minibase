package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.base.RelationalAtom;
import ed.inf.adbs.minibase.datamodel.Item;
import ed.inf.adbs.minibase.datamodel.ItemInteger;
import ed.inf.adbs.minibase.datamodel.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SumOperator extends AggOperator<Integer> {

    public SumOperator(Operator child, RelationalAtom body, RelationalAtom head) throws Exception {
        super(child, body, head);
    }

    @Override
    protected Integer getInitValue(Item item) {
        return (Integer) item.getValue();
    }

    @Override
    protected Integer getAppendValue(Integer integer, Item item) {
        return (Integer) item.getValue() + integer;
    }

    @Override
    protected Integer calcValueToInteger(Integer integer) {
        return integer;
    }
}
