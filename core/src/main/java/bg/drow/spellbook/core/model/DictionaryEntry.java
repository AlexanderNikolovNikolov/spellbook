package bg.drow.spellbook.core.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 */
public class DictionaryEntry extends AbstractEntity {
    public static final String TABLE_NAME = "DICTIONARY_ENTRIES";

    private Dictionary dictionary;

    private Set<StudySetEntry> dictionaryEntries = new HashSet<StudySetEntry>();

    private String word;
    private String translation;
    private int rank;

    private boolean updatedByUser;

    public DictionaryEntry() {
    }

    public DictionaryEntry(ResultSet rs) throws SQLException{
        super(rs);

        setWord(rs.getString("WORD"));
        setTranslation(rs.getString("WORD_TRANSLATION"));
    }

    public boolean isUpdatedByUser() {
        return updatedByUser;
    }

    public void setUpdatedByUser(boolean updatedByUser) {
        this.updatedByUser = updatedByUser;
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String wordTranslation) {
        this.translation = wordTranslation;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public Set<StudySetEntry> getDictionaryEntries() {
        return dictionaryEntries;
    }

    public void setDictionaryEntries(Set<StudySetEntry> dictionaryEntries) {
        this.dictionaryEntries = dictionaryEntries;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final DictionaryEntry that = (DictionaryEntry) o;

        if (!dictionary.equals(that.dictionary)) {
            return false;
        }

        if (!translation.equals(that.translation)) {
            return false;
        }
        if (!word.equals(that.word)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int tresult = dictionary.hashCode();
        tresult = 31 * tresult + word.hashCode();
        tresult = 31 * tresult + translation.hashCode();
        return tresult;
    }

    @Override
    public String toString() {
        return getWord();
    }

    public static enum State {
        NEW, UPDATED, DELETED
    }
}
