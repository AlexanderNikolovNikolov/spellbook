package bg.drow.spellbook.core.model;

import bg.drow.spellbook.core.i18n.Translator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 */
public class Dictionary extends AbstractEntity {
    public static final Translator TRANSLATOR = Translator.getTranslator("Model");

    public static final String TABLE_NAME = "DICTIONARIES";

    private Set<DictionaryEntry> dictionaryEntries = new HashSet<DictionaryEntry>();

    private Set<StudySet> studySets = new HashSet<StudySet>();

    private String name;

    private Language fromLanguage;

    private Language toLanguage;

    private boolean special;

    private byte[] iconSmall;

    private byte[] iconBig;

    public Dictionary() {
    }

    public Dictionary(ResultSet rs) throws SQLException {
        super(rs);

        setFromLanguage(Language.values()[(rs.getInt("FROM_LANGUAGE"))]);
        setToLanguage(Language.values()[(rs.getInt("TO_LANGUAGE"))]);
        setName(rs.getString("NAME"));
        setSpecial(rs.getBoolean("SPECIAL"));
        setIconSmall(rs.getBytes("ICON_SMALL"));
        setIconBig(rs.getBytes("ICON_BIG"));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Language getFromLanguage() {
        return fromLanguage;
    }

    public void setFromLanguage(Language fromLanguage) {
        this.fromLanguage = fromLanguage;
    }

    public Language getToLanguage() {
        return toLanguage;
    }

    public void setToLanguage(Language toLanguage) {
        this.toLanguage = toLanguage;
    }

    public boolean isSpecial() {
        return special;
    }

    public void setSpecial(final boolean pSpecial) {
        special = pSpecial;
    }

    public byte[] getIconSmall() {
        return iconSmall;
    }

    public void setIconSmall(final byte[] pIconSmall) {
        iconSmall = pIconSmall;
    }

    public byte[] getIconBig() {
        return iconBig;
    }

    public void setIconBig(final byte[] pIconBig) {
        iconBig = pIconBig;
    }

    public Set<DictionaryEntry> getDictionaryEntries() {
        return dictionaryEntries;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Dictionary that = (Dictionary) o;

        if (special != that.special) {
            return false;
        }
        if (fromLanguage != that.fromLanguage) {
            return false;
        }
        if (!name.equals(that.name)) {
            return false;
        }
        if (toLanguage != that.toLanguage) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + fromLanguage.hashCode();
        result = 31 * result + toLanguage.hashCode();
        result = 31 * result + (special ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return TRANSLATOR.translate(name + "(Dictionary)");
    }

}
