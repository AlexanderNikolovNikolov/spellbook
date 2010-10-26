package bg.drow.spellbook.ui.desktop;

import bg.drow.spellbook.core.model.Dictionary;
import bg.drow.spellbook.core.service.DictionaryService;
import bg.drow.spellbook.ui.swing.component.BaseDialog;
import bg.drow.spellbook.ui.swing.validation.ButtonControllingDocumentListener;
import com.google.common.collect.Lists;
import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.swing.DefaultOverlayable;
import com.jidesoft.swing.OverlayTextField;
import com.jidesoft.swing.OverlayableIconsFactory;
import com.jidesoft.swing.OverlayableUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * A dialog used to update existing words or add new ones.
 *
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 * @since 0.3
 */
public class AddUpdateWordDialog extends BaseDialog {
    private List<String> translationRows = Lists.newArrayList();
    private boolean whetherAddWord = false;
    private JButton addButton;
    private JTextField newMeaningTextField;
    private JTextField wordTextField;
    private JLabel wordTextFieldValidationLabel = new JLabel();
    private JTextPane translationPane;
    private Dictionary dictionary;
    private JButton okButton;
    private static final int FONT_SIZE = 11;

    private static final DictionaryService DICTIONARY_SERVICE = DictionaryService.getInstance();
    private static final int DEFAULT_WIDTH = 500;
    private static final int DEFAULT_HEIGHT = 500;

    public AddUpdateWordDialog(Frame parent, boolean modal, boolean add) {
        super(parent, modal);

        getTranslator().reset();

        addButton = new JButton(getTranslator().translate("Add(JButton)"));

        addButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!translationRows.contains(newMeaningTextField.getText())) {
                    translationRows.add(newMeaningTextField.getText());
                    if (translationPane.getText().isEmpty()) {
                        translationPane.setText(translationPane.getText() + newMeaningTextField.getText());
                    } else {
                        translationPane.setText(translationPane.getText() + "\n" + newMeaningTextField.getText());
                    }
                }
                newMeaningTextField.selectAll();
                newMeaningTextField.requestFocus();
            }
        });

        wordTextField = new OverlayTextField();

        okButton = createOkButton();

        wordTextField.getDocument().addDocumentListener(new ButtonControllingDocumentListener(wordTextField, okButton));

        // on add we add some special notifications for existing words
        if (add) {
            wordTextField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    checkForWordExistence();
                    //TODO check language as well
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    checkForWordExistence();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                }
            });
        } else {
            // on update only the translations can be updated
            wordTextField.setEditable(false);
        }

        newMeaningTextField = new JTextField();
        translationPane = new JTextPane();

        Action doNothing = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //do nothing
            }
        };
        translationPane.getInputMap().put(KeyStroke.getKeyStroke("ENTER"),
                "doNothing");
        translationPane.getActionMap().put("doNothing",
                doNothing);

        setLocationRelativeTo(parent);

        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

    }

    private void checkForWordExistence() {
        if (DICTIONARY_SERVICE.containsWord(wordTextField.getText(), dictionary)) {
            wordTextFieldValidationLabel.setIcon(OverlayableUtils.getPredefinedOverlayIcon(OverlayableIconsFactory.ERROR));
            wordTextFieldValidationLabel.setToolTipText(getTranslator().translate("WordAlreadyExists(Message)"));
            okButton.setEnabled(false);
        } else {
            wordTextFieldValidationLabel.setIcon(OverlayableUtils.getPredefinedOverlayIcon(OverlayableIconsFactory.CORRECT));
            wordTextFieldValidationLabel.setToolTipText(null);
            okButton.setEnabled(true);
        }
    }

    @Override
    public JComponent createContentPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 2", "[grow][]", "[][][][][][grow]"));
        if (whetherAddWord) {
            panel.add(new JLabel(getTranslator().translate("AddWord(Label)")), "span 2, left");
            setTitle(getTranslator().translate("AddDialogTitle(Title)"));
        } else {
            panel.add(new JLabel(getTranslator().translate("EditWord(Label)")), "span 2, left");
            setTitle(getTranslator().translate("UpdateDialogTitle(Title)"));
        }
        panel.add(new DefaultOverlayable(wordTextField, wordTextFieldValidationLabel, DefaultOverlayable.SOUTH_EAST), "span 2, growx, top");
        panel.add(new JLabel(getTranslator().translate("AddMeaning(Label)")), "span 2, left");
        panel.add(newMeaningTextField, "growx, top");
        panel.add(addButton, "w 73::,gapright 2,top");
        panel.add(new JLabel(getTranslator().translate("EditMeaning(Label)")), "span 2, left");
        panel.add(new JScrollPane(translationPane), "span 2,grow");

        return panel;
    }

    @Override
    public ButtonPanel createButtonPanel() {
        ButtonPanel buttonPanel = new ButtonPanel();
        JButton cancelButton = createCancelButton();
        JButton helpButton = createHelpButton();

        buttonPanel.addButton(okButton, ButtonPanel.AFFIRMATIVE_BUTTON);
        buttonPanel.addButton(cancelButton, ButtonPanel.CANCEL_BUTTON);
        buttonPanel.addButton(helpButton, ButtonPanel.HELP_BUTTON);

        setDefaultCancelAction(cancelButton.getAction());
        setDefaultAction(okButton.getAction());
        getRootPane().setDefaultButton(okButton);

        if (wordTextField.getText().isEmpty()) {
            okButton.setEnabled(false);
        }

        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return buttonPanel;
    }

    public void setWhetherAddWord(boolean whetherAddWord) {
        this.whetherAddWord = whetherAddWord;
    }

    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    public void setWord(String word) {
        wordTextField.setText(word);
    }

    public String getWord() {
        return wordTextField.getText();
    }

    public void setTranslation(String translation) {
        if (translation.subSequence(translation.length() - 2, translation.length() - 1).equals("\n")) {
            translation = translation.substring(0, translation.length() - 3); //delete last "\n"
        }
        translationPane.setText(translation);
        translationRows = splitTranslationOfRows(translation);
    }

    private List<String> splitTranslationOfRows(String translation) {
        List<String> rows = Lists.newArrayList();
        int endIndex = 0;
        while (translation.contains("\n")) {
            endIndex = translation.indexOf("\n");
            rows.add(translation.substring(0, endIndex));
            translation = translation.substring(endIndex + 1);
        }

        return rows;
    }

    public String getTranslation() {
        return translationPane.getText();
    }
}
