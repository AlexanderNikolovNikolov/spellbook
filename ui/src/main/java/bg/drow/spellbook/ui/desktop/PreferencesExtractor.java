package bg.drow.spellbook.ui.desktop;

import bg.drow.spellbook.core.i18n.Translator;
import bg.drow.spellbook.core.model.Language;
import bg.drow.spellbook.core.preferences.PreferencesManager;
import bg.drow.spellbook.ui.swing.component.BaseDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.Font;
import java.util.logging.Level;

/**
 * A helper class that extracts the preferences from the preferences dialog.
 *
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 * @since 0.3
 */
public class PreferencesExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpellbookFrame.class);
    private static final Translator TRANSLATOR = Translator.getTranslator("SpellbookFrame");
    private static final PreferencesManager PM = PreferencesManager.getInstance();

    public static void extract(SpellbookFrame spellbookFrame, PreferencesDialog preferencesDialog) {
        if (preferencesDialog.showDialog() == BaseDialog.RESULT_AFFIRMED) {
            extractFontPreferences(spellbookFrame, preferencesDialog);
            extractExamPreferences(preferencesDialog);
            extractStudyWordsPreferences(preferencesDialog);
            // general settings should be last since they may require restart to take effect
            extractGeneralPreferences(spellbookFrame, preferencesDialog);
        } else {
            // we need to restore the old look and feel manually since it was changed on selection
            String currentLookAndFeel = UIManager.getLookAndFeel().getName();
            String currentLookAndFeelClassName = UIManager.getLookAndFeel().getClass().getName();

            String selectedLookAndFeel = PM.get(PreferencesManager.Preference.LOOK_AND_FEEL, "System");

            if (currentLookAndFeel.equalsIgnoreCase(selectedLookAndFeel)
                    || (selectedLookAndFeel.equals("System")
                    && currentLookAndFeelClassName.equals(UIManager.getSystemLookAndFeelClassName()))) {
                LOGGER.info("Look and feel is the same, no need to reset");
                return;
            }

            LookAndFeelInfo[] lookAndFeelInfos = UIManager.getInstalledLookAndFeels();

            if (selectedLookAndFeel.equals("System")) {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException ex) {
                    java.util.logging.Logger.getLogger(SpellbookFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InstantiationException ex) {
                    java.util.logging.Logger.getLogger(SpellbookFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    java.util.logging.Logger.getLogger(SpellbookFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (UnsupportedLookAndFeelException ex) {
                    java.util.logging.Logger.getLogger(SpellbookFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                for (LookAndFeelInfo lookAndFeelInfo : lookAndFeelInfos) {
                    if (lookAndFeelInfo.getName().equals(selectedLookAndFeel)) {
                        try {
                            UIManager.setLookAndFeel(lookAndFeelInfo.getClassName());
                        } catch (ClassNotFoundException e1) {
                            e1.printStackTrace();
                        } catch (InstantiationException e1) {
                            e1.printStackTrace();
                        } catch (IllegalAccessException e1) {
                            e1.printStackTrace();
                        } catch (UnsupportedLookAndFeelException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }

            SwingUtilities.updateComponentTreeUI(spellbookFrame);
        }
    }

    private static void extractExamPreferences(final PreferencesDialog preferencesDialog) {
        // exam prefs
        PM.put(PreferencesManager.Preference.EXAM_DIFFICULTY, preferencesDialog.getExamDifficulty().name());
        PM.putInt(PreferencesManager.Preference.EXAM_WORDS, preferencesDialog.getExamWords());
        PM.putBoolean(PreferencesManager.Preference.EXAM_TIMER, preferencesDialog.isExamTimerEnabled());
    }

    private static void extractStudyWordsPreferences(final PreferencesDialog preferencesDialog) {
        //study prefs
        PM.putBoolean(PreferencesManager.Preference.REPEAT_MISSPELLED_WORDS, preferencesDialog.isRepeatMisspelledWordsEnabled());
        PM.putBoolean(PreferencesManager.Preference.REPEAT_WORDS, preferencesDialog.isRepeatWordEnabled());
        PM.putBoolean(PreferencesManager.Preference.LEARNING_IN_ORDER, preferencesDialog.isOrderOfInputRadioButtonEnabled());
        PM.putBoolean(PreferencesManager.Preference.LEARNING_IN_REVERSE_ORDER, preferencesDialog.isReverseOrderOfInputEnabled());
        PM.putBoolean(PreferencesManager.Preference.LEARNING_RANDOM, preferencesDialog.isRandomEnabled());
    }

    private static void extractGeneralPreferences(final SpellbookFrame spellbookFrame, final PreferencesDialog preferencesDialog) {
        LOGGER.info("Default dictionary is " + preferencesDialog.getDefaultDictionary());
        PM.put(PreferencesManager.Preference.DEFAULT_DICTIONARY, preferencesDialog.getDefaultDictionary());

        final boolean minimizeToTrayEnabled = preferencesDialog.isMinimizeToTrayEnabled();

        if (minimizeToTrayEnabled) {
            LOGGER.info("Minimize to tray is enabled");
        } else {
            LOGGER.info("Minimize to tray is disabled");
        }

        PM.putBoolean(PreferencesManager.Preference.MIN_TO_TRAY, minimizeToTrayEnabled);

        boolean minimizeToTrayOnCloseEnabled = preferencesDialog.isMinimizeToTrayOnCloseEnabled();

        if (minimizeToTrayOnCloseEnabled) {
            LOGGER.info("Minimize to tray on close is enabled");
            spellbookFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        } else {
            LOGGER.info("Minimize to tray on close is disabled");
            spellbookFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }

        PM.putBoolean(PreferencesManager.Preference.CLOSE_TO_TRAY, minimizeToTrayOnCloseEnabled);

        final boolean clipboardIntegrationEnabled = preferencesDialog.isClipboardIntegrationEnabled();

        if (clipboardIntegrationEnabled) {
            LOGGER.info("Clipboard integration is enabled");
            ClipboardIntegration.getInstance(spellbookFrame).start();
        } else {
            LOGGER.info("Clipboard integration is disabled");
        }

        PM.putBoolean(PreferencesManager.Preference.CLIPBOARD_INTEGRATION, clipboardIntegrationEnabled);

        final boolean trayPopupEnabled = preferencesDialog.isTrayPopupEnabled();

        if (trayPopupEnabled) {
            LOGGER.info("Tray popup is enabled");
        } else {
            LOGGER.info("Tray popup is disabled");
        }

        PM.putBoolean(PreferencesManager.Preference.TRAY_POPUP, trayPopupEnabled);

        final boolean showMemoryUsageEnabled = preferencesDialog.isShowMemoryUsageEnabled();

        if (showMemoryUsageEnabled) {
            LOGGER.info("Show memory usage is enabled");
            spellbookFrame.showMemoryUsage();
        } else {
            LOGGER.info("Show memory usage is disabled");
            spellbookFrame.hideMemoryUsage();
        }

        PM.putBoolean(PreferencesManager.Preference.SHOW_MEMORY_USAGE, showMemoryUsageEnabled);

        final boolean alwaysOnTopEnabled = preferencesDialog.isAlwaysOnTopEnabled();

        if (alwaysOnTopEnabled) {
            LOGGER.info("Always on top enabled");
            spellbookFrame.setAlwaysOnTop(true);
        } else {
            LOGGER.info("Always on top disabled");
            spellbookFrame.setAlwaysOnTop(false);
        }

        PM.putBoolean(PreferencesManager.Preference.ALWAYS_ON_TOP, alwaysOnTopEnabled);

        String selectedLookAndFeel = preferencesDialog.getSelectedLookAndFeel();

        if (!selectedLookAndFeel.equals(PM.get(PreferencesManager.Preference.LOOK_AND_FEEL, "System"))) {
            PM.put(PreferencesManager.Preference.LOOK_AND_FEEL, selectedLookAndFeel);
        }

        final boolean emptyLineSelected = preferencesDialog.isEmptyLineEnabled();

        if (emptyLineSelected) {
            LOGGER.info("Empty line after each meaning enabled");
        } else {
            LOGGER.info("Empty line after each meaning disabled");
        }

        PM.putBoolean(PreferencesManager.Preference.EMPTY_LINE, emptyLineSelected);

        final boolean startMinimized = preferencesDialog.isStartMinimizedEnabled();

        if (startMinimized) {
            LOGGER.info("Start minimized is enabled");
        } else {
            LOGGER.info("Start minimized is disabled");
        }

        PM.putBoolean(PreferencesManager.Preference.START_IN_TRAY, startMinimized);

        final boolean showWordOfTheDay = preferencesDialog.showWordOfTheDay();

        if (showWordOfTheDay) {
            LOGGER.info("Word of the day is enabled");
        } else {
            LOGGER.info("Word of the day is disabled");
        }

        PM.putBoolean(PreferencesManager.Preference.WORD_OF_THE_DAY, showWordOfTheDay);

        final boolean checkForUpdates = preferencesDialog.isCheckForUpdatesEnabled();

        if (checkForUpdates) {
            LOGGER.info("Check for updates is enabled");
        } else {
            LOGGER.info("Check for updates is disabled");
        }

        PM.putBoolean(PreferencesManager.Preference.CHECK_FOR_UPDATES, checkForUpdates);

        final boolean checkJavaVersion = preferencesDialog.isCheckJavaVersionEnabled();

        if (checkJavaVersion) {
            LOGGER.info("Check Java version is enabled");
        } else {
            LOGGER.info("Check Java version is disabled");
        }

        PM.putBoolean(PreferencesManager.Preference.CHECK_JAVA_VERSION, checkJavaVersion);

        // language settings should be changed last because they may require restart
        String oldLanguage = PM.get(PreferencesManager.Preference.UI_LANG, Language.ENGLISH.getName());
        final String newLanguage = preferencesDialog.getSelectedLanguage().getName();
        PM.put(PreferencesManager.Preference.UI_LANG, newLanguage);

        if (!oldLanguage.equals(newLanguage)) {
            LOGGER.info("Language changed from " + oldLanguage + " to " + newLanguage);
            int selectedOption = JOptionPane.showConfirmDialog(spellbookFrame, TRANSLATOR.translate("Restart(Message)"), "Restart",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

            if (selectedOption == JOptionPane.OK_OPTION) {
                spellbookFrame.restart();
            }
        }
    }

    private static void extractFontPreferences(final SpellbookFrame spellbookFrame, final PreferencesDialog preferencesDialog) {
        // set the font
        final Font selectedFont = preferencesDialog.generateFont();

        PM.put(PreferencesManager.Preference.FONT_NAME, selectedFont.getName());
        PM.putInt(PreferencesManager.Preference.FONT_SIZE, selectedFont.getSize());
        PM.putInt(PreferencesManager.Preference.FONT_STYLE, selectedFont.getStyle());

        spellbookFrame.setSelectedFont(selectedFont);
    }
}
