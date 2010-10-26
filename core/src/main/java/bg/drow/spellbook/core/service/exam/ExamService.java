package bg.drow.spellbook.core.service.exam;

import bg.drow.spellbook.core.model.Dictionary;
import bg.drow.spellbook.core.model.Difficulty;
import bg.drow.spellbook.core.model.ExamScoreEntry;
import bg.drow.spellbook.core.service.AbstractPersistenceService;
import bg.drow.spellbook.core.service.DictionaryService;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * @author Bozhidar Batsov
 * @since 0.4
 */
public class ExamService extends AbstractPersistenceService {
    private static final ExamService INSTANCE = new ExamService();

    private static final Logger LOGGER = LoggerFactory.getLogger(ExamService.class);

    public static ExamService getInstance() {
        return INSTANCE;
    }

    /**
     * A suitable dictionary for an exam is not special. Further down the line
     * additional restrictions might be added.
     *
     * @return a list of all suitable dictionaries
     */
    public List<Dictionary> getSuitableDictionaries() {
        return Lists.newArrayList(Collections2.filter(DictionaryService.getInstance().getDictionaries(), new Predicate<Dictionary>() {
            @Override
            public boolean apply(Dictionary input) {
                return !input.isSpecial();
            }
        }));
    }

    /**
     * Provides a list of appropriate words for an exam.
     *
     * @param dictionary the dictionary to be used
     * @param difficulty the exam's difficulty
     * @param size the size of the exam in words
     * @return a list of suitable words for the requested type of exam
     */
    public List<String> getWordsForExam(Dictionary dictionary, Difficulty difficulty, int size) {
        List<String> result = Lists.newArrayList();

        try {
            PreparedStatement ps = dbConnection.prepareStatement("select word from DICTIONARY_ENTRIES d join RATING_ENTRIES r on d.id=r.dictionary_entry_id where d.dictionary_id=? and r.rating>? and r.rating<?");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                result.add(rs.getString("WORD"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Collections.shuffle(result);

        return result;
    }

    private int calculateAvgRating(Dictionary dictionary) {
        try {
            PreparedStatement ps = dbConnection.prepareStatement("select sum(rating) from RANK_ENTRIES r join DICTIONARY_ENTRIES de where r.dictionary_entry_id=de.id and de.dictionary_id=?");

            ps.setLong(1, dictionary.getId());

            ResultSet rs = ps.executeQuery();

            return rs.getInt(0);
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return 0;
    }

    public boolean checkAnswer(Dictionary dictionary, String word, String guess) {
        String translation = DictionaryService.getInstance().getTranslation(word, dictionary);

        return translation.contains(guess);
    }

        public void addScoreboardResult(ExamScoreEntry examScoreEntry) {
//        EntityTransaction t = EM.getTransaction();
//        t.begin();
//        EM.persist(examScoreEntry);
//        t.commit();
    }

    public List<ExamScoreEntry> getExamScores() {
        //return EM.createQuery("select se from ExamScoreEntry se order by se.created asc", ExamScoreEntry.class).getResultList();

        return null;
    }
}
