package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.base.RelationalAtom;
import ed.inf.adbs.minibase.datamodel.Item;

/**
 * An Operator for calculating sum on one variable while grouping other variables
 */
public class SumOperator extends AggOperator<Integer> {

    public SumOperator(Operator child, RelationalAtom body, RelationalAtom head) throws Exception {
        super(child, body, head);
    }

    /**
     * The behavior when first item is recorded
     * @param item the item containing value to be calculated with agg function
     * @return the sum of all values seen
     */
    @Override
    protected Integer getInitValue(Item item) {
        return (Integer) item.getValue();
    }

    /**
     * The behavior when more item is added into the intermediate values
     * @param item the item containing value to be calculated with agg function
     * @return the sum of all values seen
     */
    @Override
    protected Integer getAppendValue(Integer integer, Item item) {
        return (Integer) item.getValue() + integer;
    }

    /**
     * Result of sum aggregation function
     * @param integer the sum of all values seen
     * @return the sum of all values seen
     */
    @Override
    protected Integer calcValueToInteger(Integer integer) {
        return integer;
    }
}
