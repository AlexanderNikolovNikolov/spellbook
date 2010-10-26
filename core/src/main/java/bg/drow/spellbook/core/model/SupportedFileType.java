package bg.drow.spellbook.core.model;

import bg.drow.spellbook.core.i18n.Translator;

/**
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 * @since 0.4
 */
public enum SupportedFileType {
    BGOFFICE("BgOffice"), BABYLON("Babylon");

    public static final Translator TRANSLATOR = Translator.getTranslator("Model");

    private String name;

    SupportedFileType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return TRANSLATOR.translate(name + "(FileType)");
    }
}
