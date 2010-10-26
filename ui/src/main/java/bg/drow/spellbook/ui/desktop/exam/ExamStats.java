package bg.drow.spellbook.ui.desktop.exam;

import bg.drow.spellbook.core.model.Dictionary;
import bg.drow.spellbook.core.model.Difficulty;
import bg.drow.spellbook.core.model.ExamScoreEntry;
import com.google.common.collect.Lists;

import java.util.Date;
import java.util.List;

/**
 * A simple Java bean containing the information for an exam.
 *
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 * @since 0.3
 */
public class ExamStats {
    private Difficulty difficulty;
    private Dictionary dictionary;
    private List<String> incorrectWords = Lists.newArrayList();
    private List<String> correctWords = Lists.newArrayList();
    private Date startTime = new Date();
    private Date endTime;

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    public List<String> getIncorrectWords() {
        return incorrectWords;
    }

    public void setIncorrectWords(List<String> incorrectWords) {
        this.incorrectWords = incorrectWords;
    }

    public List<String> getCorrectWords() {
        return correctWords;
    }

    public void setCorrectWords(List<String> correctWords) {
        this.correctWords = correctWords;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getTotalWords() {
        return correctWords.size() + incorrectWords.size();
    }

    public ExamScoreEntry createExamScoreEntry(String name) {
        ExamScoreEntry scoreEntry = new ExamScoreEntry();
        scoreEntry.setCorrectWords(correctWords.size());
        scoreEntry.setTotalWords(getTotalWords());
        scoreEntry.setFromLanguage(dictionary.getFromLanguage());
        scoreEntry.setToLanguage(dictionary.getToLanguage());
        scoreEntry.setDifficulty(difficulty);
        scoreEntry.setName(name);

        return scoreEntry;
    }

    public int getScore() {
        return (int)((correctWords.size() / (double)getTotalWords()) * 100);
    }
}
