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
     *
     * Parse input, minimize query, then write minimized query to output
     *
     */
    public static void cqMinimizerPipe(String inputFile, String outputFile) {
        // parse query string
        Query query = readCq(inputFile);

        // get minimized query
        Query minimizedQuery = minimizeCq(query);

        // write minimized query to outputFile
        writeCq(minimizedQuery, outputFile);
    }

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
     * CQ minimization procedure
     *
     * Assume the body of the query from inputFile has no comparison atoms
     * but could potentially have constants in its relational atoms.
     *
     * Iteratively find atom to remove
     *
     */
    public static Query minimizeCq(Query query) {
        RelationalAtom head = query.getHead();
        List<Atom> body = query.getBody();


        // Remove one, try build a homomorphism from original one to one removed
        boolean isRemoved = true;
        while (isRemoved) {
            isRemoved = false;
            for (int i = 0; i < body.size(); i++) {
                List<Atom> newBody = removeAtom(body, i);
                // give the atom be removed the highest priority
                List<Atom> tmpBody = removeAtom(body, i);
                tmpBody.add(0, body.get(i));
                System.out.println("Initial Body: " + tmpBody);
                boolean success = buildHomo(body, newBody, head, new ArrayList<>());
                if (success) {
                    System.out.println("Remove Atom : " + body.get(i) + " at Position " + i);
                    body = newBody;
                    System.out.println("New Body : " + body);
                    isRemoved = true;
                    break;
                }
            }
        }
        System.out.println("Final Body: " + body);
        return new Query(head, body);
    }

    private static <T> List<T> cloneList(List<T> list) {
        return new ArrayList<>(list);
    }

    private static List<Atom> removeAtom(List<Atom> body, int atomPos) {
        List<Atom> newBody = cloneList(body);
        newBody.remove(atomPos);
        return newBody;
    }

    private static boolean buildHomo(List<Atom> body, List<Atom> newBody, RelationalAtom head, List<HashMap<String, Term>> partialHomos ) {
        if (body.size() == 0) {
            System.out.println("Q -> Q/{a} Homomorphism: " + partialHomos);
            return true;
        }
        RelationalAtom atomToEliminate = (RelationalAtom) body.get(0);
        for (int i = 0; i < newBody.size(); i++) {
            RelationalAtom atomCandidateTransform = (RelationalAtom) newBody.get(i);
            // term-level checks
            if (!checkTermLevelHomo(atomToEliminate, atomCandidateTransform, head)) continue;
            // build partial homomorphism mapping
            HashMap<String, Term> homoMapping = buildTermLevelHomo(atomToEliminate, atomCandidateTransform, partialHomos);
            if (homoMapping == null) continue; // build homo failed
            // remove another atom with previous homo mapping and new built one
            List<Atom> tmpBody = removeAtom(body, 0);
            List<HashMap<String, Term>> tmpPartialHomos = cloneList(partialHomos);
            if (homoMapping.size() > 0) tmpPartialHomos.add(homoMapping);
            if (buildHomo(tmpBody, newBody, head, tmpPartialHomos)) return true;
        }
        return false;
    }

    private static HashMap<String, Term> buildTermLevelHomo(RelationalAtom src, RelationalAtom dst, List<HashMap<String, Term>> partialHomos) {
        List<Term> srcTerms = src.getTerms();
        List<Term> dstTerms = dst.getTerms();

        HashMap<String, Term> localHomo = new HashMap<>();
        int i;
        for (i = 0; i < srcTerms.size(); i++) {
            Term keyTerm = srcTerms.get(i);
            Term value = dstTerms.get(i);
            String key = srcTerms.get(i).toString();
            String valueStr = dstTerms.get(i).toString();
            if (keyTerm instanceof Constant && value instanceof Constant) {
                if (key.equals(valueStr)) {
                    continue;
                } else {
                    return null;
                }
            }
            boolean isKeyExist = false;
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
            if (localHomo.containsKey(key)) {
                if (!localHomo.get(key).toString().equals(valueStr)) {
                    return null;
                } else {
                    isKeyExist = true;
                }
            }
            if (isKeyExist) continue;
            localHomo.put(key, value);
        }
        return localHomo;
    }

    /**
     * Check whether a source relational atom can transform to a dst relational atom with a homo mapping.
     *
     * names of two relational atoms should be consistent;
     * for terms, variable to constant, variable to variable, constant x to constant x is allowed
     * no constant to variable or constant x to constant y or map distinguish variable to other is allowed
     *
     * @param src an atom to be transform
     * @param dst an atom for src to transform to
     * @param head head atom of the cq containing distinguish variables
     * @return a boolean indicating whether the basic homo can be built between src and dst
     */
    private static boolean checkTermLevelHomo(RelationalAtom src, RelationalAtom dst, RelationalAtom head) {
        // name mismatch
        if (!src.getName().equals(dst.getName())) return false;

        List<String> headTermStrList = head.getTermStrList();
        List<Term> srcTerms = src.getTerms();
        List<Term> dstTerms = dst.getTerms();

        // term number mismatch
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
