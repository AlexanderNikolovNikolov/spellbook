package bg.drow.spellbook.ui.desktop.study;

import bg.drow.spellbook.ui.swing.component.BaseDialog;
import com.jidesoft.dialog.ButtonPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import java.awt.Dialog;

/**
 * @author Sasho
 */
public class StudyAnswerDialog extends BaseDialog {
    private JTextPane answerTextPane = new JTextPane();

    public StudyAnswerDialog(Dialog parent, boolean modal) {
        super(parent, modal);

        setResizable(false);
    }

    @Override
    public JComponent createContentPanel() {
        JPanel topPanel = new JPanel(new MigLayout("wrap 1", "[550]", "[250][30]"));

        JScrollPane seeAnswerScrollPane = new JScrollPane();
        answerTextPane.setEditable(false);
        seeAnswerScrollPane.add(answerTextPane);
        seeAnswerScrollPane.setViewportView(answerTextPane);
        topPanel.add(seeAnswerScrollPane, "w 550!, h 250!");

        return topPanel;
    }

    @Override
    public ButtonPanel createButtonPanel() {
        ButtonPanel buttonPanel = new ButtonPanel();

        JButton closeButton = createCloseButton();

        buttonPanel.add(closeButton, ButtonPanel.CANCEL_BUTTON);

        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        return buttonPanel;
    }

    public void setAnswer(String answer) {
        answerTextPane.setText(answer);
        answerTextPane.setCaretPosition(0);
    }
}
