package ed.inf.adbs.minibase;

import ed.inf.adbs.minibase.base.*;
import ed.inf.adbs.minibase.datamodel.Catalog;
import ed.inf.adbs.minibase.operator.Operator;
import ed.inf.adbs.minibase.operator.ProjectOperator;
import ed.inf.adbs.minibase.operator.ScanOperator;
import ed.inf.adbs.minibase.operator.SelectOperator;
import ed.inf.adbs.minibase.parser.QueryParser;

import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class Interpreter {

    Operator root;
    Query query;

    public Interpreter(String databaseDir, String inputFile) throws Exception {
        Catalog.INSTANCE.loadCatalog(databaseDir);
        query = QueryParser.parse(Paths.get(inputFile));
        planQuery(query);
    }

    public Interpreter(String databaseDir, Query query) throws Exception {
        Catalog.INSTANCE.loadCatalog(databaseDir);
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
        } else if (bodyRelationalAtoms.size() == 1) {
            planSingleRelationQuery(head, bodyRelationalAtoms, bodyComparisonAtoms);
        } else {
            planMultipleRelationQuery(head, bodyRelationalAtoms, bodyComparisonAtoms);
        }
    }

    private void planSingleRelationQuery(RelationalAtom head, List<RelationalAtom> bodyRelationalAtoms, List<ComparisonAtom> bodyComparisonAtoms) throws Exception {
        RelationalAtom rAtom = bodyRelationalAtoms.get(0);
        Operator scanOp = new ScanOperator(rAtom.getName());
        boolean requireProjection = !rAtom.getTermStr().equals(head.getTermStr());
        boolean requireSelectionExplicit = bodyComparisonAtoms.size() >= 1;
        boolean requireSelectionImplicit = rAtom.getTerms().stream().anyMatch(term -> term instanceof Constant);
        boolean requireSelection = requireSelectionExplicit || requireSelectionImplicit;
        if (!requireProjection && !requireSelection) {
            root = scanOp;
        } else if (requireProjection && !requireSelection) {
            root = new ProjectOperator(scanOp, rAtom, head);
        } else if (!requireProjection && requireSelection) {
            root = new SelectOperator(scanOp, rAtom, bodyComparisonAtoms);
        } else if (requireProjection && requireSelection) {
            Operator selectOp = new SelectOperator(scanOp, rAtom, bodyComparisonAtoms);
            root = new ProjectOperator(selectOp, rAtom, head);
        }
    }

    private void planMultipleRelationQuery(RelationalAtom head, List<RelationalAtom> bodyRelationalAtoms, List<ComparisonAtom> bodyComparisonAtoms) throws Exception {

    }

    public void dump(PrintStream ps) {
        root.dump(ps);
    }
}
