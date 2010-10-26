package bg.drow.spellbook.ui.swing.util;

import bg.drow.spellbook.core.model.Language;
import bg.drow.spellbook.core.preferences.PreferencesManager;
import net.java.balloontip.BalloonTip;
import net.java.balloontip.utils.TimingUtils;

import javax.swing.JComponent;

/**
 * Utility class for recurring Swing related tasks.
 *
 * @author <a mailto="bozhidar@drow.bg">Bozhidar Batsov</a>
 * @since 0.3
 */
public class SwingUtil {

    private static final PreferencesManager PM = PreferencesManager.getInstance();
    private static final int DEFAULT_BALLOONTIP_DISPLAY_TIME = 5000;
    private static final int FONT_SIZE = 11;

    public static void showBalloonTip(JComponent component, String text) {
        showBalloonTip(component, text, DEFAULT_BALLOONTIP_DISPLAY_TIME);
    }

    public static void showBalloonTip(JComponent component, String text, int displayTime) {
        BalloonTip balloonTip = new BalloonTip(component, text);
        balloonTip.setIcon(IconManager.getImageIcon("about.png", IconManager.IconSize.SIZE16));
        TimingUtils.showTimedBalloon(balloonTip, displayTime);

    }

    public static String formatTranslation(String word, String translation) {
        StringBuilder result = new StringBuilder();

        result.append("<html><head></head><body>");

        result.append("<b>").append(word).append("</b>");

        result.append("<p>").append(formatTranslation(translation)).append("</p>");

        result.append("</body></html>");

        return result.toString();
    }

    private static String formatTranslation(String translation) {
        StringBuffer result = new StringBuffer();

        String textFormatting;
        if (PM.getBoolean(PreferencesManager.Preference.EMPTY_LINE, true)) {
            textFormatting = "<br/>";
        } else {
            textFormatting = "";
        }

        String[] lines = translation.split("\n");

        for (String line : lines) {
            // some special handling for transcripts
            if (line.startsWith("[") && line.endsWith("]")) {
                result.append("<span style=\"color:blue\">" + line + "</span>");
            } else {
                result.append(line + textFormatting);
            }

            if (line.startsWith("[") && line.endsWith("]")) {
                result.append("<br/><br/>");
            } else {
                result.append("<br/>");
            }
        }

        return result.toString();
    }

    public static String languageToLowerCase(Language language) {
        return language.toString().substring(0, 1) + language.toString().substring(1).toLowerCase();
    }
}
