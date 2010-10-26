package bg.drow.spellbook.ui.desktop;

import bg.drow.spellbook.core.SpellbookConstants;
import bg.drow.spellbook.core.i18n.Translator;
import bg.drow.spellbook.core.model.Language;
import bg.drow.spellbook.core.model.SupportedFileType;
import bg.drow.spellbook.core.preferences.PreferencesManager;
import bg.drow.spellbook.ui.swing.component.SelectDbDialog;
import bg.drow.spellbook.ui.swing.util.LafUtil;
import bg.drow.spellbook.util.ArchiveUtils;
import bg.drow.spellbook.core.model.Dictionary;
import bg.drow.spellbook.ui.swing.component.SpellbookDefaultExceptionHandler;
import com.jidesoft.dialog.StandardDialog;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.SplashScreen;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

/**
 * The entry point in Spellbook. Here the preferences manager gets initialized, some
 * important settings are taken into account(such as the initial look and feel and language)
 * and the application's main frame is created and displayed.
 *
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 * @since 0.1
 */
public class SpellbookApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpellbookApp.class);

    private static final Translator TRANSLATOR = Translator.getTranslator("SpellbookFrame");

    private static SpellbookFrame appFrame;
    private static boolean dbPresent = false;

    private static JWindow splashWindow;
    private static JProgressBar progressBar;
    private static boolean startup = true;

    public static void main(final String[] args) {
        //the application main method just delegates execution to the init() method
        init();
    }

    private static void createSplashWindow() {
        //get the JVM splash, if any
        final SplashScreen splashScreen = SplashScreen.getSplashScreen();
        splashWindow = new JWindow();

        final ImageIcon icon = new ImageIcon(SpellbookApp.class.getResource("/images/spellbook-splash.png"));

        Rectangle bounds;

        if (splashScreen != null) {
            bounds = splashScreen.getBounds();
            System.out.println(bounds);
        } else {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            bounds = new Rectangle(toolkit.getScreenSize().width / 2 - icon.getIconWidth() / 2,
                                   toolkit.getScreenSize().height / 2 - icon.getIconHeight() / 2,
                                   icon.getIconWidth(), icon.getIconHeight());
        }

        splashWindow.setBounds((int) bounds.getX(), (int) bounds.getY(), (int) bounds.getWidth(), (int) bounds.getHeight());
        JPanel panel = new JPanel(new MigLayout("wrap 1", "[][grow]", "[][grow]"));

        JLabel splashImage = new JLabel();

        splashImage.setIcon(icon);

        panel.add(splashImage);
        progressBar = new JProgressBar(0, 4);
        progressBar.setStringPainted(true);
        progressBar.setString(TRANSLATOR.translate("Starting(Message)"));
        panel.add(progressBar, "grow");

        splashWindow.setContentPane(panel);
        splashWindow.pack();
        splashWindow.setVisible(true);
    }

    public static void closeSplashWindow() {
        progressBar.setValue(progressBar.getMaximum());
        progressBar.setString(TRANSLATOR.translate("Done(Message)"));
        splashWindow.setVisible(false);
    }

    private static void increaseProgress(String message) {
        progressBar.setValue(progressBar.getValue() + 1);
        progressBar.setString(message);
    }

    public static void init() {
        // check for spellbook home dir presence and create if necessary
        final File userDir = new File(SpellbookConstants.SPELLBOOK_HOME);
        if (!userDir.exists()) {
            if (userDir.mkdir()) {
                LOGGER.info("Successfully create user dir: " + SpellbookConstants.SPELLBOOK_HOME);
            }
        }

        // install the default exception handler
        Thread.setDefaultUncaughtExceptionHandler(new SpellbookDefaultExceptionHandler());

        PreferencesManager.init(SpellbookApp.class);

        final PreferencesManager pm = PreferencesManager.getInstance();

        if (pm.get(PreferencesManager.Preference.UI_LANG, Language.ENGLISH.getName()).equals(Language.BULGARIAN.getName())) {
            Locale.setDefault(new Locale("bg", "BG"));
            LOGGER.info("Selected locate is " + Locale.getDefault());
        } else {
            Locale.setDefault(Locale.ENGLISH);
        }

        Language.TRANSLATOR.reset();
        Dictionary.TRANSLATOR.reset();
        SupportedFileType.TRANSLATOR.reset();
        TRANSLATOR.reset();

        try {
            String selectedLookAndFeel = pm.get(PreferencesManager.Preference.LOOK_AND_FEEL, "System");

            if (selectedLookAndFeel.equals("System")) {
                // Set System L&F
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } else {
                List<UIManager.LookAndFeelInfo> lookAndFeelInfos = LafUtil.getAvailableLookAndFeels();

                for (UIManager.LookAndFeelInfo lookAndFeelInfo : lookAndFeelInfos) {
                    if (lookAndFeelInfo.getName().equals(selectedLookAndFeel)) {
                        UIManager.setLookAndFeel(lookAndFeelInfo.getClassName());
                    }
                }
            }
        } catch (UnsupportedLookAndFeelException e) {
            // handle exception
        } catch (ClassNotFoundException e) {
            // handle exception
        } catch (InstantiationException e) {
            // handle exception
        } catch (IllegalAccessException e) {
            // handle exception
        }

        // don't show splash on restart and don't create/check lock
        if (startup) {
            try {
                // we determine whether another instance of spellbook is running by trying to obtain a lock
                // on a specifically designated for the purpose file. If we cannot obtain the lock we assume that
                // another instance of the application has already done that
                RandomAccessFile lockFile = new RandomAccessFile(SpellbookConstants.SPELLBOOK_HOME + File.separator + "spellbook.lock", "rw");

                FileChannel channel = lockFile.getChannel();

                if (channel.tryLock() == null) {
                    JOptionPane.showMessageDialog(null, TRANSLATOR.translate("AlreadyRunning(Message)"),
                                                  TRANSLATOR.translate("Warning(Title)"), JOptionPane.WARNING_MESSAGE);
                    System.exit(0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            createSplashWindow();
        }

        increaseProgress(TRANSLATOR.translate("VerifyingDb(Message)"));

        // check the presence of the dictionary database
        if (!verifyDbPresence()) {
            splashWindow.setVisible(false);
            JOptionPane.showMessageDialog(null, TRANSLATOR.translate("NoDbSelected(Message)"),
                    TRANSLATOR.translate("Error(Title)"), JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                appFrame = new SpellbookFrame(dbPresent);

                appFrame.init();

                closeSplashWindow();

                if (!pm.getBoolean(PreferencesManager.Preference.START_IN_TRAY, false)) {
                    appFrame.setVisible(true);

                    // these actions should only happen on startup, on restart we ignore them
                    if (startup) {
                        if (pm.getBoolean(PreferencesManager.Preference.CHECK_JAVA_VERSION, true)) {
                            checkJavaRuntime(appFrame);
                        }

                        if (pm.getBoolean(PreferencesManager.Preference.CHECK_FOR_UPDATES, true)) {
                            // check for the new version is a separate thread to avoid startup delay
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    appFrame.checkForUpdates(true);
                                }
                            }).start();
                        }

                        appFrame.showWordOfTheDay();
                    }
                }

                // next time we enter this method it will be a restart
                startup = false;
            }
        });
    }

    private static void checkJavaRuntime(JFrame frame) {
        String vmName = System.getProperty("java.vm.name");

        if (!vmName.startsWith("Java HotSpot")) {
            JOptionPane.showMessageDialog(frame, TRANSLATOR.translate("JavaRuntimeWarning(Message)", vmName),
                    TRANSLATOR.translate("Warning(Title)"), JOptionPane.WARNING_MESSAGE);
        }
    }

    private static boolean verifyDbPresence() {
        File file = new File(SpellbookConstants.SPELLBOOK_DB_PATH);

        if (!file.exists() || file.isDirectory()) {
            final File archiveFile = new File(SpellbookConstants.SPELLBOOK_HOME + File.separator + "spellbook-db-0.4.tar.bz2");

            final SelectDbDialog selectDbDialog = new SelectDbDialog();

            if (!archiveFile.exists()) {
                // hide the splash temporarily
                splashWindow.setVisible(false);
            }

            if (archiveFile.exists() || (selectDbDialog.showDialog() == StandardDialog.RESULT_AFFIRMED)) {
                splashWindow.setVisible(true);
                increaseProgress(TRANSLATOR.translate("ExtractingDb(Message)"));
                ArchiveUtils.extractDbFromArchive(archiveFile.exists() ? archiveFile.getAbsolutePath() : selectDbDialog.getDbPath());
                increaseProgress(TRANSLATOR.translate("LoadingDb(Message)"));
            } else {
                return false;
            }
        } else {
            increaseProgress(TRANSLATOR.translate("LoadingDb(Message)"));

            dbPresent = true;
        }

        return true;
    }
}
