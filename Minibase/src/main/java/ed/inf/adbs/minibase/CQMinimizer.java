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
    private static Query minimizeCq(Query query) {
        RelationalAtom head = query.getHead();
        List<Atom> body = query.getBody();

        boolean isRemoved = true;
        while (isRemoved) {
            isRemoved = false;
            for (int i = 0; i < body.size(); i++) {
                boolean canRemove = checkRemove(body, i, head);
                if (canRemove) {
                    System.out.println("Remove Atom : " + body + " at Position " + i);
                    body.remove(i);
                    isRemoved = true;
                    break;
                }
            }
        }
        System.out.println("Body: " + body);
        return query;
    }

    /**
     * Check whether an atom can be remove by finding another atom to transform to
     *
     * Firstly, find another atom and perform basic checks (name, length, constant -> variable)
     * Secondly, build a partial homo mapping between atom to remove and atom for transform
     * Thirdly, check the homo mapping on atoms except the one to remove
     *
     */
    private static boolean checkRemove(List<Atom> body, int atomPos, RelationalAtom head) {
        RelationalAtom aToRemove = (RelationalAtom) body.get(atomPos);

        // find another atom for the atom to remove to transform
        for (int i = 0; i < body.size(); i++) {
            if (i == atomPos) continue;
            RelationalAtom aForTransform = (RelationalAtom) body.get(i);
            // term-level checks
            if (!checkTermLevelHomo(aToRemove, aForTransform, head)) continue;
            // build partial homomorphism mapping
            HashMap<String, Term> homoMapping = buildPartialHomo(aToRemove, aForTransform);
            if (homoMapping == null) continue; // build homo failed due to one-to-many mapping
            // create a new body
            List<Atom> bodyDeletedOne = new ArrayList<>();
            bodyDeletedOne.addAll(body);
            bodyDeletedOne.remove(atomPos);
            // apply mapping to all atoms
            List<Atom> bodyDeletedMappedList = mapAtoms(homoMapping, bodyDeletedOne);
            // homo check - two check methods
            // method 1 - trivial check, all atoms are in the body after mapping
            List<String> bodyDeletedStrList = bodyDeletedOne.stream().map(Object::toString).collect(Collectors.toList());
            List<Integer> mismatchList = new ArrayList<>();
            for (int j = 0; j < bodyDeletedMappedList.size(); j++) {
                if (!bodyDeletedStrList.contains(bodyDeletedMappedList.get(j).toString())) {
                    mismatchList.add(j);
                }
            }
            if (mismatchList.size() == 0) return true;
            // method 2 - complex check, find a homo back to original body
        }

        return false;
    }

    private static List<Atom> mapAtoms(HashMap<String, Term> homoMapping, List<Atom> bodyDeletedOne) {
        List<Atom> bodyDeletedMappedList = new ArrayList<>();
        for (int j = 0; j < bodyDeletedOne.size(); j++) {
            RelationalAtom oldAtom = (RelationalAtom) bodyDeletedOne.get(j);
            List<Term> oldTermList = oldAtom.getTerms();
            List<Term> newTermList = new ArrayList<>();
            for (Term oldTerm: oldTermList) {
                newTermList.add(homoMapping.getOrDefault(oldTerm.toString(), oldTerm));
            }
            RelationalAtom newAtom = new RelationalAtom(oldAtom.getName(), newTermList);
            bodyDeletedMappedList.add(newAtom);
        }
        return bodyDeletedMappedList;
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
     * build a key-value pair for homo mapping between src and dst
     *
     * if the mapping can not be constructed (i.e. one-to-many mapping), null will be returned
     *
     * @param src an atom to be transform
     * @param dst an atom for src to transform to
     * @return a key-value pair, key is the original term, value is the term after mapping
     */
    private static HashMap<String, Term> buildPartialHomo(RelationalAtom src, RelationalAtom dst) {
        List<Term> srcTerms = src.getTerms();
        List<Term> dstTerms = dst.getTerms();

        HashMap<String, Term> homoMapping = new HashMap<>();
        int i;
        for (i = 0; i < srcTerms.size(); i++) {
            String key = srcTerms.get(i).toString();
            String valueStr = dstTerms.get(i).toString();
            if (homoMapping.containsKey(key)) {
                // no one-to-many mapping
                if (!homoMapping.get(key).toString().equals(valueStr)) return null;
            } else {
                homoMapping.put(key, dstTerms.get(i));
            }
        }
        return homoMapping;
    }

    /**
     * Check whether atoms except the one to remove, still exist in the CQ body after mapping
     *
     * @param body main body of cq with one atom removed
     * @param homoMapping key-value pair for homo mapping
     * @return a boolean showing the homo is valid
     */
    private static boolean checkPartialHomoSubset(List<Atom> body, HashMap<String, String> homoMapping) {
        List<String> atomStrList = body.stream().map(Object::toString).collect(Collectors.toList());
        for (int i = 0; i < body.size(); i++) {
            RelationalAtom atom = (RelationalAtom) body.get(i);
            List<String> termStrList = atom.getTermStrList();
            String atomStrMapped = atom.toString();
            // apply mapping
            for (String termStr: termStrList) {
                if (homoMapping.containsKey(termStr)) {
                    atomStrMapped = atomStrMapped.replace(termStr, homoMapping.get(termStr));
                }
            }
            // if the atom is not exists the CQ body, homo mapping is invalid
            if (!atomStrList.contains(atomStrMapped)) return false;
        }
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
