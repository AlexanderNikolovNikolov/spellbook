package bg.drow.spellbook.ui.desktop;

import bg.drow.spellbook.core.preferences.PreferencesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;

/**
 * Implements the integration layer with the host operating system clipboard.
 *
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 * @since 0.1
 */
public final class ClipboardIntegration implements ClipboardOwner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClipboardIntegration.class);

    private SpellbookFrame spellbookFrame;

    private static ClipboardIntegration instance;
    private Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    public static ClipboardIntegration getInstance(SpellbookFrame spellbookFrame) {
        if (instance == null) {
            instance = new ClipboardIntegration();
        }

        instance.spellbookFrame = spellbookFrame;

        return instance;
    }

    private ClipboardIntegration() {
        start();
    }

    public void start() {
        setClipboardContents(getClipboardContents());
    }

    /**
     * A nasty hack occurs here - Spellbook uses this method to get notified of sys clipboard
     * changes by grabbing its ownership and waiting to other apps to request it.
     */
    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        if (PreferencesManager.getInstance().getBoolean(PreferencesManager.Preference.CLIPBOARD_INTEGRATION, true)) {
            try {
                // this delay in necessary - otherwise all sort of nasty things happen
                Thread.sleep(200);
            } catch (Exception e) {
                System.out.println("Exception: " + e);
            }

            LOGGER.info("Clipboard ownership lost");
            // replace the contents in the clipboard with the same contents
            // just to restore the ownership to Spellbook
            clipboard.setContents(clipboard.getContents(this), this);

            // call the frame callback
            spellbookFrame.clipboardCallback();
        }
    }

    /**
     * Place a String on the clipboard, and make this class the
     * owner of the Clipboard's contents.
     *
     * @param text the string to place in the clipboard
     */
    public void setClipboardContents(String text) {
        StringSelection stringSelection = new StringSelection(text);
        clipboard.setContents(stringSelection, this);
    }

    /**
     * Get the String residing on the clipboard.
     *
     * @return any text found on the Clipboard; if none found, return an
     *         empty String.
     */
    public String getClipboardContents() {
        final DataFlavor stringFlavor = DataFlavor.stringFlavor;

        if (clipboard.isDataFlavorAvailable(stringFlavor)) {
            try {
                String text = (String) clipboard.getData(stringFlavor);

                // the string we receive maybe a file name we've copied so
                // we must ignore this
                if (text != null && !new File(text).exists()) {
                    return text;
                }
            } catch (UnsupportedFlavorException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }


        return "";
    }
}

