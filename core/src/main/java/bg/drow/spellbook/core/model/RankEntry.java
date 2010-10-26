package bg.drow.spellbook.core.model;

/**
 * The rank entries are used by the exam and the games to set
 * difficulties based on the frequency of a word in a language.
 *
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 * @since 0.3
 */
public class RankEntry extends AbstractEntity {
    public static final String TABLE_NAME = "RANK_ENTRIES";

    private Language language;

    private String word;

    private int rank;

    public RankEntry() {
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language lang) {
        this.language = lang;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (o == this) {
            return true;
        }

        if (o.getClass() != getClass()) {
            return false;
        }

        RankEntry other = (RankEntry) o;

        if (!word.equals(other.word)) {
            return false;
        }

        if (language != other.language) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.language != null ? this.language.hashCode() : 0);
        hash = 67 * hash + (this.word != null ? this.word.hashCode() : 0);
        return hash;
    }
}
