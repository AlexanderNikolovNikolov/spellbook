package bg.drow.spellbook.util;

/**
 * A simple helper class containing some common argument validation logic.
 *
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 * @since 0.4
 */
public class ValidationUtil {
    public static void nonNull(Object argument, String message) {
        if (argument == null) {
            throw new NullPointerException(message);
        }
    }

    public static void nonNull(Object argument) {
        nonNull(argument, "Null argument!");
    }
}
