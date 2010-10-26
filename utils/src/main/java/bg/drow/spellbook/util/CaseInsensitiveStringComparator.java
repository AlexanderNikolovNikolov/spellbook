package bg.drow.spellbook.util;

import java.util.Comparator;

/**
 *
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 */
public class CaseInsensitiveStringComparator implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {
        return o1.compareToIgnoreCase(o2);
    }
}
