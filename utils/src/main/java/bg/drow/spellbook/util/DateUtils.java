package bg.drow.spellbook.util;

import java.util.Date;

/**
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 * @since 0.3
 */
public class DateUtils {
    private static final int MILIS_IN_A_SECOND = 1000;
    private static final int MILIS_IN_A_MIN = 60 * MILIS_IN_A_SECOND;
    private static final int MILIS_IN_A_HOUR = 60 * MILIS_IN_A_MIN;

    public static String dateDifference(Date from, Date to) {
        long milisDif = to.getTime() - from.getTime();

        int hours = (int) milisDif / MILIS_IN_A_HOUR;

        int mins = (int) (milisDif % MILIS_IN_A_HOUR) / MILIS_IN_A_MIN;

        int secs = (int) ((milisDif % MILIS_IN_A_HOUR) % MILIS_IN_A_MIN) / MILIS_IN_A_SECOND;

        return String.format("%02d:%02d:%02d", hours, mins, secs);
    }

    public static String getAvgDuration(Date from, Date to, int number) {
        long milisDif = to.getTime() - from.getTime();

        if (number < 1) {
            throw new IllegalArgumentException("number must be positive");
        }

        int hours = (int) milisDif / MILIS_IN_A_HOUR;

        int mins = (int) (milisDif % MILIS_IN_A_HOUR) / MILIS_IN_A_MIN;

        int secs = (int) ((milisDif % MILIS_IN_A_HOUR) % MILIS_IN_A_MIN) / MILIS_IN_A_SECOND;

        return String.format("%02d:%02d:%02d", hours / number, mins / number, secs / number);
    }
}
