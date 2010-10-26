package bg.drow.spellbook.ui.swing.validation;

import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

/**
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 * @version 0.3
 */
public class ButtonControllingDocumentListener implements DocumentListener {
    private final JButton button;
    private final JTextComponent textComponent;

    public ButtonControllingDocumentListener(JTextComponent textComponent, JButton button) {
        this.textComponent = textComponent;
        this.button = button;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        if (!textComponent.getText().trim().isEmpty()) {
            button.setEnabled(true);
        } else {
            button.setEnabled(false);
        }
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        if (!textComponent.getText().trim().isEmpty()) {
            button.setEnabled(true);
        } else {
            button.setEnabled(false);
        }
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
