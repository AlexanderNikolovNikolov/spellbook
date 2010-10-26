package bg.drow.spellbook.core.model;

/**
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 */
public class ExamScoreEntry extends AbstractEntity {
    public static final String TABLE_NAME = "EXAM_SCORE_ENTRIES";

    private int totalWords;

    private int correctWords;

    private String name;

    private Difficulty difficulty;

    private Language fromLanguage;

    private Language toLanguage;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setTotalWords(int totalWords) {
        this.totalWords = totalWords;
    }

    public long getTotalWords() {
        return totalWords;
    }

    public void setCorrectWords(int correctWords) {
        this.correctWords = correctWords;
    }

    public long getCorrectWords() {
        return correctWords;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Difficulty getDifficulty() {
        return difficulty;
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

    public int getScore() {
        return (int) (correctWords / (double) totalWords * 100);
    }

    public Object[] toArray() {
        return new Object[]{name, fromLanguage.toString(), toLanguage.toString(), getScore()};
    }
}
