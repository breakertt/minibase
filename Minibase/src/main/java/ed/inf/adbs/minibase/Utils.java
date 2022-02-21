package ed.inf.adbs.minibase;

import ed.inf.adbs.minibase.base.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {

    public static String join(Collection<?> c, String delimiter) {
        return c.stream()
                .map(x -> x.toString())
                .collect(Collectors.joining(delimiter));
    }

}
