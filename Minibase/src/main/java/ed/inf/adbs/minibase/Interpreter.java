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

/**
 * An Interpreter class for 1. prepare the database 2. build the query three 3. expose a interface for query evaluation
 * results (i.e. dump)
 */
public class Interpreter {

    private Operator root; // the root of query tree
    private final Query query; // the query to evaluate

    // Unpacked query
    private RelationalAtom head; // the head of query
    private List<RelationalAtom> bodyRelationalAtoms; // the relational atoms in query body
    private List<ComparisonAtom> bodyComparisonAtoms; // the comparison atoms in query body

    /**
     * Constructor for Interpreter
     * @param databaseDir the path of database
     * @param inputFile the path of query
     */
    public Interpreter(String databaseDir, String inputFile) throws Exception {
        Catalog.INSTANCE.loadCatalog(databaseDir);
        query = QueryParser.parse(Paths.get(inputFile));
        unpackQueryPlan();
        planRelationQuery();
    }

    /**
     * Unpack the query to three parts
     */
    private void unpackQueryPlan() throws Exception {
        head = query.getHead();
        List<Atom> bodyAtoms = query.getBody();
        bodyRelationalAtoms = bodyAtoms.stream().filter(atom -> atom instanceof RelationalAtom)
                .map(atom -> (RelationalAtom) atom).collect(Collectors.toList());
        bodyComparisonAtoms = bodyAtoms.stream().filter(atom -> atom instanceof ComparisonAtom)
                .map(atom -> (ComparisonAtom) atom).collect(Collectors.toList());
        if (bodyRelationalAtoms.size() == 0) {
            throw new Exception("invalid query");
        }
    }

    /**
     * Build a query tree with a three-pass:
     * 1. build base operator trees which only includes ScanOperator (compulsory) and SelectOperator (single relation
     * wide, optional);
     * 2. based on the base operators, build a left-deep join three;
     * 3. compare the atom after join with head, add a ProjectOperator or AggOperator if in need.
     */
    private void planRelationQuery() throws Exception {
        // scan & select within atom
        List<Operator> baseOperators = buildBaseOperators();
        // join & select cross atoms
        RelationalAtom atomBodyOutput = buildJoinTreeRoot(baseOperators);
        // finalization with aggregation or projection
        boolean isAggQuery = buildAggregationRoot(atomBodyOutput);
        // no aggregate then project
        if (!isAggQuery) {
            buildProjectionRoot(atomBodyOutput);
        }
    }

    /**
     * Build a list of base operators which only consider single-atom-wide selection and scan, and no join
     * 1. Build a ScanOperator for every relational atom.
     * 2. Check whether this atom needs single atom wide select (push down selection), if so build a SelectOperator for
     * this atom with selection conditions (comparison atoms)
     * 2.1 Check whether this atom needs implicit selection (one term in the atom is a constant)
     * 2.2 Check whether this atom needs explicit selection (a comparison atom does not contain a variable this atom
     * does not have)
     * @return a List of base operators (no join)
     */
    private List<Operator> buildBaseOperators() {
        List<Operator> baseOperators = new ArrayList<>(bodyComparisonAtoms.size());
        for (RelationalAtom rAtom : bodyRelationalAtoms) {
            boolean requireSelectionImplicit = rAtom.getTerms().stream().anyMatch(term -> term instanceof Constant);
            List<ComparisonAtom> matchedComparisonAtoms = getIndividualComparisonAtoms(rAtom);
            boolean requireSelectionExplicit = matchedComparisonAtoms.size() > 0;
            Operator baseRoot = new ScanOperator(rAtom.getName());
            if (requireSelectionExplicit || requireSelectionImplicit) {
                baseRoot = new SelectOperator(baseRoot, rAtom, matchedComparisonAtoms);
            }
            baseOperators.add(baseRoot);
        }
        return baseOperators;
    }

    /**
     * Build a left-deep join tree on base operators
     * 1. Build a Join Operator on two base operators, then take the output as updated left operator. The join operator
     * can resolve implicit equality selections, but not explicit ones in Comparison Atoms.
     * 2. Iterate over the ComparisonAtom list, add a SelectOperator on the top of JoinOperator if there is cross
     * comparison required.
     * @param baseOperators a List of base operators (no join)
     * @return the atom of join-tree root
     */
    private RelationalAtom buildJoinTreeRoot(List<Operator> baseOperators) {
        root = baseOperators.get(0);
        RelationalAtom atomBodyOutput = bodyRelationalAtoms.get(0);
        for (int i = 1; i < baseOperators.size(); i++) {
            RelationalAtom rightAtom = bodyRelationalAtoms.get(i);
            List<ComparisonAtom> crossRelationComparisonAtoms = getCrossComparisonAtoms(atomBodyOutput, rightAtom);
            root = new JoinOperator(root, baseOperators.get(i), atomBodyOutput, rightAtom);
            atomBodyOutput = ((JoinOperator) root).getAtomOutput();
            if (crossRelationComparisonAtoms.size() > 0) {
                root = new SelectOperator(root, atomBodyOutput, crossRelationComparisonAtoms);
            }
        }
        return atomBodyOutput;
    }

    private boolean buildAggregationRoot(RelationalAtom atomBodyOutput) throws Exception {
        Term lastTerm = head.getTerms().get(head.getTerms().size() - 1);
        if (lastTerm instanceof AvgVariable) {
            root = new AvgOperator(root, atomBodyOutput, head);
            return true;
        }
        if (lastTerm instanceof SumVariable) {
            root = new SumOperator(root, atomBodyOutput, head);
            return true;
        }
        return false;
    }

    private boolean buildProjectionRoot(RelationalAtom atomBodyOutput) throws Exception {
        if (!atomBodyOutput.getTermStr().equals(head.getTermStr())) {
            root = new ProjectOperator(root, atomBodyOutput, head);
            return true;
        }
        return false;
    }

    /**
     * Filter out the comparison atoms can be performed only on one particular atom
     * @param rAtom a relational atom
     * @return a list of comparison atoms do not contain a variable the input atom does not have
     */
    private List<ComparisonAtom> getIndividualComparisonAtoms(RelationalAtom rAtom) {
        List<ComparisonAtom> individualComparisonAtoms = new ArrayList<>();
        List<String> rAtomStrList = rAtom.getTermStrList();
        for (ComparisonAtom cAtom : bodyComparisonAtoms) {
            // delete all comparison atoms with non-related variables
            Term term1 = cAtom.getTerm1();
            if (term1 instanceof Variable) {
                if (!rAtomStrList.contains(term1.toString())) continue;
            }
            Term term2 = cAtom.getTerm2();
            if (term2 instanceof Variable) {
                if (!rAtomStrList.contains(term2.toString())) continue;
            }
            individualComparisonAtoms.add(cAtom);
        }
        return individualComparisonAtoms;
    }

    /**
     * Filter out all comparison atoms can only be performed in this join
     * Rules:
     * 1. the comparison atom is a variable - variable comparison
     * 2. the two variables are not in one atom before join
     * 3. one variable in comparison occurred in one relation, and another one occurred in another
     *
     * @param leftAtom the relation at left side on the query tree
     * @param rightAtom the relation at right side on the query tree
     * @return a list of comparison atoms which can only applied on join
     */
    private List<ComparisonAtom> getCrossComparisonAtoms(RelationalAtom leftAtom, RelationalAtom rightAtom) {
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
            boolean isCrossRelationAtom = (term1InLeft && !term1InRight && term2InRight && !term2InLeft);
            isCrossRelationAtom = isCrossRelationAtom || (!term1InLeft && term1InRight && !term2InRight && term2InLeft);
            if (!isCrossRelationAtom) continue;
            crossRelationComparisonAtoms.add(cAtom);
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
