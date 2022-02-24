package ed.inf.adbs.minibase.base;

import ed.inf.adbs.minibase.Utils;

import java.util.List;
import java.util.stream.Collectors;

public class RelationalAtom extends Atom {
    private String name;

    private List<Term> terms;

    public RelationalAtom(String name, List<Term> terms) {
        this.name = name;
        this.terms = terms;
    }

    public String getName() {
        return name;
    }

    public List<Term> getTerms() {
        return terms;
    }

    @Override
    public String toString() {
        return name + "(" + Utils.join(terms, ", ") + ")";
    }

    public List<String> getTermStrList() {
        return this.getTerms().stream().map(Object::toString).collect(Collectors.toList());
    }

    public String getTermStr() {
        return Utils.join(terms, ", ");
    }

}
