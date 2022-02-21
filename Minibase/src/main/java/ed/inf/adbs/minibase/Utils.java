package ed.inf.adbs.minibase;

import ed.inf.adbs.minibase.base.RelationalAtom;
import ed.inf.adbs.minibase.base.Term;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {

    public static String join(Collection<?> c, String delimiter) {
        return c.stream()
                .map(x -> x.toString())
                .collect(Collectors.joining(delimiter));
    }

    public static boolean isTermInRelationalAtom(RelationalAtom a, Term t) {
        List<Term> aTerms = a.getTerms();
        String tStr = t.toString();
        for (Term aTerm : aTerms) {
            if (tStr.equals(aTerm.toString())) return true;
        }
        return false;
    }
}
