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
import java.util.stream.Collectors;

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

//        parsingExample(inputFile);
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
                boolean success = buildHomo(body, newBody, head);
                if (success) {
                    System.out.println("Remove Atom : " + body + " at Position " + i);
                    body = newBody;
                    isRemoved = true;
                    break;
                }
            }
        }
        System.out.println("Body: " + body);
        return new Query(head, body);
    }

    private static <T> List<T> cloneList(List<T> list) {
        List<T> newList = new ArrayList<>();
        newList.addAll(list);
        return newList;
    }

    private static List<Atom> removeAtom(List<Atom> body, int atomPos) {
        List<Atom> newBody = cloneList(body);
        newBody.remove(atomPos);
        return newBody;
    }

    private static boolean buildHomo(List<Atom> body, List<Atom> newBody, RelationalAtom head) {
        return buildHomoHelper(cloneList(body), newBody, head, new ArrayList<>());
    }

    private static boolean buildHomoHelper(List<Atom> body, List<Atom> newBody, RelationalAtom head, List<HashMap<String, Term>> partialHomos ) {
        if (body.size() == 0) {
            return true;
        }
        for (int i = 0; i < body.size(); i++) {
            RelationalAtom atomToEliminate = (RelationalAtom) body.get(i);
            for (int j = 0; j < newBody.size(); j++) {
                RelationalAtom atomCandidateTransform = (RelationalAtom) newBody.get(j);
                // term-level checks
                if (!checkTermLevelHomo(atomToEliminate, atomCandidateTransform, head)) continue;
                // build partial homomorphism mapping
                HashMap<String, Term> homoMapping = buildTermLevelHomo(atomToEliminate, atomCandidateTransform, partialHomos);
                if (homoMapping == null) continue; // build homo failed due to one-to-many mapping

                List<Atom> tmpBody = removeAtom(body, i);
                List<HashMap<String, Term>> tmpPartialHomos = cloneList(partialHomos);
                tmpPartialHomos.add(homoMapping);
                if (buildHomoHelper(tmpBody, newBody, head, tmpPartialHomos)) return true;
            }
        }
        return false;
    }

    private static HashMap<String, Term> buildTermLevelHomo(RelationalAtom src, RelationalAtom dst, List<HashMap<String, Term>> partialHomos) {
        List<Term> srcTerms = src.getTerms();
        List<Term> dstTerms = dst.getTerms();

        HashMap<String, Term> localHomo = new HashMap<>();
        int i;
        for (i = 0; i < srcTerms.size(); i++) {
            String key = srcTerms.get(i).toString();
            String valueStr = dstTerms.get(i).toString();
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
            localHomo.put(key, dstTerms.get(i));
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

    /**
     * Example method for getting started with the parser.
     * Reads CQ from a file and prints it to screen, then extracts Head and Body
     * from the query and prints them to screen.
     */

    public static void parsingExample(String filename) {

        try {
            Query query = QueryParser.parse(Paths.get(filename));
//            Query query = QueryParser.parse("Q(x, y) :- R(x, z), S(y, z, w)");
//            Query query = QueryParser.parse("Q() :- R(x, 'z'), S(4, z, w)");

            System.out.println("Entire query: " + query);
            RelationalAtom head = query.getHead();
            System.out.println("Head: " + head);
            List<Atom> body = query.getBody();
            System.out.println("Body: " + body);
        }
        catch (Exception e)
        {
            System.err.println("Exception occurred during parsing");
            e.printStackTrace();
        }
    }

}
