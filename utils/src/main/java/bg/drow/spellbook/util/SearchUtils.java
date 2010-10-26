package bg.drow.spellbook.util;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 */
public class SearchUtils {
    public static int findInsertionIndex(List<String> list, String elem) {
        return Math.abs(Collections.binarySearch(list, elem, new CaseInsensitiveStringComparator())) - 1;
    }
}
