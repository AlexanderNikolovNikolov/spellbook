package bg.drow.spellbook.ui.swing.component;

import bg.drow.spellbook.core.SpellbookConstants;
import bg.drow.spellbook.core.i18n.Translator;
import bg.drow.spellbook.ui.swing.util.IconManager;
import com.jidesoft.dialog.BannerPanel;
import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.StandardDialog;
import com.jidesoft.icons.JideIconsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/**
 * Base dialog class in Spellbook.
 *
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 * @since 0.4
 */
public abstract class BaseDialog extends StandardDialog {
    private static final int FONT_SIZE = 11;

    public BaseDialog(Frame owner, boolean modal) throws HeadlessException {
        super(owner, modal);

        init();
    }

    public BaseDialog(Dialog owner, boolean modal) throws HeadlessException {
        super(owner, modal);

        init();
    }

    private void init() {
        // reset translators on dialog creation
        getTranslator().reset();

        // init title and icon from resource bundle via translator
        setTitle(getTranslator().translate("Dialog(Title)"));
        final ImageIcon imageIcon = IconManager.getImageIcon(getTranslator().translate("Dialog(Icon)"), IconManager.IconSize.SIZE16);

        if (imageIcon != null) {
            setIconImage(imageIcon.getImage());
        } else {
            setIconImage(IconManager.getMenuIcon("dictionary.png").getImage());
        }
    }

    @Override
    public JComponent createBannerPanel() {
        if (!getTranslator().translate("Banner(Title)").equals("Banner(Title)")) {
            BannerPanel bannerPanel = new BannerPanel(getTranslator().translate("Banner(Title)"),
                    getTranslator().translate("Banner(Subtitle)"),
                    JideIconsFactory.getImageIcon(getTranslator().translate("Banner(Icon)")));

            bannerPanel.setFont(new Font("Tahoma", Font.PLAIN, FONT_SIZE));
            bannerPanel.setBackground(Color.WHITE);
            bannerPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            return bannerPanel;
        } else {
            return null;
        }
    }

    @Override
    public ButtonPanel createButtonPanel() {
        return null;
    }

    public int showDialog() {
        pack();

        // this should be called after pack()!
        setLocationRelativeTo(getParent());

        setVisible(true);

        return getDialogResult();
    }

    public int showDialog(Component comp) {
        pack();

        // this should be called after pack()!
        setLocationRelativeTo(comp);

        setVisible(true);

        return getDialogResult();
    }

    public Translator getTranslator() {
        return Translator.getTranslator(this.getClass().getSimpleName());
    }

    public Translator getBaseTranslator() {
        return Translator.getTranslator(BaseDialog.class.getSimpleName());
    }

    public Logger getLogger() {
        return LoggerFactory.getLogger(this.getClass());
    }

    protected JButton createOkButton() {
        JButton okButton = new JButton();

        okButton.setAction(new AbstractAction(getBaseTranslator().translate("OK(Button)")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDialogResult(RESULT_AFFIRMED);
                setVisible(false);
            }
        });

        return okButton;
    }

    protected JButton createCancelButton() {
        JButton closeButton = new JButton();

        closeButton.setAction(new AbstractAction(getBaseTranslator().translate("Cancel(Button)")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDialogResult(RESULT_CANCELLED);
                setVisible(false);
            }
        });

        return closeButton;
    }

    protected JButton createCloseButton() {
        JButton closeButton = new JButton();

        closeButton.setAction(new AbstractAction(getBaseTranslator().translate("Close(Button)")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDialogResult(RESULT_CANCELLED);
                setVisible(false);
            }
        });

        return closeButton;
    }

    protected JButton createHelpButton() {
        JButton helpButton = new JButton();

        helpButton.setAction(new AbstractAction(getBaseTranslator().translate("Help(Button)")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(SpellbookConstants.HELP_URL + (Locale.getDefault().getCountry().equals("BG") ? "Bulgarian" : "English")));
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }
        });

        return helpButton;
    }
}
