package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.base.*;
import ed.inf.adbs.minibase.datamodel.Item;
import ed.inf.adbs.minibase.datamodel.Tuple;

import java.util.HashMap;
import java.util.List;

public class SelectOperator extends Operator {

    private final ScanOperator childScanOp;
    private final List<ComparisonAtom> cAtomList;
    private final List<Term> rAtomBody;
    private HashMap<String, Integer> termnamePosMap;

    public SelectOperator(String tableName, RelationalAtom rAtom, List<ComparisonAtom> cAtomList) {
        childScanOp = new ScanOperator(tableName); // one scan child
        this.cAtomList = cAtomList;
        this.rAtomBody = rAtom.getTerms();
        this.termnamePosMap = new HashMap<String, Integer>();
    }

    private boolean explicitRulesCheck(Tuple tuple) {
        for (ComparisonAtom cAtom: cAtomList) {
            Item item1 = convertTermToItem(cAtom.getTerm1(), tuple);
            Item item2 = convertTermToItem(cAtom.getTerm2(), tuple);
            if (!CompareCheck(Item.compareBetween(item1, item2), cAtom.getOp())) return false;
        }
        return true;
    }

    private Item convertTermToItem(Term term, Tuple tuple) {
        if (term instanceof Variable) {
            String termName = ((Variable) term).getName();
            if (!termnamePosMap.containsKey(termName)) {
                int i;
                for (i = 0; i < rAtomBody.size(); i++) {
                    if (rAtomBody.get(i).toString().equals(termName)) break;
                }
                termnamePosMap.put(termName, i);
                return tuple.getItems().get(i);
            }
            return tuple.getItems().get(termnamePosMap.get(termName));
        } else {
            return Item.itemBuilder((Constant) term);
        }
    }

    private boolean CompareCheck(int comparedToRes, ComparisonOperator op) {
        switch (op) {
            case EQ:
                return comparedToRes == 0;
            case NEQ:
                return comparedToRes != 0;
            case GT:
                return comparedToRes > 0;
            case LT:
                return comparedToRes < 0;
            case GEQ:
                return comparedToRes >= 0;
            case LEQ:
                return comparedToRes <= 0;
            default:
                return false;
        }
    }

    private boolean implicitRulesCheck(Tuple tuple) {
        for (int i = 0; i < rAtomBody.size(); i++) {
            Term term = rAtomBody.get(i);
            if (term instanceof Constant) {
                Item item = Item.itemBuilder((Constant) term);
                if (Item.compareBetween(item, tuple.getItems().get(i)) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public Tuple getNextTuple() {
        Tuple tuple = null;
        while ((tuple = childScanOp.getNextTuple()) != null) {
            if (explicitRulesCheck(tuple) && implicitRulesCheck(tuple)) return tuple;
        }
        return null;
    }

    @Override
    public void reset() {
        childScanOp.reset();
    }

}
