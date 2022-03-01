package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.base.RelationalAtom;
import ed.inf.adbs.minibase.datamodel.Item;
import ed.inf.adbs.minibase.datamodel.ItemInteger;
import ed.inf.adbs.minibase.datamodel.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SumOperator extends AggOperator {

    public SumOperator(Operator child, RelationalAtom body, RelationalAtom head) throws Exception {
        super(child, body, head);
    }

    protected List<Tuple> groupBy(List<Tuple> childTupleList) {
        HashMap<String, Integer> valueMap = new HashMap<>();
        HashMap<String, Tuple> tupleMap = new HashMap<>();
        for (Tuple childTuple: childTupleList) {
            Tuple headTuple = new Tuple(childTuple, reorderArray);
            String key = genGroupKey(headTuple);
            updateValue(valueMap, key, headTuple.getItems().get(aggTermPos));
            updateTuple(tupleMap, key, headTuple);
        }
        List<Tuple> tupleList = new ArrayList<>();
        for (HashMap.Entry<String, Integer> entry : valueMap.entrySet()) {
            Tuple oldTuple = tupleMap.get(entry.getKey());
            ArrayList<Item> tupleItems = new ArrayList<>(oldTuple.getItems());
            tupleItems.remove(aggTermPos);
            tupleItems.add(aggTermPos, new ItemInteger(entry.getValue()));
            Tuple tuple = new Tuple(oldTuple.getTableName(), tupleItems);
            tupleList.add(tuple);
        }
        return tupleList;
    }

    private void updateTuple(HashMap<String, Tuple> tupleMap, String key, Tuple childTuple) {
        if (!tupleMap.containsKey(key)) {
            tupleMap.put(key, childTuple);
        }
    }

    private void updateValue(HashMap<String, Integer> valueMap, String key, Item item) {
        if (valueMap.containsKey(key)) {
            Integer integer = valueMap.get(key);
            valueMap.put(key, integer + (Integer) item.getValue());
        } else {
            valueMap.put(key, (Integer) item.getValue());
        }
    }
}
