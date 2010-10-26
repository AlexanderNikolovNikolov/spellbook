package bg.drow.spellbook.core.preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 */
public class PreferencesManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PreferencesManager.class);

    private static PreferencesManager instance;

    private Preferences preferences;

    private PreferencesManager(Class<?> mainClass) {
        preferences = Preferences.userNodeForPackage(mainClass);
    }

    public static void init(Class<?> mainClass) {
        if (instance == null) {
            instance = new PreferencesManager(mainClass);
        } else {
            LOGGER.info("Preferences manager is already initialized");
        }
    }

    public static PreferencesManager getInstance() {
        return instance;
    }

    public String get(Preference key, String def) {
        return preferences.get(key.toString(), def);
    }

    public void put(Preference key, String value) {
        preferences.put(key.toString(), value);
    }

    public int getInt(Preference key, int def) {
        return preferences.getInt(key.toString(), def);
    }

    public void putInt(Preference key, int value) {
        preferences.putInt(key.toString(), value);
    }

    public boolean getBoolean(Preference key, boolean def) {
        return preferences.getBoolean(key.toString(), def);
    }

    public void putBoolean(Preference key, boolean value) {
        preferences.putBoolean(key.toString(), value);
    }

    public void putDouble(Preference key, double value) {
        preferences.putDouble(key.toString(), value);
    }

    public double getDouble(Preference key, double def) {
        return preferences.getDouble(key.toString(), def);
    }

    public void clear() throws BackingStoreException {
        preferences.clear();
    }

    public enum Preference {
        MIN_TO_TRAY,
        CLOSE_TO_TRAY,
        TRAY_POPUP,
        CLIPBOARD_INTEGRATION,
        SHOW_MEMORY_USAGE,
        UI_LANG,
        DEFAULT_DICTIONARY,
        ALWAYS_ON_TOP,
        LOOK_AND_FEEL,
        START_IN_TRAY,
        WORD_OF_THE_DAY,
        CHECK_FOR_UPDATES,
        FONT_NAME,
        FONT_SIZE,
        FONT_STYLE,
        EMPTY_LINE,
        EXAM_WORDS,
        EXAM_DIFFICULTY,
        EXAM_TIMER,
        FRAME_X,
        FRAME_Y,
        FRAME_HEIGHT,
        FRAME_WIDTH,
        DIVIDER_LOCATION,
        DICTIONARIES,
        STUDY_SETS,
        LEARNING_IN_ORDER,
        LEARNING_IN_REVERSE_ORDER,
        LEARNING_RANDOM,
        REPEAT_MISSPELLED_WORDS,
        REPEAT_WORDS,
        CHECK_JAVA_VERSION,
        SHOW_TOOLBAR,
        SHOW_STATUSBAR
    }
}
