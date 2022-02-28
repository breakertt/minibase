package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.base.RelationalAtom;
import ed.inf.adbs.minibase.base.Term;
import ed.inf.adbs.minibase.datamodel.Tuple;

import java.util.ArrayList;
import java.util.List;

public class JoinOperator extends Operator {

    private final Operator child1;
    private final Operator child2;
    private final RelationalAtom atom1;
    private final RelationalAtom atom2;
    private RelationalAtom atomOutput;
    private List<Integer> reorderList; // atomOutput term pos -> atom1&atom2 term pos
    private List<Pair> equalPairList;

    public JoinOperator(Operator child1, Operator child2, RelationalAtom atom1, RelationalAtom atom2) {
        this.child1 = child1;
        this.child2 = child2;
        this.atom1 = atom1;
        this.atom2 = atom2;
        this.reorderList = new ArrayList<>();
        this.equalPairList = new ArrayList<>();
        analysisJoin(atom1, atom2);
    }

    private void analysisJoin(RelationalAtom atom1, RelationalAtom atom2) {
        // find the output schema & the implicit equality
        List<Term> atomOutputTerms = new ArrayList<>();
        atomOutput = new RelationalAtom(atom1.getName() + "_JOIN_" + atom2.getName(), atomOutputTerms);
        List<Term> atom1Terms = atom1.getTerms();
        List<String> atom1TermStrList = atom1.getTermStrList();
        List<Term> atom2Terms = atom2.getTerms();
        atomOutputTerms.addAll(atom1Terms);
        int atom1Length = atom1Terms.size();
        int atom2Length = atom2Terms.size();
        for (int i = 0; i < atom1Length; i++) {
            reorderList.add(i);
        }
        for (int i = 0; i < atom2Length; i++) {
            Term atom2Term = atom2Terms.get(i);
            if (atom1TermStrList.contains(atom2Term.toString())) {
                equalPairList.add(new Pair(atom1TermStrList.indexOf(atom2Term.toString()), i));
            } else {
                atomOutputTerms.add(atom2Term);
                reorderList.add(i + atom1Length);
            }
        }
    }

    @Override
    public String toString() {
        return "JoinOperator{" +
                "child1=" + child1 +
                ", child2=" + child2 +
                ", atom1=" + atom1 +
                ", atom2=" + atom2 +
                ", atomOutput=" + atomOutput +
                ", reorderList=" + reorderList +
                ", equalPairList=" + equalPairList +
                '}';
    }

    @Override
    public Tuple getNextTuple() {

        return null;
    }

    @Override
    public void reset() {
        child1.reset();
        child2.reset();
    }

    static class Pair {
        public int a, b;
        Pair(int a, int b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public String toString() {
            return "Pair{" + a + ", " + b + "}";
        }
    }
}