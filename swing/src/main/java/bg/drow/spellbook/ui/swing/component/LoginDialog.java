package bg.drow.spellbook.ui.swing.component;

import com.jidesoft.dialog.ButtonPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 * @since 0.4
 */
public class LoginDialog extends BaseDialog {
    private JTextField usernameTextField;
    private JPasswordField passwordTextField;

    public LoginDialog(Dialog owner, boolean modal) throws HeadlessException {
        super(owner, modal);

        setSize(200, 200);
    }

    @Override
    public JComponent createContentPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 2", "[][]", "[][]"));

        usernameTextField = new JTextField();
        passwordTextField = new JPasswordField();

        panel.add(new JLabel("Username"));
        panel.add(usernameTextField, "growx");
        panel.add(new JLabel("Password"));
        panel.add(passwordTextField, "growx");

        return panel;
    }

    @Override
    public ButtonPanel createButtonPanel() {
        ButtonPanel buttonPanel = new ButtonPanel();

        JButton loginButton = new JButton("Login");
        JButton cancelButton = new JButton("Cancel");

        buttonPanel.add(loginButton, ButtonPanel.AFFIRMATIVE_BUTTON);
        buttonPanel.add(cancelButton, ButtonPanel.CANCEL_BUTTON);

        return buttonPanel;
    }

    public String getUsername() {
        return usernameTextField.getText();
    }

    public String getPassword() {
        return String.valueOf(passwordTextField.getPassword());
    }
}
