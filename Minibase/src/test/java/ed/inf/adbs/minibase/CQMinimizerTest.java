package ed.inf.adbs.minibase;

import ed.inf.adbs.minibase.base.Query;
import ed.inf.adbs.minibase.parser.QueryParser;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CQMinimizerTest {

    @Test
    public void minimizerTest() throws IOException {
        String basePath = "data/minimization/";
        String input = "input/";
        String expectedFolder = "expected_output/";
        String actualFolder = "output/";

        File inputFolder = new File(basePath + input);
        File[] inputFiles = inputFolder.listFiles();
        for (File inputFile: inputFiles) {
            String queryFilename = inputFile.getName();
            System.out.println(queryFilename);

            File expectedFile = new File(basePath + expectedFolder + queryFilename);
            File calculatedFile = new File(basePath + actualFolder + queryFilename);
            if(!expectedFile.exists()) continue;

            CQMinimizer.main(new String[]{inputFile.getPath(), calculatedFile.getPath()});

            Query expectedQuery = QueryParser.parse(Paths.get(expectedFile.getPath()));
            Query calculatedQuery = QueryParser.parse(Paths.get(calculatedFile.getPath()));

            assertEquals(expectedQuery.getHead().toString(), calculatedQuery.getHead().toString());
            List<String> expectedQueryBodyStrList = expectedQuery.getBody().
                    stream().map(Object::toString).collect(Collectors.toList());
            List<String> calculatedQueryBodyStrList = calculatedQuery.getBody().
                    stream().map(Object::toString).collect(Collectors.toList());
            Collections.sort(expectedQueryBodyStrList);
            Collections.sort(calculatedQueryBodyStrList);

            assertEquals(expectedQueryBodyStrList, calculatedQueryBodyStrList);
        }
    }

    @Test
    public void minimizerTestTemp() throws IOException {
        Query query = QueryParser.parse("Q(x, y) :- R(y, x, z), R(w, v, z), T(x, z), T(x, a), R(y, x, a)");
        System.out.println(CQMinimizer.minimizeCq(query));
    }
}
