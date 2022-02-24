package ed.inf.adbs.minibase.operator;

import ed.inf.adbs.minibase.base.RelationalAtom;
import ed.inf.adbs.minibase.base.Term;
import ed.inf.adbs.minibase.datamodel.Tuple;

import java.util.List;
import java.util.stream.Collectors;

public class ProjectOperator extends Operator {

    Operator child;
    Integer[] reorderList; // head term pos -> input term pos

    ProjectOperator(Operator child, RelationalAtom input, RelationalAtom output) {
        this.child = child;
        analysisProjection(input, output);
    }

    private void analysisProjection(RelationalAtom input, RelationalAtom output) {
        List<Term> outputTerms = output.getTerms();
        reorderList = new Integer[outputTerms.size()];
        List<String> inputTermStrList = input.getTerms().stream().map(Object::toString).collect(Collectors.toList());
        for (int i = 0; i < outputTerms.size(); i++) {
            String outputTermStr = outputTerms.get(i).toString();
            int j;
            for (j = 0; j < inputTermStrList.size(); j++) {
                if (inputTermStrList.get(j).equals(outputTermStr)) {
                    reorderList[i] = j;
                }
            }
            if (j == inputTermStrList.size()) reorderList[i] = -1;
        }
    }

    @Override
    public Tuple getNextTuple() {
        Tuple tuple = child.getNextTuple();
        if (tuple != null) return new Tuple(tuple, reorderList);
        return null;
    }

    @Override
    public void reset() {
        child.reset();
    }
}
