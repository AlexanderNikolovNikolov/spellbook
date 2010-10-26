package bg.drow.spellbook.ui.swing.component;

import bg.drow.spellbook.core.SpellbookConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.net.URI;

/**
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 * @since 0.1
 */
public class SpellbookDefaultExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpellbookDefaultExceptionHandler.class);

    public SpellbookDefaultExceptionHandler() {
    }

    @Override
    public void uncaughtException(final Thread t, final Throwable e) {
        if (SwingUtilities.isEventDispatchThread()) {
            showException(t, e);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    showException(t, e);
                }
            });
        }
    }

    private void showException(Thread t, Throwable e) {
        String msg = String.format("Unexpected problem on thread %s: %s",
                t.getName(), e.getMessage());

        LOGGER.info(msg);

        logException(t, e);

        // note: in a real app, you should locate the currently focused frame
        // or dialog and use it as the parent. In this example, I'm just passing
        // a null owner, which means this dialog may get buried behind
        // some other screen.
        ErrorDialog errorDialog = new ErrorDialog(null, e);

        if (errorDialog.showDialog() == BaseDialog.RESULT_AFFIRMED) {
            try {
                Desktop.getDesktop().browse(new URI(SpellbookConstants.REPORT_ISSUE_URL));
            } catch (Exception e0) {
                LOGGER.error(e0.getMessage());
            }
        }
    }

    private void logException(Thread t, Throwable e) {
        e.printStackTrace();
    }
}
