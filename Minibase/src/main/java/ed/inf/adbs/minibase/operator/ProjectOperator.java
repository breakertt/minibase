package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.base.RelationalAtom;
import ed.inf.adbs.minibase.base.Term;
import ed.inf.adbs.minibase.datamodel.Tuple;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class ProjectOperator extends Operator {

    private final Operator child;
    private HashSet<String> tupleRecord;
    private Integer[] reorderArray; // head term pos -> input term pos

    public ProjectOperator(Operator child, RelationalAtom input, RelationalAtom output) throws Exception {
        this.child = child;
        this.tupleRecord = new HashSet<>();
        analysisProject(input, output);
    }

    private void analysisProject(RelationalAtom input, RelationalAtom output) throws Exception {
        List<Term> outputTerms = output.getTerms();
        reorderArray = new Integer[outputTerms.size()];
        List<String> inputTermStrList = input.getTermStrList();
        for (int i = 0; i < outputTerms.size(); i++) {
            String outputTermStr = outputTerms.get(i).toString();
            int j;
            for (j = 0; j < inputTermStrList.size(); j++) {
                if (inputTermStrList.get(j).equals(outputTermStr)) {
                    reorderArray[i] = j;
                    break;
                }
            }
            if (j == inputTermStrList.size()) {
                throw new Exception("head variable not in body");
            }
        }
    }

    @Override
    public Tuple getNextTuple() {
        Tuple tuple = null;
        while ((tuple = child.getNextTuple()) != null) {
            Tuple projectedTuple = new Tuple(tuple, reorderArray);
            String projectedTupleStr = projectedTuple.toString();
            if (!tupleRecord.contains(projectedTupleStr)) {
                tupleRecord.add(projectedTupleStr);
                return projectedTuple;
            }
        }
        return null;
    }

    @Override
    public void reset() {
        child.reset();
    }

    @Override
    public String toString() {
        return "ProjectOperator{" +
                "child=" + child +
                ", reorderArray=" + Arrays.toString(reorderArray) +
                '}';
    }
}
