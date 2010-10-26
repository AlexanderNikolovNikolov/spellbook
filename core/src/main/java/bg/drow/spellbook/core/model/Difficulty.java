package bg.drow.spellbook.core.model;

import bg.drow.spellbook.core.i18n.Translator;

/**
 * @author Bozhidar Batsov
 * @since 0.2
 */
public enum Difficulty {
    EASY(60), MEDIUM(30), HARD(15);

    private final int time; // in seconds
    private static final Translator TRANSLATOR = Translator.getTranslator("Model");

    private Difficulty(int time) {
        this.time = time;
    }

    public int getTime() {
        return time;
    }

    @Override
    public String toString() {
        switch (this) {
            case EASY:
                return TRANSLATOR.translate("Easy(Difficulty)");
            case MEDIUM:
                return TRANSLATOR.translate("Medium(Difficulty)");
            case HARD:
                return TRANSLATOR.translate("Hard(Difficulty)");
            default:
                return null;
        }
    }
}
