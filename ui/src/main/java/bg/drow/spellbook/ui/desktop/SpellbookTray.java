package bg.drow.spellbook.ui.desktop;

import bg.drow.spellbook.core.i18n.Translator;
import bg.drow.spellbook.ui.swing.util.IconManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JFrame;
import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A thin layer of abstraction on top the application's tray icon. Here is
 * the handling of all events associated with it.
 *
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 * @since 0.2
 */
public class SpellbookTray {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpellbookTray.class);
    private static final Translator TRANSLATOR = Translator.getTranslator("SpellbookTray");

    private static TrayIcon trayIcon;

    public static TrayIcon createTraySection(final SpellbookFrame appFrame) {
        TRANSLATOR.reset();

        //Check the SystemTray support
        if (!SystemTray.isSupported()) {
            LOGGER.info("SystemTray is not supported");
            return null;
        }

        final PopupMenu popup = new PopupMenu();
        trayIcon =
                new TrayIcon(IconManager.getImageIcon("dictionary.png", IconManager.IconSize.SIZE48).getImage());
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip("Spellbook Dictionary");

        final SystemTray tray = SystemTray.getSystemTray();

        // Create a popup menu components
        MenuItem aboutItem = new MenuItem(TRANSLATOR.translate("About(MenuItem)"));
        MenuItem exitItem = new MenuItem(TRANSLATOR.translate("Exit(MenuItem)"));

        //Add components to popup menu
        popup.add(aboutItem);
        popup.addSeparator();
        popup.add(exitItem);

        trayIcon.setPopupMenu(popup);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            LOGGER.error("TrayIcon could not be added.");
            return null;
        }

        // clicking the tray icon message generates a action event we need to handle
        trayIcon.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                LOGGER.info("Tray balloon message clicked");

                if (appFrame.getState() == JFrame.ICONIFIED) {
                    LOGGER.info("App is iconified");
                    appFrame.setState(JFrame.NORMAL);
                    appFrame.toFront();
                }

                if (!appFrame.isVisible()) {
                    appFrame.setVisible(true);
                    appFrame.toFront();
                }
            }
        });

        trayIcon.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                LOGGER.info("Tray icon clicked");
                if (appFrame.isInitialized()) {

                    if (appFrame.getState() == JFrame.ICONIFIED) {
                        LOGGER.info("App is iconified");
                        appFrame.setState(JFrame.NORMAL);
                    }

                    appFrame.setVisible(!appFrame.isVisible());

                    if (appFrame.isVisible()) {
                        appFrame.toFront();
                    }
                } else {
                    LOGGER.info("Initialization is not yet complete - ignoring...");
                }
            }
        });

        aboutItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                LOGGER.info("About dialog opened from tray");
                if (appFrame.isInitialized()) {
                    AboutDialog aboutDialog = new AboutDialog(appFrame, true);
                    aboutDialog.setLocationRelativeTo(appFrame.isVisible() ? appFrame : null);
                    aboutDialog.setVisible(true);
                } else {
                    LOGGER.info("Initialization is not yet complete - ignoring...");
                }
            }
        });

        exitItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                LOGGER.info("Exit from tray");
                tray.remove(trayIcon);
                System.exit(0);
            }
        });

        return trayIcon;
    }

    public static void destroyTrayIcon() {
        if (SystemTray.isSupported()) {
            final SystemTray tray = SystemTray.getSystemTray();

            tray.remove(trayIcon);
        }
    }
}
