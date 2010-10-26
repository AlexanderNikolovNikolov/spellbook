package bg.drow.spellbook.ui.swing.component;

import bg.drow.spellbook.core.model.Difficulty;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

/**
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 * @since 0.3
 */
public class DifficultyComboBox extends JComboBox {
    public DifficultyComboBox() {
        setModel(new DefaultComboBoxModel(Difficulty.values()));
    }
}
