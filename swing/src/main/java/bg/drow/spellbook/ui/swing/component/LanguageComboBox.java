package bg.drow.spellbook.ui.swing.component;

import bg.drow.spellbook.core.model.Language;

import javax.swing.JComboBox;

/**
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 * @since 0.4
 */
public class LanguageComboBox extends JComboBox {
    public LanguageComboBox() {
        super(Language.values());
    }
}
