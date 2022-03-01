package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.Utils;
import ed.inf.adbs.minibase.base.AggVariable;
import ed.inf.adbs.minibase.base.RelationalAtom;
import ed.inf.adbs.minibase.base.Term;
import ed.inf.adbs.minibase.datamodel.Item;
import ed.inf.adbs.minibase.datamodel.Tuple;

import java.util.List;

public abstract class AggOperator extends Operator {

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

    @Override
    public Tuple getNextTuple() {
        if (!isLoaded) {
            List<Tuple> childTupleList = child.dump();
            tupleList = groupBy(childTupleList);
            isLoaded = true;
        }
        if (isLoaded) {
            if (tupleList.size() == tupleListPointer) {
                return null;
            } else {
                Tuple tuple = tupleList.get(tupleListPointer);
                tupleListPointer++;
                return tuple;
            }
        }
        return null;
    }

    abstract protected List<Tuple> groupBy(List<Tuple> childTupleList);

    protected String genGroupKey(Tuple tuple) {
        List<Item> items = Utils.cloneList(tuple.getItems());
        items.remove(aggTermPos);
        return Utils.join(items, ",");
    }

    @Override
    public void reset() {
        tupleListPointer = 0;
    }
}
