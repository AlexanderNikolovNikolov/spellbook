package bg.drow.spellbook.core.model;

import bg.drow.spellbook.core.i18n.Translator;

public enum Language {
    BULGARIAN("Bulgarian", "абвгдежзийклмнопрстуфхцчшщъьюя", "flag_bulgaria.png","bg_BG"),
    ENGLISH("English", "abcdefghijklmnopqrstuvwxyz", "flag_great_britain.png","en_US"),
    GERMAN("German", "abcdefghijklmnopqrstuvwxyz", "flag_germany.png","de_DE"),
    FRENCH("French", "abcdefghijklmnopqrstuvwxyz", "flag_france.png","fr_FR"),
    ITALIAN("Italian", "abcdefghijklmnopqrstuvwxyz", "flag_italy.png","it_IT"),
    SPANISH("Spanish", "abcdefghijklmnopqrstuvwxyz", "flag_spain.png","es_ES"),
    PORTUGUESE("Portuguese", "abcdefghijklmnopqrstuvwxyz", "flag_portugal.png","pt_PT"),
    RUSSIAN("Russian", "abcdefghijklmnopqrstuvwxyz", "flag_russia.png","ru_RU");

    public static final Translator TRANSLATOR = Translator.getTranslator("Model");

    private String name;
    private String alphabet;
    private String iconName;
    private String pathToHunDictionary;

    private Language(String name, String alphabet, String iconName, String pathToHunDictionary) {
        this.name = name;
        this.alphabet = alphabet;
        this.iconName = iconName;
        this.pathToHunDictionary = pathToHunDictionary;
    }

    public String getName() {
        return name;
    }

    public String getAlphabet() {
        return alphabet;
    }

    public String getIconName() {
        return iconName;
    }

    public String getPathToHunDictionary() {
        return pathToHunDictionary;
    }

    @Override
    public String toString() {
        return TRANSLATOR.translate(name + "(Language)");
    }
}
