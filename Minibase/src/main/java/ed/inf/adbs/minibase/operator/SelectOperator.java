package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.base.*;
import ed.inf.adbs.minibase.datamodel.Item;
import ed.inf.adbs.minibase.datamodel.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * An Operator for selecting the tuple under explicit conditions and implicit ones
 * For each tuple, the variables in comparison atoms will be transferred into actual values for comparison, meanwhile,
 * the constant terms in the relational atom itself will be compared to the item in the tuple on same position.
 */
public class SelectOperator extends Operator {

    private final Operator child;
    private final List<ComparisonAtom> cAtomList; // the list of comparison atoms
    private final List<Term> rAtomBody; // terms of the atom
    private HashMap<String, Integer> variablePosMap; // a map for variable name to pos in atom fast lookup
    private List<Integer> constantTermPosList; // the positions for constant terms for fast implicit equal check

    /**
     * Constructor for select operator
     * @param child child operator
     * @param rAtom the relational atom which will be selected on conditions
     * @param cAtomList list of comparison atoms as conditions for selection
     */
    public SelectOperator(Operator child, RelationalAtom rAtom, List<ComparisonAtom> cAtomList) {
        this.child = child; // one scan child
        this.cAtomList = cAtomList;
        this.rAtomBody = rAtom.getTerms();
        this.variablePosMap = new HashMap<>();
        this.constantTermPosList = new ArrayList<>();
        analysisSelect();
    }

    /**
     * Prepare the data structures for performing select in a fast way
     */
    private void analysisSelect() {
        for (int i = 0; i < rAtomBody.size(); i++) {
            Term term = rAtomBody.get(i);
            // store the name and position of variable terms into variablePosMap
            if (term instanceof Variable) {
                variablePosMap.put(((Variable) term).getName(), i);
            // store the position of constant terms into constantTermPosList
            } else {
                constantTermPosList.add(i);
            }
        }
    }

    /**
     * Check whether a tuple meets the explicit conditions in cAtomList
     * @param tuple the tuple to check
     * @return whether the tuple meets the explicit conditions
     */
    private boolean explicitRulesCheck(Tuple tuple) {
        for (ComparisonAtom cAtom: cAtomList) {
            Comparable comparable1 = extractOrMatchValueForTerm(cAtom.getTerm1(), tuple);
            Comparable comparable2 = extractOrMatchValueForTerm(cAtom.getTerm2(), tuple);
            if (!compareCheck(comparable1.compareTo(comparable2), cAtom.getOp())) return false;
        }
        return true;
    }

    /**
     * Transform a term in comparison to a Comparable obj with concrete value, similar to de-referencing
     * if the term in comparison atom is a variable, find where the variable is in the relational atom, the get the item
     * (with value) in the tuple according to the pos; else, get the constant value in term.
     * @param term the term to transform
     * @param tuple the tuple for variable term to get concrete value
     * @return a Comparable obj with concrete value
     */
    private Comparable extractOrMatchValueForTerm(Term term, Tuple tuple) {
        if (term instanceof Variable) {
            Integer variablePos = variablePosMap.getOrDefault(((Variable) term).getName(), -1);
            return tuple.getItems().get(variablePos).getValue();
        } else {
            return Item.itemBuilder((Constant) term).getValue();
        }
    }

    /**
     * Check whether a tuple meets the explicit conditions in the relational atom itself, i.e. the constant terms
     * @param tuple the tuple to check
     * @return whether the tuple meets the implicit conditions
     */
    private boolean implicitRulesCheck(Tuple tuple) {
        for (Integer i: constantTermPosList) {
            // get the constant value in the atom body
            Comparable comparable1 = Item.itemBuilder((Constant) rAtomBody.get(i)).getValue();
            // get the value in the tuple
            Comparable comparable2 = tuple.getItems().get(i).getValue();
            if (!compareCheck(comparable1.compareTo(comparable2), ComparisonOperator.EQ)) return false;
        }
        return true;
    }

    /**
     * A function which adopts between the result from compareTo method for Comparable objects to the ComparisonOperator
     * enums
     * @param comparedToRes the result from compareTo method between two Comparable objects
     * @param op the ComparisonOperator operator enums
     * @return whether the comparedTo results match the ComparisonOperator operator
     */
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
        Tuple tuple;
        while ((tuple = child.getNextTuple()) != null) {
            // get tuple from child operator until a tuple meets the condition, or no tuple from child op
            if (explicitRulesCheck(tuple) && implicitRulesCheck(tuple)) return tuple;
        }
        return null;
    }

    @Override
    public void reset() {
        child.reset();
    }

    @Override
    public String toString() {
        return "SelectOperator{\n" +
                "child=" + child +
                ", cAtomList=" + cAtomList +
                "\n}";
    }
}
