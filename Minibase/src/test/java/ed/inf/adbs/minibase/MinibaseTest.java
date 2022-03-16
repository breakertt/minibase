package ed.inf.adbs.minibase;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for Minibase.
 */

public class MinibaseTest {

//    @Test
//    public void minibaseTempDebugTest() {
//        Minibase.evaluateCQ("data/evaluation/db", "data/evaluation/input/query7.txt", System.out);
//    }

    @Test
    public void singleRelationAtomTest() throws IOException {
        String databaseDir = "data/evaluation/db";
        String inputDir = "data/evaluation/input";
        String expectedDir = "data/evaluation/expected_output";
        String actualDir = "data/evaluation/output";

        for (int i = 0; i < 5; i++) {
            String queryFilename = "query" + i + ".txt";
            String outputFilename = "query" + i + ".csv";

            System.out.println(queryFilename);

            File inputFile = new File(inputDir, queryFilename);
            File expectedFile = new File(expectedDir, outputFilename);
            File acutalFile = new File(actualDir, outputFilename);

            if(!expectedFile.exists()) continue;
            Minibase.main(new String[]{databaseDir, inputFile.getPath(), acutalFile.getPath()});

            String expectedRes = new String(Files.readAllBytes(Paths.get(expectedFile.getPath())), StandardCharsets.UTF_8);
            String actualRes = new String(Files.readAllBytes(Paths.get(acutalFile.getPath())), StandardCharsets.UTF_8);

            assertEquals(expectedRes.trim(), actualRes.trim());
        }
    }

    @Test
    public void multipleRelationAtomNoAggTest() throws IOException {
        String databaseDir = "data/evaluation/db";
        String inputDir = "data/evaluation/input";
        String expectedDir = "data/evaluation/expected_output";
        String actualDir = "data/evaluation/output";

        for (int i = 5; i < 7; i++) {
            String queryFilename = "query" + i + ".txt";
            String outputFilename = "query" + i + ".csv";

            System.out.println(queryFilename);

            File inputFile = new File(inputDir, queryFilename);
            File expectedFile = new File(expectedDir, outputFilename);
            File acutalFile = new File(actualDir, outputFilename);

            if(!expectedFile.exists()) continue;
            Minibase.main(new String[]{databaseDir, inputFile.getPath(), acutalFile.getPath()});

            String expectedRes = new String(Files.readAllBytes(Paths.get(expectedFile.getPath())), StandardCharsets.UTF_8);
            String actualRes = new String(Files.readAllBytes(Paths.get(acutalFile.getPath())), StandardCharsets.UTF_8);

            assertEquals(expectedRes.trim(), actualRes.trim());
        }
    }

    @Test
    public void multipleRelationAtomAggTest() throws IOException {
        String databaseDir = "data/evaluation/db";
        String inputDir = "data/evaluation/input";
        String expectedDir = "data/evaluation/expected_output";
        String actualDir = "data/evaluation/output";

        for (int i = 7; i < 10; i++) {
            String queryFilename = "query" + i + ".txt";
            String outputFilename = "query" + i + ".csv";

            System.out.println(queryFilename);

            File inputFile = new File(inputDir, queryFilename);
            File expectedFile = new File(expectedDir, outputFilename);
            File acutalFile = new File(actualDir, outputFilename);

            if(!expectedFile.exists()) continue;
            Minibase.main(new String[]{databaseDir, inputFile.getPath(), acutalFile.getPath()});

            String expectedRes = new String(Files.readAllBytes(Paths.get(expectedFile.getPath())), StandardCharsets.UTF_8);
            String actualRes = new String(Files.readAllBytes(Paths.get(acutalFile.getPath())), StandardCharsets.UTF_8);

            assertEquals(expectedRes.trim(), actualRes.trim());
        }
    }
}

