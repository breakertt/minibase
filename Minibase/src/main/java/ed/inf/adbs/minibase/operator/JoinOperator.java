package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.base.RelationalAtom;
import ed.inf.adbs.minibase.base.Term;
import ed.inf.adbs.minibase.datamodel.Pair;
import ed.inf.adbs.minibase.datamodel.Tuple;

import java.util.ArrayList;
import java.util.List;

/**
 * An Operator for joining two tuples and atoms, and also solve the implicit equality while joining
 * This operator will first find the schema of output atom after joining, meanwhile, eliminating variables with same
 * name and record the positions of identical variables in child1 atom and child2 atom. In every getNextTuple() call,
 * two tuples will be combined on the schema generated, and check the implicit equalities.
 */
public class JoinOperator extends Operator {

    private final Operator child1;
    private final Operator child2;

    private RelationalAtom atomOutput; // the output atom, referring to the schema
    private Integer[] reorderArray; // for each output atom term, the positions of terms in atom1 and atom2
    // pairs which position .a term in left tuple should equal to position .b term in right tuple
    private final List<Pair> equalPairList;

    private Tuple tuple1; // persist tuple1 for outer loop of nested join

    /**
     * Constructor for join operator
     * @param child1 left child operator
     * @param child2 right child operator
     * @param atom1 atom from left child operator
     * @param atom2 atom from left child operator
     */
    public JoinOperator(Operator child1, Operator child2, RelationalAtom atom1, RelationalAtom atom2) {
        this.child1 = child1;
        this.child2 = child2;
        this.equalPairList = new ArrayList<>();
        analysisJoin(atom1, atom2);
    }


    /**
     * Find the output schema, the reorder mapping and the implicit equalities
     * @param atom1 atom from left child operator
     * @param atom2 atom from left child operator
     */
    private void analysisJoin(RelationalAtom atom1, RelationalAtom atom2) {
        List<Term> atomOutputTerms = new ArrayList<>();
        atomOutput = new RelationalAtom(atom1.getName() + "_JOIN_" + atom2.getName(), atomOutputTerms);
        List<Term> atom1Terms = atom1.getTerms();
        List<String> atom1TermStrList = atom1.getTermStrList();
        List<Term> atom2Terms = atom2.getTerms();
        // add all left atom variables into atom to output
        atomOutputTerms.addAll(atom1Terms);
        int atom1Length = atom1Terms.size();
        int atom2Length = atom2Terms.size();
        List<Integer> reorderList = new ArrayList<>();
        // add all left atom variables into reorderList as (i,i)
        for (int i = 0; i < atom1Length; i++) {
            reorderList.add(i);
        }
        for (int i = 0; i < atom2Length; i++) {
            Term atom2Term = atom2Terms.get(i);
            // if term in right atom already exists in left atom, add the term into equalities pair list
            if (atom1TermStrList.contains(atom2Term.toString())) {
                equalPairList.add(new Pair(atom1TermStrList.indexOf(atom2Term.toString()), i));
            } else {
                // if term not exists in left atom, add the term into output
                atomOutputTerms.add(atom2Term);
                reorderList.add(i + atom1Length);
            }
        }
        reorderArray = reorderList.toArray(new Integer[0]);
    }

    /**
     * Return the atom for output, can be useful when building another join operator over this join operator
     * @return the atom for output
     */
    public RelationalAtom getAtomOutput() {
        return atomOutput;
    }

    /**
     * Check the implicit equalities
     * @param tuple1 tuple from left operator
     * @param tuple2 tuple from right operator
     * @param equalPairList the position pairs of identical variables in child1 atom and child2 atom
     * @return whether the two tuples pass all implicit equalities
     */
    private boolean checkEqualOnVariables(Tuple tuple1, Tuple tuple2, List<Pair> equalPairList) {
        boolean isEqualOnVariables = true;
        for (Pair pair: equalPairList) {
            Comparable item1Val = tuple1.getItems().get(pair.a).getValue();
            Comparable item2Val = tuple2.getItems().get(pair.b).getValue();
            isEqualOnVariables = isEqualOnVariables && (item1Val.compareTo(item2Val) == 0);
        }
        return isEqualOnVariables;
    }

    @Override
    public Tuple getNextTuple() {
        while (true) {
            // if tuple1 is not loaded, load; to be noticed, do not get new tuple1 for every getNextTuple() call
            if (tuple1 == null) {
                tuple1 = child1.getNextTuple();
                // if load failed then no need to continue
                if (tuple1 == null) {
                    return null;
                }
            }
            Tuple tuple2 = child2.getNextTuple();
            // if tuple2 load is failed, this can be two possibilities - 1. child2 operators is empty, 2. child2
            // operator needs to be reset
            if (tuple2 == null) {
                child2.reset(); // reset the child2 operators
                tuple2 = child2.getNextTuple(); // try to get the first tuple after reset
                tuple1 = child1.getNextTuple(); // as inner loop finishes, should all reset the outer loop
                // if tuple2 is still null, indicating child2 operator is empty, return; if tuple1 is null, indicating
                // child1 operator is also finished
                if (tuple2 == null || tuple1 == null) {
                    return null;
                }
            }
            // return only implicit equal checks are passed
            if (checkEqualOnVariables(tuple1, tuple2, equalPairList)) {
                return new Tuple(tuple1, tuple2, atomOutput.getName(), reorderArray);
            }
        }
    }

    @Override
    public void reset() {
        child1.reset();
        child2.reset();
    }

    @Override
    public String toString() {
        return "JoinOperator{\n" +
                "child1=" + child1 +
                ", \nchild2=" + child2 +
                "\n}";
    }

}
