package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.Utils;
import ed.inf.adbs.minibase.base.RelationalAtom;
import ed.inf.adbs.minibase.datamodel.Tuple;

import java.util.Arrays;
import java.util.HashSet;

public class ProjectOperator extends Operator {

    private final Operator child;
    private final HashSet<String> tupleRecord;
    private Integer[] reorderArray; // head term pos -> input term pos

    public ProjectOperator(Operator child, RelationalAtom input, RelationalAtom output) throws Exception {
        this.child = child;
        this.tupleRecord = new HashSet<>();
        analysisProject(input, output);
    }

    private void analysisProject(RelationalAtom input, RelationalAtom output) throws Exception {
        reorderArray = Utils.genAtomPosMap(input, output);
    }

    @Override
    public Tuple getNextTuple() {
        Tuple tuple;
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
