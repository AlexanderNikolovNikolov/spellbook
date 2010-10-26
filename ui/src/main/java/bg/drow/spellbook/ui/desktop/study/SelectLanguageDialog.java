package bg.drow.spellbook.ui.desktop.study;

import bg.drow.spellbook.core.model.Dictionary;
import bg.drow.spellbook.core.service.DictionaryService;
import bg.drow.spellbook.ui.swing.component.BaseDialog;
import com.google.common.collect.Lists;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 *
 * @author Sasho
 */
public class SelectLanguageDialog extends BaseDialog {

    private final DictionaryService dictionaryService;
    private List<Dictionary> dictionaries = Lists.newArrayList();
    private Dictionary selectedDictionary = new Dictionary();
    private boolean dictionaryIsSelected = false;

    private JComboBox selectLanguageComboBox = new JComboBox();

    /** Creates new form SelectLanguageDialog */
    public SelectLanguageDialog(Dialog parent, boolean modal) {
        super(parent, modal);

        dictionaryService = DictionaryService.getInstance();
        dictionaries = dictionaryService.getDictionaries();

        setResizable(false);
    }

    @Override
    public JComponent createContentPanel() {

        JPanel topPanel = new JPanel(new MigLayout("", "10[200]10[]10", "[15][30]"));

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        JLabel selectLanguageLabel = new JLabel();
        selectLanguageLabel.setText(getTranslator().translate("SelectLanguage(Label)"));
        topPanel.add(selectLanguageLabel, "span, wrap");


        for (Dictionary dict : dictionaries) {
            if (!dict.isSpecial() && !dict.getFromLanguage().getName().equals("Bulgarian")) {
                selectLanguageComboBox.addItem(dict.getFromLanguage().getName());
            }
        }
        topPanel.add(selectLanguageComboBox, "growx");

        JButton okButton = new JButton();
        okButton.setText(getTranslator().translate("ok(Button)"));
        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        topPanel.add(okButton, "w 81!");

        return topPanel;
    }

    public void okButtonActionPerformed(ActionEvent evt) {
        dictionaryIsSelected = true;
        String language = (String) selectLanguageComboBox.getSelectedItem();
        for (Dictionary dict : dictionaries) {
            if (!dict.isSpecial() && dict.getFromLanguage().getName().equals(language)) {
                selectedDictionary = dict;
            }
        }
        setVisible(false);
    }

    public Dictionary getSelectedDictionary() {
        return selectedDictionary;
    }

    public boolean getDictionaryIsSelected() {
        return dictionaryIsSelected;
    }

    public void setDictionaryIsSelected(boolean isSelected) {
        dictionaryIsSelected = isSelected;
    }
}
