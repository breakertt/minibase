package ed.inf.adbs.minibase;

import ed.inf.adbs.minibase.base.*;
import ed.inf.adbs.minibase.datamodel.Catalog;
import ed.inf.adbs.minibase.operator.*;
import ed.inf.adbs.minibase.parser.QueryParser;

import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Interpreter {

    private Operator root;
    private final Query query;

    public Interpreter(String databaseDir, String inputFile) throws Exception {
        Catalog.INSTANCE.loadCatalog(databaseDir);
        query = QueryParser.parse(Paths.get(inputFile));
        planQuery(query);
    }

    private void planQuery(Query query) throws Exception {
        RelationalAtom head = query.getHead();
        List<Atom> bodyAtoms = query.getBody();

        List<RelationalAtom> bodyRelationalAtoms = bodyAtoms.stream().filter(atom -> atom instanceof RelationalAtom)
                .map(atom -> (RelationalAtom) atom).collect(Collectors.toList());
        List<ComparisonAtom> bodyComparisonAtoms = bodyAtoms.stream().filter(atom -> atom instanceof ComparisonAtom)
                .map(atom -> (ComparisonAtom) atom).collect(Collectors.toList());

        if (bodyRelationalAtoms.size() == 0) {
            throw new Exception("invalid query");
        } else {
            planRelationQuery(head, bodyRelationalAtoms, bodyComparisonAtoms);
        }
    }

    private void planRelationQuery(RelationalAtom head, List<RelationalAtom> bodyRelationalAtoms, List<ComparisonAtom> bodyComparisonAtoms) throws Exception {
        // scan & select within atom
        List<Operator> baseOperators = new ArrayList<>(bodyComparisonAtoms.size());
        for (RelationalAtom rAtom : bodyRelationalAtoms) {
            boolean requireSelectionImplicit = rAtom.getTerms().stream().anyMatch(term -> term instanceof Constant);
            List<ComparisonAtom> eliminatedComparisonAtoms = getIndividualComparisonAtoms(bodyComparisonAtoms, rAtom);
            boolean requireSelectionExplicit = eliminatedComparisonAtoms.size() > 0;
            Operator baseRoot = new ScanOperator(rAtom.getName());
            if (requireSelectionExplicit || requireSelectionImplicit) {
                baseRoot = new SelectOperator(baseRoot, rAtom, eliminatedComparisonAtoms);
            }
            baseOperators.add(baseRoot);
        }
        // join & select cross atoms
        root = baseOperators.get(0);
        RelationalAtom atomBodyOutput = bodyRelationalAtoms.get(0);
        for (int i = 1; i < baseOperators.size(); i++) {
            RelationalAtom rightAtom = bodyRelationalAtoms.get(i);
            List<ComparisonAtom> crossRelationComparisonAtoms = getCrossComparisonAtoms(bodyComparisonAtoms, atomBodyOutput, rightAtom);
            root = new JoinOperator(root, baseOperators.get(i), atomBodyOutput, rightAtom);
            atomBodyOutput = ((JoinOperator) root).getAtomOutput();
            if (crossRelationComparisonAtoms.size() > 0) {
                root = new SelectOperator(root, atomBodyOutput, crossRelationComparisonAtoms);
            }
        }
        // aggregate
        int i;
        for (i = 0; i < head.getTerms().size(); i++) {
            Term term = head.getTerms().get(i);
            if (term instanceof AvgVariable) {
                root = new AvgOperator(root, atomBodyOutput, head);
                break;
            }
            if (term instanceof SumVariable) {
                root = new SumOperator(root, atomBodyOutput, head);
                break;
            }
        }
        // no aggregate then project
        if (i == head.getTerms().size()) {
            if (!atomBodyOutput.getTermStr().equals(head.getTermStr())) {
                root = new ProjectOperator(root, atomBodyOutput, head);
            }
        }
    }

    private List<ComparisonAtom> getIndividualComparisonAtoms(List<ComparisonAtom> bodyComparisonAtoms, RelationalAtom rAtom) {
        List<ComparisonAtom> individualComparisonAtoms = new ArrayList<>();
        List<String> rAtomStrList = rAtom.getTermStrList();
        for (ComparisonAtom cAtom : bodyComparisonAtoms) {
            // delete all comparison atoms with non-related variables
            Term term = cAtom.getTerm1();
            if (term instanceof Variable) {
                if (!rAtomStrList.contains(term.toString())) {
                    continue;
                }
            }
            term = cAtom.getTerm2();
            if (term instanceof Variable) {
                if (!rAtomStrList.contains(term.toString())) {
                    continue;
                }
            }
            individualComparisonAtoms.add(cAtom);
        }
        return individualComparisonAtoms;
    }

    private List<ComparisonAtom> getCrossComparisonAtoms(List<ComparisonAtom> bodyComparisonAtoms, RelationalAtom leftAtom, RelationalAtom rightAtom) {
        List<ComparisonAtom> crossRelationComparisonAtoms = new ArrayList<>();
        List<String> leftAtomTermStrList = leftAtom.getTermStrList();
        List<String> rightAtomTermStrList = rightAtom.getTermStrList();
        for (ComparisonAtom cAtom: bodyComparisonAtoms) {
            Term term1 = cAtom.getTerm1();
            String term1Str = cAtom.getTerm1().toString();
            Term term2 = cAtom.getTerm2();
            String term2Str = cAtom.getTerm2().toString();
            if (!(term1 instanceof Variable && term2 instanceof Variable)) continue;
            boolean term1InLeft = leftAtomTermStrList.contains(term1Str);
            boolean term1InRight = rightAtomTermStrList.contains(term1Str);
            boolean term2InLeft = leftAtomTermStrList.contains(term2Str);
            boolean term2InRight = rightAtomTermStrList.contains(term2Str);
            if (term1InLeft && !term1InRight && term2InRight && !term2InLeft) {
                crossRelationComparisonAtoms.add(cAtom);
            }
            if (!term1InLeft && term1InRight && !term2InRight && term2InLeft) {
                crossRelationComparisonAtoms.add(cAtom);
            }
        }
        return crossRelationComparisonAtoms;
    }

    public void dump(PrintStream ps) {
        root.dump(ps);
    }

    public Operator getRoot() {
        return root;
    }

}
