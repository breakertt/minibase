package ed.inf.adbs.minibase;

import java.io.*;

/**
 * In-memory database system
 *
 */
public class Minibase {

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: Minibase database_dir input_file output_file");
            return;
        }

        String databaseDir = args[0];
        String inputFile = args[1];
        String outputFile = args[2];

        evaluateCQ(databaseDir, inputFile, outputFile);
    }

    /**
     * Evaluate a CQ with the output as a file
     * @param databaseDir the directory with database files
     * @param inputFile the input file containing query
     * @param outputFile the file where found records are written to
     */
    public static void evaluateCQ(String databaseDir, String inputFile, String outputFile) {
        try {
            PrintStream filePrintStream = new PrintStream(new FileOutputStream(outputFile));
            evaluateCQ(databaseDir, inputFile, filePrintStream);
            filePrintStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Evaluate a CQ with the output as a PrintStream
     * @param databaseDir the directory with database files
     * @param inputFile the input file containing query
     * @param printStream a stream where found records are printed to
     */
    public static void evaluateCQ(String databaseDir, String inputFile, PrintStream printStream) {
        try {
            ed.inf.adbs.minibase.Interpreter interpreter = new ed.inf.adbs.minibase.Interpreter(databaseDir, inputFile);
            interpreter.dump(printStream);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
