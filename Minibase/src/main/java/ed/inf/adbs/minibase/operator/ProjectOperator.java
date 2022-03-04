package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.Utils;
import ed.inf.adbs.minibase.base.RelationalAtom;
import ed.inf.adbs.minibase.datamodel.Tuple;

import java.util.Arrays;
import java.util.HashSet;

/**
 * An Operator for projecting variables to a specified format
 * A reorderArray will be constructed first, then in every call, the tuple will be reordered accordingly. Additionally,
 * if the tuple after record already occurred before, it will be skipped.
 */
public class ProjectOperator extends Operator {

    private final Operator child;

    private final HashSet<String> tupleRecord; // all tuples already produced, for discarding duplicates
    private Integer[] reorderArray; // an array which records the pos of head atom variables in the body atom

    /**
     * Constructor for project operator
     * Initialize hash set for recording previous tuples and analyze the relationship between input atom and output atom
     * @param child child operator
     * @param input the atom from child operator
     * @param output the atom to output
     * @throws Exception
     */
    public ProjectOperator(Operator child, RelationalAtom input, RelationalAtom output) throws Exception {
        this.child = child;
        this.tupleRecord = new HashSet<>();
        analysisProject(input, output);
    }

    /**
     * Generate the reorderArray, i.e. the relationship between input atom and output atom
     * @param input the atom from child operator
     * @param output the atom to output
     * @throws Exception
     */
    private void analysisProject(RelationalAtom input, RelationalAtom output) throws Exception {
        reorderArray = Utils.genAtomPosMap(input, output);
    }

    @Override
    public Tuple getNextTuple() {
        Tuple tuple;
        while ((tuple = child.getNextTuple()) != null) {
            Tuple projectedTuple = new Tuple(tuple, reorderArray);
            String projectedTupleStr = projectedTuple.toString();
            // return the tuple only if not seen previously
            if (!tupleRecord.contains(projectedTupleStr)) {
                tupleRecord.add(projectedTupleStr); // record the new tuple
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
