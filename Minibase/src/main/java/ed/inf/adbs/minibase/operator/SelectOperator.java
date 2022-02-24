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
    private HashMap<String, Integer> variablePosMap;

    public SelectOperator(String tableName, RelationalAtom rAtom, List<ComparisonAtom> cAtomList) {
        childScanOp = new ScanOperator(tableName); // one scan child
        this.cAtomList = cAtomList;
        this.rAtomBody = rAtom.getTerms();
        this.variablePosMap = new HashMap<String, Integer>();
    }

    private boolean explicitRulesCheck(Tuple tuple) {
        for (ComparisonAtom cAtom: cAtomList) {
            Comparable comparable1 = convertTermToComparable(cAtom.getTerm1(), tuple);
            Comparable comparable2 = convertTermToComparable(cAtom.getTerm2(), tuple);
            if (!CompareCheck(comparable1.compareTo(comparable2), cAtom.getOp())) return false;
        }
        return true;
    }

    private Comparable convertTermToComparable(Term term, Tuple tuple) {
        if (term instanceof Variable) {
            return tuple.getItems().get(getVariablePosInBody((Variable) term)).getValue();
        } else {
            return Item.itemBuilder((Constant) term).getValue();
        }
    }

    private int getVariablePosInBody(Variable variable) {
        String variableName = ((Variable) variable).getName();
        if (!variablePosMap.containsKey(variableName)) {
            int i;
            for (i = 0; i < rAtomBody.size(); i++) {
                if (rAtomBody.get(i).toString().equals(variableName)) {
                    variablePosMap.put(variableName, i);
                    return i;
                }
            }
            return -1;
        } else {
            return variablePosMap.get(variableName);
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
                Comparable comparable1 = item.getValue();
                Comparable comparable2 = tuple.getItems().get(i).getValue();
                if (comparable1.compareTo(comparable2) != 0) {
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
