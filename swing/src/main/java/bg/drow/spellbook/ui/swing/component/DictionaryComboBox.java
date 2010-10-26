package bg.drow.spellbook.ui.swing.component;

import bg.drow.spellbook.core.model.Dictionary;

import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

/**
 *
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 * @since 0.3
 */
public class DictionaryComboBox extends JComboBox {
    public DictionaryComboBox(List<Dictionary> dictionaries) {
        setModel(new DefaultComboBoxModel(dictionaries.toArray()));
    }
}
