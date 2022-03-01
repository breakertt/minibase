package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.base.RelationalAtom;
import ed.inf.adbs.minibase.datamodel.Item;
import ed.inf.adbs.minibase.datamodel.Pair;

/**
 * An Operator for calculating average on one variable while grouping other variables
 */
public class AvgOperator extends AggOperator<Pair> {

    public AvgOperator(Operator child, RelationalAtom body, RelationalAtom head) throws Exception {
        super(child, body, head);
    }

    /**
     * The behavior when first item is recorded
     * @param item the item containing value to be calculated with agg function
     * @return a Pair with the first element as the sum of all values and the second as the count
     */
    @Override
    protected Pair getInitValue(Item item) {
        return new Pair((Integer) item.getValue(), 1);
    }

    /**
     * The behavior when more item is added into the intermediate values
     * @param item the item containing value to be calculated with agg function
     * @return a Pair with the first element as the sum of all values and the second as the count
     */
    @Override
    protected Pair getAppendValue(Pair pair, Item item) {
        return new Pair((Integer) item.getValue() + pair.a, 1 + pair.b);
    }

    /**
     * Divide the sum on the count for an average value
     * @param pair a Pair with the first element as the sum of all values and the second as the count
     * @return the rounded average value
     */
    @Override
    protected Integer calcValueToInteger(Pair pair) {
        double avg = (double) pair.a / (double) pair.b;
        return (int) Math.round(avg);
    }
}
