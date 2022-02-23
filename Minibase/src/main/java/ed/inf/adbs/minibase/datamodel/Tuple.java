package ed.inf.adbs.minibase.datamodel;

import java.util.List;

public class Tuple {
    List<Item> items;

    class Item {
        boolean type = false; // 0 for int, 1 for string

        int i;
        String s;

        Item(int i, String s, boolean type) {
            if (!type) {
                this.i = i;
            } else {
                this.s = s;
            }
        }
    }
}
