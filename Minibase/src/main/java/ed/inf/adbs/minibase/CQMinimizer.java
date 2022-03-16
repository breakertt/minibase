package ed.inf.adbs.minibase;

import ed.inf.adbs.minibase.base.*;
import ed.inf.adbs.minibase.parser.QueryParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * Minimization of conjunctive queries
 *
 */
public class CQMinimizer {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: CQMinimizer input_file output_file");
            return;
        }

        String inputFile = args[0];
        String outputFile = args[1];

        cqMinimizerPipe(inputFile, outputFile);
    }

    /**
     * Pipeline for CQMinimizer
     * Parse input, minimize query, then write minimized query to output
     * @param inputFile the input file containing original query
     * @param outputFile the output file to write minimized query
     */
    public static void cqMinimizerPipe(String inputFile, String outputFile) {
        // parse query string
        Query query = readCq(inputFile);

        // get minimized query
        Query minimizedQuery = minimizeCq(query);

        // write minimized query to outputFile
        writeCq(minimizedQuery, outputFile);
    }

    /**
     * Read and parse a query from provided file
     * @param inputFile the input file containing original query
     * @return the query to be minimized
     */
    private static Query readCq(String inputFile) {
        Query query = null;
        try {
            query = QueryParser.parse(Paths.get(inputFile));
        }
        catch (IOException e)
        {
            System.err.println("Exception occurred during parsing");
            e.printStackTrace();
        }
        return query;
    }

    /**
     * Write a query into provided file
     * @param query  a query
     * @param outputFile the file to be written to
     */
    private static void writeCq(Query query, String outputFile) {
        try{
            File file = new File(outputFile);
            File parentFile = file.getParentFile();
            if (parentFile != null) {
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            fw.write(query.toString());
            fw.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Main CQ minimization procedure
     * Try to remove one atom from the query, then build a homomorphism from Q to Q/{a}
     * @param query the query to be minimized
     * @return a minimized query
     */
    public static Query minimizeCq(Query query) {
        RelationalAtom head = query.getHead();
        List<Atom> body = query.getBody();
        boolean isRemoved = true;
        // stop until no atom can be removed
        while (isRemoved) {
            isRemoved = false;
            // try to remove one atom
            for (int i = 0; i < body.size(); i++) {
                // Q/{a}
                List<Atom> newBody = removeAtom(body, i);
                // naive tree-pruning - let the atom to be removed be mapped first
                List<Atom> tmpBody = removeAtom(body, i);
                tmpBody.add(0, body.get(i));
                // try build homo from Q -> Q/{a}
                boolean success = buildHomo(tmpBody, newBody, head, new ArrayList<>());
                if (success) {
                    body = newBody; // Q is minimized to Q/{a}
                    isRemoved = true;
                    break;
                }
            }
        }
//        System.out.println("Final Body: " + body);
        return new Query(head, body);
    }

    /**
     * Remove one atom in a list of atoms with given position
     * @param body a list of atoms
     * @param atomPos the pos of atom to be removed
     * @return a new list of atoms which one atom is removed
     */
    private static List<Atom> removeAtom(List<Atom> body, int atomPos) {
        List<Atom> newBody = Utils.cloneList(body);
        newBody.remove(atomPos);
        return newBody;
    }

    /**
     * Build a homomorphism with a Depth-First-Search like algorithm
     * @param body query body of the atoms in this list will be eliminated entirely to build a homomorphism
     * @param newBody query body of the atoms which can be mapped to with one atom removed (i.e. Q/{a})
     * @param head head of query
     * @param partialHomos a list of partial homomorphism mappings, which indicates a full homo if the body is empty
     * @return whether a homo can be built between body to newBody
     */
    private static boolean buildHomo(List<Atom> body, List<Atom> newBody, RelationalAtom head, List<HashMap<String, Term>> partialHomos ) {
        // a homo is built, as all atoms in the old body are eliminated
        if (body.size() == 0) {
            return true;
        }
        // the first atom in the body is going to be eliminated, as all atoms will be eliminated soon or later
        RelationalAtom atomToEliminate = (RelationalAtom) body.get(0);
        for (int i = 0; i < newBody.size(); i++) {
            // find another atom in Q/{a}, which atomToEliminate will be mapped to
            RelationalAtom atomCandidateTransform = (RelationalAtom) newBody.get(i);
            // basic term-level checks to show
            // whether a mapping from atomToEliminate to atomCandidateTransform is feasible
            if (!checkTermLevelHomo(atomToEliminate, atomCandidateTransform, head)) continue;
            // build partial homomorphism mapping between two given atoms, with previous built partial homo mapping
            HashMap<String, Term> homoMapping = buildTermLevelHomo(atomToEliminate, atomCandidateTransform, partialHomos);
            if (homoMapping == null) continue; // build homo failed
            // a partial homo mapping which does not violate previous mappings is built, the atom can be removed, and
            // the new partial mapping is added
            List<Atom> tmpBody = removeAtom(body, 0);
            List<HashMap<String, Term>> tmpPartialHomos = Utils.cloneList(partialHomos);
            if (homoMapping.size() > 0) tmpPartialHomos.add(homoMapping);
            // continue build further homo mappings - go deep
            if (buildHomo(tmpBody, newBody, head, tmpPartialHomos)) return true;
        }
        // if no further mappings can be built, stop and backtrace
        return false;
    }

    /**
     * build a term level homo from one atom to another atom, without violating the given current homo mapping
     * @param src atom to transform
     * @param dst atom for transform to
     * @param partialHomos previous homo mappings which this homo should follow
     * @return a new partial homo mapping
     */
    private static HashMap<String, Term> buildTermLevelHomo(RelationalAtom src, RelationalAtom dst, List<HashMap<String, Term>> partialHomos) {
        List<Term> srcTerms = src.getTerms();
        List<Term> dstTerms = dst.getTerms();
        HashMap<String, Term> localHomo = new HashMap<>();
        int i;
        // compare all the terms
        for (i = 0; i < srcTerms.size(); i++) {
            Term keyTerm = srcTerms.get(i);
            Term value = dstTerms.get(i);
            String key = srcTerms.get(i).toString();
            String valueStr = dstTerms.get(i).toString();
            // two terms are both constant, if equal then skip, if not equal then fail
            if (keyTerm instanceof Constant && value instanceof Constant) {
                if (key.equals(valueStr)) {
                    continue;
                } else {
                    return null;
                }
            }
            boolean isKeyExist = false;
            // apply previous homo mappings, fail if the being inconsistent and skip for duplicate
            for (HashMap<String, Term> partialHomo: partialHomos) {
                if (partialHomo.containsKey(key)) {
                    if (!partialHomo.get(key).toString().equals(valueStr)) {
                        return null;
                    } else {
                        isKeyExist = true;
                    }
                }
            }
            if (isKeyExist) continue;
            // check one-to-many violations and skip the duplicate
            if (localHomo.containsKey(key)) {
                if (!localHomo.get(key).toString().equals(valueStr)) {
                    return null;
                } else {
                    isKeyExist = true;
                }
            }
            if (isKeyExist) continue;
            // add new homo
            localHomo.put(key, value);
        }
        return localHomo;
    }

    /**
     * Check whether a source relational atom can transform to a dst relational atom with a homo mapping.
     * Rules: Names and length of two relational atoms should be consistent. For terms, variable to constant, variable
     * to variable, constant x to constant x is allowed; no constant to variable or constant x to constant y or map
     * distinguish variable to other is allowed
     * @param src an atom to be transformed
     * @param dst an atom for src to transform to
     * @param head head atom of the cq containing distinguish variables
     * @return a boolean indicating whether the basic homo can be built between src and dst
     */
    private static boolean checkTermLevelHomo(RelationalAtom src, RelationalAtom dst, RelationalAtom head) {
        // name mismatch
        if (!src.getName().equals(dst.getName())) return false;
        // extract multiple time used variables
        List<String> headTermStrList = head.getTermStrList();
        List<Term> srcTerms = src.getTerms();
        List<Term> dstTerms = dst.getTerms();
        // amount of terms mismatch
        if (srcTerms.size() != dstTerms.size()) return false;
        // check each term
        for (int i = 0; i < srcTerms.size(); i++) {
            Term srcTerm = srcTerms.get(i);
            Term dstTerm = dstTerms.get(i);
            boolean isSameTerm = srcTerm.toString().equals(dstTerm.toString());
            if (srcTerm instanceof Constant) {
                // constant to variable
                if (dstTerm instanceof Variable) return false;
                // constant x to constant y
                if (!isSameTerm) return false;
            } else {
                // distinguish variable to other
                if (headTermStrList.contains(srcTerm.toString()) && !isSameTerm) {
                    return false;
                }
            }
        }
        // pass all checks
        return true;
    }

}
