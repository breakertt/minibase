package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.base.*;
import ed.inf.adbs.minibase.datamodel.Item;
import ed.inf.adbs.minibase.datamodel.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SelectOperator extends Operator {

    private final Operator childScanOp;
    private final List<ComparisonAtom> cAtomList;
    private final List<Term> rAtomBody;
    private HashMap<String, Integer> variablePosMap;
    private List<Integer> constantTermPosList;

    public SelectOperator(Operator child, RelationalAtom rAtom, List<ComparisonAtom> cAtomList) {
        childScanOp = child; // one scan child
        this.cAtomList = cAtomList;
        this.rAtomBody = rAtom.getTerms();
        this.variablePosMap = new HashMap<String, Integer>();
        this.constantTermPosList = new ArrayList<>();
        analysisAtomBody();
    }

    private void analysisAtomBody() {
        int i;
        for (i = 0; i < rAtomBody.size(); i++) {
            Term term = rAtomBody.get(i);
            if (term instanceof Variable) {
                variablePosMap.put(((Variable) term).getName(), i);
            } else {
                constantTermPosList.add(i);
            }
        }
    }

    private int getVariablePos(Variable variable) {
        String variableName = ((Variable) variable).getName();
        return variablePosMap.getOrDefault(variableName, -1);
    }

    private boolean explicitRulesCheck(Tuple tuple) {
        for (ComparisonAtom cAtom: cAtomList) {
            Comparable comparable1 = extractOrMatchValueForTerm(cAtom.getTerm1(), tuple);
            Comparable comparable2 = extractOrMatchValueForTerm(cAtom.getTerm2(), tuple);
            if (!compareCheck(comparable1.compareTo(comparable2), cAtom.getOp())) return false;
        }
        return true;
    }

    private Comparable extractOrMatchValueForTerm(Term term, Tuple tuple) {
        if (term instanceof Variable) {
            return tuple.getItems().get(getVariablePos((Variable) term)).getValue();
        } else {
            return Item.itemBuilder((Constant) term).getValue();
        }
    }

    private boolean implicitRulesCheck(Tuple tuple) {
        for (Integer i: constantTermPosList) {
            Comparable comparable1 = Item.itemBuilder((Constant) rAtomBody.get(i)).getValue();
            Comparable comparable2 = tuple.getItems().get(i).getValue();
            if (!compareCheck(comparable1.compareTo(comparable2), ComparisonOperator.EQ)) return false;
        }
        return true;
    }

    private boolean compareCheck(int comparedToRes, ComparisonOperator op) {
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
