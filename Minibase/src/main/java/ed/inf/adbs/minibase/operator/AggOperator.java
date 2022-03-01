package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.Utils;
import ed.inf.adbs.minibase.base.AggVariable;
import ed.inf.adbs.minibase.base.RelationalAtom;
import ed.inf.adbs.minibase.base.Term;
import ed.inf.adbs.minibase.datamodel.Item;
import ed.inf.adbs.minibase.datamodel.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * An abstract operator for aggregator operations
 * @param <T> the type of intermedia values while grouping
 */
public abstract class AggOperator<T> extends Operator {

    Operator child;

    int aggTermPos; // the position of aggregator in query head
    Integer[] reorderArray; // an array which records the pos of head atom variables in the body atom (maybe after join)

    boolean isLoaded; // whether the grouping has performed, lazy load
    List<Tuple> tupleList; // the output list of this operator
    int tupleListPointer; // which tuple to be returned in next getNextTuple() call

    /**
     * Constructor for aggregator abstract class
     * @param child child operator
     * @param body the atom body from child operator
     * @param head the atom to output
     * @throws Exception
     */
    public AggOperator(Operator child, RelationalAtom body, RelationalAtom head) throws Exception {
        this.child = child;
        isLoaded = false;
        tupleListPointer = 0;
        analysisAgg(body, head);
    }

    /**
     * Prepare the data structures for performing groping by
     * @param body the atom body from child operator
     * @param head the atom to output
     * @throws Exception
     */
    private void analysisAgg(RelationalAtom body, RelationalAtom head) throws Exception {
        // projection from body to head
        reorderArray = Utils.genAtomPosMap(body, head);
        // check the agg term as the last term of head
        List<Term> headTerms = head.getTerms();
        if (headTerms.get(headTerms.size() - 1) instanceof AggVariable) {
            aggTermPos = headTerms.size() - 1;
        } else {
            throw new Exception("no aggregate function found");
        }
    }

    /**
     * Calculate group by and aggregation functions
     * @param childTupleList tuples from child operators
     * @return tuples after applying group by and agg functions
     */
    private List<Tuple> groupBy(List<Tuple> childTupleList) {
        // grouping by the tuples from child operators
        HashMap<String, T> valueMap = new HashMap<>(); // the hashmap for storing the aggregator values
        HashMap<String, Tuple> tupleMap = new HashMap<>(); // the hashmap for storing the tuple examples
        for (Tuple childTuple: childTupleList) {
            Tuple headTuple = new Tuple(childTuple, reorderArray); // delete non-aggregated and non-groupby variables
            String key = getGroupKey(headTuple); // the group
            updateValueMap(valueMap, key, headTuple.getItems().get(aggTermPos)); // add value to the group
            updateTupleMap(tupleMap, key, headTuple); // store a tuple example for the group
        }
        // processing the vales stored for each group and produce tuples
        List<Tuple> tupleList = new ArrayList<>();
        for (HashMap.Entry<String, T> entry : valueMap.entrySet()) {
            Tuple oldTuple = tupleMap.get(entry.getKey());
            ArrayList<Item> tupleItems = new ArrayList<>(oldTuple.getItems());
            tupleItems.remove(aggTermPos);
            tupleItems.add(aggTermPos, Item.itemBuilder(calcValueToInteger(entry.getValue())));
            Tuple tuple = new Tuple(oldTuple.getTableName(), tupleItems);
            tupleList.add(tuple);
        }
        return tupleList;
    }

    /**
     * Generate the string of a tuple with grouping variables only
     * @param tuple tuple for generating the group key
     * @return a string as the indicator of group
     */
    private String getGroupKey(Tuple tuple) {
        List<Item> items = Utils.cloneList(tuple.getItems());
        items.remove(aggTermPos);
        return Utils.join(items, ",");
    }

    /**
     * Store a example tuple for each group key
     * @param tupleMap the hashmap for storing the tuple examples
     * @param key group key
     * @param childTuple example tuple to be stored
     */
    private void updateTupleMap(HashMap<String, Tuple> tupleMap, String key, Tuple childTuple) {
        if (!tupleMap.containsKey(key)) {
            tupleMap.put(key, childTuple);
        }
    }

    /**
     * Store all intermediate values for computing aggregator functions
     * @param valueMap the hashmap for storing the aggregator values
     * @param key group key
     * @param item the item containing value to be calculated with agg function
     */
    private void updateValueMap(HashMap<String, T> valueMap, String key, Item item) {
        if (!valueMap.containsKey(key)) {
            valueMap.put(key, getInitValue(item));
        } else {
            T t = valueMap.get(key);
            valueMap.put(key, getAppendValue(t, item));
        }
    }

    /**
     * The behavior when first item is recorded
     * @param item the item containing value to be calculated with agg function
     * @return an updated intermediate values in a specific data structure
     */
    abstract protected T getInitValue(Item item);

    /**
     * The behavior when more item is added into the intermediate values
     * @param item the item containing value to be calculated with agg function
     * @return an updated intermediate values in a specific data structure
     */
    abstract protected T getAppendValue(T t, Item item);

    /**
     * Transform intermediate values into one single integer
     * @param t intermediate values in a specific data structure
     * @return the integer value after agg function
     */
    abstract protected Integer calcValueToInteger(T t);

    @Override
    public Tuple getNextTuple() {
        if (!isLoaded) {
            List<Tuple> childTupleList = child.dump(); // requests all tuples that child can produces
            tupleList = groupBy(childTupleList); // calculate aggregations and store into tupleList
            isLoaded = true; // no reload from child in future
        }
        if (tupleList.size() == tupleListPointer) { // all tuples are returned
            return null;
        } else {
            // return next tuple, move the pointer
            Tuple tuple = tupleList.get(tupleListPointer);
            tupleListPointer++;
            return tuple;
        }
    }

    @Override
    public void reset() {
        tupleListPointer = 0;
    }
}
