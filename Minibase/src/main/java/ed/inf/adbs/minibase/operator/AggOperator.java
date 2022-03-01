package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.Utils;
import ed.inf.adbs.minibase.base.AggVariable;
import ed.inf.adbs.minibase.base.RelationalAtom;
import ed.inf.adbs.minibase.base.Term;
import ed.inf.adbs.minibase.datamodel.Item;
import ed.inf.adbs.minibase.datamodel.ItemInteger;
import ed.inf.adbs.minibase.datamodel.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class AggOperator<T> extends Operator {

    Operator child;

    int aggTermPos;
    Integer[] reorderArray;

    boolean isLoaded;
    List<Tuple> tupleList;
    int tupleListPointer;


    public AggOperator(Operator child, RelationalAtom body, RelationalAtom head) throws Exception {
        this.child = child;
        isLoaded = false;
        tupleListPointer = 0;
        analysisAgg(body, head);
    }

    private void analysisAgg(RelationalAtom body, RelationalAtom head) throws Exception {
        reorderArray = Utils.genAtomPosMap(body, head);
        List<Term> headTerms = head.getTerms();
        int i;
        for (i = 0; i < headTerms.size(); i++) {
            if (headTerms.get(i) instanceof AggVariable) {
                aggTermPos = i;
                return;
            }
        }
        if (i == headTerms.size()) throw new Exception("no aggregate function found");
    }

    private List<Tuple> groupBy(List<Tuple> childTupleList) {
        HashMap<String, T> valueMap = new HashMap<>();
        HashMap<String, Tuple> tupleMap = new HashMap<>();
        for (Tuple childTuple: childTupleList) {
            Tuple headTuple = new Tuple(childTuple, reorderArray);
            String key = getGroupKey(headTuple);
            updateValueMap(valueMap, key, headTuple.getItems().get(aggTermPos));
            updateTupleMap(tupleMap, key, headTuple);
        }
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

    private String getGroupKey(Tuple tuple) {
        List<Item> items = Utils.cloneList(tuple.getItems());
        items.remove(aggTermPos);
        return Utils.join(items, ",");
    }

    private void updateTupleMap(HashMap<String, Tuple> tupleMap, String key, Tuple childTuple) {
        if (!tupleMap.containsKey(key)) {
            tupleMap.put(key, childTuple);
        }
    }

    private void updateValueMap(HashMap<String, T> valueMap, String key, Item item) {
        if (!valueMap.containsKey(key)) {
            valueMap.put(key, getInitValue(item));
        } else {
            T t = valueMap.get(key);
            valueMap.put(key, getAppendValue(t, item));
        }
    }

    abstract protected T getInitValue(Item item);

    abstract protected T getAppendValue(T t, Item item);

    abstract protected Integer calcValueToInteger(T t);

    @Override
    public Tuple getNextTuple() {
        if (!isLoaded) {
            List<Tuple> childTupleList = child.dump();
            tupleList = groupBy(childTupleList);
            isLoaded = true;
        }
        if (tupleList.size() == tupleListPointer) {
            return null;
        } else {
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
