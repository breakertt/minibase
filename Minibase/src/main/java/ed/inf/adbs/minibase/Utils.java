package ed.inf.adbs.minibase;

import ed.inf.adbs.minibase.base.RelationalAtom;
import ed.inf.adbs.minibase.base.Term;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {

    public static String join(Collection<?> c, String delimiter) {
        return c.stream()
                .map(x -> x.toString())
                .collect(Collectors.joining(delimiter));
    }

    public static <T> List<T> cloneList(List<T> list) {
        return new ArrayList<>(list);
    }

    public static Integer[] genAtomPosMap(RelationalAtom input, RelationalAtom output) throws Exception {
        List<Term> outputTerms = output.getTerms();
        Integer[] reorderArray = new Integer[outputTerms.size()];
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
        return reorderArray;
    }
}
