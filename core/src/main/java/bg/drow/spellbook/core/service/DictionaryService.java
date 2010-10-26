package bg.drow.spellbook.core.service;

import bg.drow.spellbook.core.model.Dictionary;
import bg.drow.spellbook.core.model.DictionaryEntry;
import bg.drow.spellbook.core.model.Language;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Provides Spellbook's basic dictionary related functionality like looking for dictionaries, words, adding/updating/
 * deleting dictionary entries.
 *
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 */
public class DictionaryService extends AbstractPersistenceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DictionaryService.class);

    private static final DictionaryService INSTANCE = new DictionaryService();

    private static final Map<String, List<String>> DICTIONARY_CACHE = Maps.newHashMap();

    public DictionaryService() {
        super();
    }

    /**
     * Obtains the service single instance.
     *
     * @return service instance
     */
    public static DictionaryService getInstance() {
        return INSTANCE;
    }

    /**
     * Retrieve a list of all available dictionaries.
     *
     * @return a list of available dictionaries, empty list if none are available
     */
    public List<Dictionary> getDictionaries() {
        List<Dictionary> result = Lists.newArrayList();

        try {
            PreparedStatement ps = dbConnection.prepareStatement("select * from " + Dictionary.TABLE_NAME);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Dictionary dictionary = new Dictionary(rs);

                result.add(dictionary);
            }

            return result;
        } catch (SQLException e) {
            e.printStackTrace();

            System.exit(-1);
        }

        return result;
    }

    /**
     * Retrieves a dictionary by its name.
     *
     * @param dictionaryName the name of the dictionary we wish to obtain
     * @return the dictionary corresponding to the name
     */
    public Dictionary getDictionary(String dictionaryName) {
        List<Dictionary> dictionaries = getDictionaries();

        if (dictionaries.size() < 1) {
            throw new IllegalStateException("No dictionaries!");
        }

        for (Dictionary dictionary : dictionaries) {
            if (dictionary.getName().equals(dictionaryName)) {
                return dictionary;
            }
        }

        return null;
    }

    /**
     * Retrieves all words from the target dictionary. The words are cached for subsequent
     * invocations of the method
     *
     * @param d the target dictionary
     * @return a list of the words in the dictionary
     */
    public List<String> getWordsFromDictionary(Dictionary d) {
        if (!DICTIONARY_CACHE.containsKey(d.getName())) {
            LOGGER.info("Caching dictionary " + d.getName());

            List<String> words = Lists.newArrayList();

            try {
                PreparedStatement ps = dbConnection.prepareStatement("select word from " + DictionaryEntry.TABLE_NAME
                                + " where dictionary_id = ? order by LOWER(word) asc");

                ps.setLong(1, d.getId());

                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    words.add(rs.getString("word"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            DICTIONARY_CACHE.put(d.getName(), words);
        } else {
            LOGGER.info("Loading from cache dictionary " + d.getName());
        }

        return DICTIONARY_CACHE.get(d.getName());
    }

    /**
     * Retrieves the translation of a word from the specified dictionary.
     *
     * @param word the target word
     * @param d    the target dictionary
     * @return the word's translation
     */
    public String getTranslation(String word, Dictionary d) {
        try {
            PreparedStatement ps = dbConnection.prepareStatement("SELECT word_translation from " + DictionaryEntry.TABLE_NAME + " where word=? and dictionary_id=?");

            ps.setString(1, word);
            ps.setLong(2, d.getId());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("word_translation");
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();

            return null;
        }
    }

    /**
     * Adds a new word to a dictionary
     *
     * @param word        the word to add
     * @param translation the word's translation
     * @param d           the dictionary in which the word will be added
     */
    public void addWord(String word, String translation, Dictionary d) {
        if (word == null || word.trim().isEmpty()) {
            LOGGER.error("word == null || word.isEmpty()");
            throw new IllegalArgumentException("word == null || word.isEmpty()");
        }

        if (translation == null || translation.isEmpty()) {
            LOGGER.error("translation == null || translation.isEmpty()");
            throw new IllegalArgumentException("word == null || word.isEmpty()");
        }
        if (d == null) {
            LOGGER.error("d == null");
            throw new IllegalArgumentException("d == null");
        }

        if (containsWord(word, d)) {
            LOGGER.warn("word already exists: " + word);
            return;
        }

        try {
            PreparedStatement ps = dbConnection.prepareStatement("INSERT INTO DICTIONARY_ENTRIES (word, word_translation, rank, updated_by_user, dictionary_id)" +
                    "VALUES(?, ?, 1, true, ?)");

            ps.setString(1, word);
            ps.setString(2, translation);
            ps.setLong(3, d.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates a dictionary entry. Only its translation can be updated.
     *
     * @param word        the word definition to update(needed to find the entry to update)
     * @param translation the new translation
     * @param d           the dictionary containing the word
     */
    public void updateWord(String word, String translation, Dictionary d) {
        if (word == null || word.trim().isEmpty()) {
            LOGGER.error("word == null || word.isEmpty()");
            throw new IllegalArgumentException("word == null || word.isEmpty()");
        }

        if (translation == null || translation.trim().isEmpty()) {
            LOGGER.error("translation == null || translation.isEmpty()");
            throw new IllegalArgumentException("word == null || word.isEmpty()");
        }
        if (d == null) {
            LOGGER.error("d == null");
            throw new IllegalArgumentException("d == null");
        }

        if (!containsWord(word, d)) {
            return;
        }

        try {
            PreparedStatement ps = dbConnection.prepareStatement("UPDATE DICTIONARY_ENTRIES SET word_translation=? where" +
                    " word=? and dictionary_id=?");

            ps.setString(1, translation);
            ps.setString(2, word);
            ps.setLong(3, d.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes a word from the specified dictionary.
     *
     * @param word       word to delete
     * @param dictionary the dictionary to remove the word from
     */
    public void deleteWord(String word, Dictionary dictionary) {
        try {
            PreparedStatement ps = dbConnection.prepareStatement("DELETE FROM ? WHERE word=? and dictionary_id=?");

            ps.setString(1, DictionaryEntry.TABLE_NAME);
            ps.setString(2, word);
            ps.setLong(3, dictionary.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks whether a dictionary contains a word.
     *
     * @param word the word for which to check
     * @param d    the dictionary in which to check
     * @return true if the word is present, false otherwise
     */
    public boolean containsWord(String word, Dictionary d) {
        if (d == null) {
            LOGGER.error("d == null");
            throw new IllegalArgumentException("d == null");
        }

        try {
            PreparedStatement ps = dbConnection.prepareStatement("SELECT * from " + DictionaryEntry.TABLE_NAME + " WHERE word = ? and dictionary_id = ?");

            ps.setString(1, word);
            ps.setLong(2, d.getId());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();

            return false;
        }
    }

    /**
     * Checks if a dictionary is complemented(dual).
     *
     * @param dictionary the dictionary to check
     * @return true if the dictionary is complemented, false otherwise
     */
    public boolean isComplemented(Dictionary dictionary) {
        return getComplement(dictionary) != null;
    }

    /**
     * Retrieves a dictionary complement
     *
     * @param dictionary the dictionary for which we need a complement
     * @return the dictionary's complement
     */
    public Dictionary getComplement(Dictionary dictionary) {
        List<Dictionary> candidates = Lists.newArrayList();

        List<Dictionary> dictionaries = getDictionaries();

        for (Dictionary d : dictionaries) {
            if (d.getFromLanguage().equals(dictionary.getToLanguage()) && d.getToLanguage().equals(dictionary.getFromLanguage())) {
                candidates.add(d);
            }
        }

        for (Dictionary candidate : candidates) {
            String[] langs = candidate.getName().split("-");

            if (langs.length == 2 && (langs[0].equalsIgnoreCase(dictionary.getToLanguage().getName())) &&
                    langs[1].equalsIgnoreCase(dictionary.getFromLanguage().getName())) {
                return candidate;
            }
        }

        return null;
    }

    public Dictionary createDictionary(Language from, Language to, String name, boolean special, byte[] smallIcon, byte[] bigIcon) {
        Dictionary dictionary = new Dictionary();
        dictionary.setFromLanguage(from);
        dictionary.setToLanguage(to);
        dictionary.setName(name);
        dictionary.setSpecial(special);
        dictionary.setIconSmall(smallIcon);
        dictionary.setIconBig(bigIcon);

        try {
            PreparedStatement ps = dbConnection.prepareStatement("INSERT INTO " + Dictionary.TABLE_NAME + " (name, from_language, to_language, special, icon_small, icon_big) " +
                    "values(?, ?, ?, ?, ?, ?)");

            ps.setString(1, name);
            ps.setInt(2, from.ordinal());
            ps.setInt(3, to.ordinal());
            ps.setBoolean(4, special);
            ps.setBytes(5, smallIcon);
            ps.setBytes(6, bigIcon);

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        LOGGER.info("Created dictionary " + name + "with id " + dictionary.getId());

        return dictionary;
    }

    public void addWords(List<DictionaryEntry> dictionaryEntries) {
        for (DictionaryEntry tDictionaryEntry : dictionaryEntries) {
            //TODO persist
        }
    }
}
