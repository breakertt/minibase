package ed.inf.adbs.minibase;

import ed.inf.adbs.minibase.base.Atom;
import ed.inf.adbs.minibase.base.Query;
import ed.inf.adbs.minibase.base.RelationalAtom;
import ed.inf.adbs.minibase.parser.QueryParser;

import java.io.IOException;
import java.nio.file.Paths;
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

//        parsingExample(inputFile);
    }

    /**
     * Pipeline for CQMinimizer
     *
     * Parse input, minimize query, then output minimized query
     *
     */
    public static void cqMinimizerPipe(String inputFile, String outputFile) {
        // parse query string
        Query query = null;
        try {
            query = QueryParser.parse(Paths.get(inputFile));
        }
        catch (IOException e)
        {
            System.err.println("Exception occurred during parsing");
            e.printStackTrace();
        }

        // get minimized query
        Query minimizedQuery = minimizeCq(query);

        // write minimized query to outputFile

    }

    /**
     * CQ minimization procedure
     *
     * Assume the body of the query from inputFile has no comparison atoms
     * but could potentially have constants in its relational atoms.
     *
     */
    public static Query minimizeCq(Query query) {
        RelationalAtom head = query.getHead();
        List<Atom> body = query.getBody();

        boolean isRemoved = true;
        while (isRemoved) {
            isRemoved = false;
            System.out.println("Body: " + body);
            for (int i = 0; i < body.size(); i++) {
                boolean canRemove = checkRemove(body, i);
                if (canRemove) {
                    body.remove(i);
                    isRemoved = true;
                    break;
                }
            }
        }

        return query;
    }

    private static boolean checkRemove(List<Atom> body, int i) {
        Atom aToRemove = body.get(i);
        // Find another atom from the atom to remove to transform
        // build a partial homomorphism
        // check the partial homomorphism - 1. no output variable should be mapped 2. the mapping result of other atoms
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
