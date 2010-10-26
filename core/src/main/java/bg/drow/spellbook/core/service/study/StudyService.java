package bg.drow.spellbook.core.service.study;

import bg.drow.spellbook.core.model.Dictionary;
import bg.drow.spellbook.core.model.DictionaryEntry;
import bg.drow.spellbook.core.model.StudySet;
import bg.drow.spellbook.core.model.StudySetEntry;
import bg.drow.spellbook.core.service.AbstractPersistenceService;
import java.util.Date;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Alexander Nikolov
 * @since 0.3
 */
public class StudyService extends AbstractPersistenceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StudyService.class);
    private List<String> translations = Lists.newArrayList();

    /**
     * Builds a service object.
     */
    public StudyService() {
        super();
    }

    public List<String> getAnothersPossiblesAnswers() {
        return translations;
    }

    /**
     * Retrieves all words for study. The words are cached for subsequent
     * invokations of the method.
     *
     * @param studySetName a name which uniquely identifies a study set from which will taken the words
     * @return a list of the words for study
     */
    public List<String> getWordsForStudy(String studySetName) {
        List<String> wordsForStudy = new ArrayList<String>();

        try {
            PreparedStatement ps = dbConnection.prepareStatement("select SE.ID,DE.WORD from DICTIONARY_ENTRIES DE join STUDY_ENTRIES SE on DE.ID = SE.DICTIONARY_ENTRY_ID join STUDY_SETS SS on SE.STUDY_SET_ID = SS.ID where SS.NAME = ? order by SE.ID");
            ps.setString(1, studySetName);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                wordsForStudy.add(rs.getString("WORD"));
            }

            return wordsForStudy;
        } catch (SQLException e) {
            e.printStackTrace();

            return wordsForStudy;
        }
    }

    /**
     * Retrieves the translations of the words for study.
     *
     * @param studySetName a name which uniquely identifies a study set from which will taken the translations
     * @return a list of the translations for study
     */
    public List<String> getTranslationsForStudy(String studySetName) {
        List<String> translationsForStudy = new ArrayList<String>();

        try {
            PreparedStatement ps = dbConnection.prepareStatement("select SE.ID,DE.WORD_TRANSLATION from DICTIONARY_ENTRIES DE join STUDY_ENTRIES SE on DE.ID = SE.DICTIONARY_ENTRY_ID join STUDY_SETS SS on SE.STUDY_SET_ID = SS.ID where SS.NAME = ? order by SE.ID");
            ps.setString(1, studySetName);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                translationsForStudy.add(rs.getString("WORD_TRANSLATION"));
            }

            return translationsForStudy;
        } catch (SQLException e) {
            e.printStackTrace();

            return translationsForStudy;
        }
    }

    /**
     * Retrieves count of the words for study.
     *
     * @param studySetName sets of which study set will be taken count of words
     * @return current number of words for study from respective study set
     */
    public Long getCountOfTheWordsInStudySet(String studySetName) {
        Long count = 0L;

        try {
            PreparedStatement ps = dbConnection.prepareStatement("select count(*) from STUDY_ENTRIES where STUDY_SET_ID = (select ID from STUDY_SETS where NAME=?)");
            ps.setString(1, studySetName);

            ResultSet rs = ps.executeQuery();
            rs.next();
            count = rs.getLong("count(*)");

            return count;
        } catch (SQLException e) {
            e.getStackTrace();

            return count;
        }
    }

    /**
     * Retrieves count of the study sets.
     *
     * @return current number of the study sets
     */
    public int getCountOfStudySets(){
        int count = 0;

        try {
            PreparedStatement ps = dbConnection.prepareStatement("select count(*) from STUDY_SETS");

            ResultSet rs = ps.executeQuery();
            rs.next();
            count = rs.getInt("count(*)");

            return count;
        } catch (SQLException e) {
            e.getStackTrace();

            return count;
        }
    }

    /**
     * Retrieves all names of the study sets.
     *
     * @return a list with the names of all study sets
     * StudySet
     */
    public List<String> getNamesOfStudySets() {
        List<String> namesOfStudySets = new ArrayList<String>();

        try {

            PreparedStatement ps = dbConnection.prepareStatement("select NAME from STUDY_SETS");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                namesOfStudySets.add(rs.getString("NAME"));
            }

            return namesOfStudySets;
        } catch (SQLException e) {
            e.printStackTrace();

            return namesOfStudySets;
        }
    }

    /**
     * Retrieves all study sets.
     *
     * @return a list of study sets
     * @see StudySet
     */
    public List<StudySet> getStudySets() {
        List<String> namesOfStudySets = getNamesOfStudySets();
        List<StudySet> studySets = new ArrayList<StudySet>();
        for (String name : namesOfStudySets) {
            studySets.add(getStudySet(name));
        }

        return studySets;
    }

    /**
     * Retrieves a study set
     *
     * @param studySetName determined which study set will be returned
     * @return a StudySet
     */
    public StudySet getStudySet(String studySetName) {
        List<StudySetEntry> studySetEntries = new ArrayList<StudySetEntry>();
        StudySet studySet = new StudySet();
        try {
            PreparedStatement ps = dbConnection.prepareStatement("select * from STUDY_SETS where NAME=?");
            ps.setString(1, studySetName);
            ResultSet rs = ps.executeQuery();
            rs.next();
            studySet = new StudySet(rs);

            ps = dbConnection.prepareStatement("select * from DICTIONARIES where ID = (select DICTIONARY_ID from STUDY_SETS where NAME=?)");
            ps.setString(1, studySetName);
            rs = ps.executeQuery();
            rs.next();
            Dictionary dictionary = new Dictionary(rs);

            studySet.setDictionary(dictionary);

            ps = dbConnection.prepareStatement("select * from DICTIONARY_ENTRIES where ID in (select DICTIONARY_ENTRY_ID from STUDY_ENTRIES where STUDY_SET_ID = (select ID from STUDY_SETS where NAME=?))");
            ps.setString(1, studySetName);
            rs = ps.executeQuery();
            while (rs.next()) {
                DictionaryEntry dictionaryEntry = new DictionaryEntry(rs);
                dictionaryEntry.setDictionary(dictionary);

                StudySetEntry studySetEntry = new StudySetEntry();
                studySetEntry.setDictionaryEntry(dictionaryEntry);
                studySetEntry.setStudySet(studySet);
                studySetEntries.add(studySetEntry);
            }

            studySet.setStudySetEntries(studySetEntries);

            return studySet;
        } catch (SQLException e) {
            e.getStackTrace();

            return studySet;
        }
    }

    /**
     * Adds a new word for study
     *
     * @param word         the word to add
     * @param dictionary   dictionary from which will be taken the word
     * @param studySetName a name which uniquely identifies a study set
     * @see Dictionary
     */
    public void addWord(String word, Dictionary dictionary, String studySetName) {

        if (word == null || word.trim().isEmpty()) {
            LOGGER.error("word == null || word.isEmpty()");
            throw new IllegalArgumentException("word == null || word.isEmpty()");
        }

        if (dictionary == null) {
            LOGGER.error("dictionary == null");
            throw new IllegalArgumentException("dictionary == null");
        }

        if (studySetName == null || studySetName.isEmpty()) {
            LOGGER.error("studySetName == null || studySetName.isEmpty()");
            throw new IllegalArgumentException("studySetName == null || studySetName.isEmpty()");
        }

        try {
            PreparedStatement ps = dbConnection.prepareStatement("select ID from DICTIONARY_ENTRIES where WORD=? and DICTIONARY_ID=?");
            ps.setString(1, word);
            ps.setLong(2, dictionary.getId());
            ResultSet rs = ps.executeQuery();
            rs.next();
            long dictionaryEntryID = rs.getLong("ID");

            ps = dbConnection.prepareStatement("select ID from STUDY_SETS where NAME=?");
            ps.setString(1, studySetName);
            rs = ps.executeQuery();
            rs.next();
            int studySetId = rs.getInt("ID");

            ps = dbConnection.prepareStatement("insert into STUDY_ENTRIES (ID, MODIFIED, CREATED, DICTIONARY_ENTRY_ID, STUDY_SET_ID) values(?, NULL, ?, ?, ?)");
            ps.setLong(1, getLastIdFromTable("STUDY_ENTRIES") + 1);
            long date = (new Date()).getTime();
            ps.setDate(2, new java.sql.Date(date));
            ps.setLong(3, dictionaryEntryID);
            ps.setLong(4, studySetId);

            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Retrieves last id from table which is determined with tableName parameter.
     *
     * @param tableName the name of the table from which will taken the last id
     * @return last id from the table
     */
    private long getLastIdFromTable(String tableName){
        long id = 0L;

        try {
            PreparedStatement ps = dbConnection.prepareStatement("select ID from " + tableName + " order by ID desc limit 1");

            ResultSet rs = ps.executeQuery();
            rs.next();

            id = rs.getLong("ID");

            return id;
        } catch (SQLException e) {
            e.getStackTrace();

            return id;
        }
    }

    /**
     * Deletes a word which no want any more to study.
     *
     * @param word         word to delete
     * @param studySetName the name of study set from which we want to delete the word
     * @param dictionary the dictionary of study set and of the word
     */
    public void deleteWord(String word, String studySetName, Dictionary dictionary) {

        try{
            PreparedStatement ps = dbConnection.prepareStatement("delete from STUDY_ENTRIES where DICTIONARY_ENTRY_ID = (select ID from DICTIONARY_ENTRIES where WORD=? and DICTIONARY_ID=?)");
            ps.setString(1, word);
            ps.setLong(2, dictionary.getId());
            ps.executeUpdate();

        }catch(SQLException e){
            e.getStackTrace();
        }
    }

    /**
     * Adds a new study set.
     *
     * @param name study set's name
     * @param dictionary study set's dictionary
     * @see StudySet
     * @see Dictionary
     */
    public void addStudySet(String name, Dictionary dictionary) {

        try {
            PreparedStatement ps = dbConnection.prepareStatement("insert into STUDY_SETS (ID, MODIFIED, CREATED, NAME, DICTIONARY_ID) values(?, NULL, ?, ?, ?)");

            ps.setLong(1, getLastIdFromTable("STUDY_SETS") + 1);
            long date = (new Date()).getTime();
            ps.setDate(2, new java.sql.Date(date));
            ps.setString(3, name);
            ps.setLong(4, dictionary.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.getStackTrace();
        }
    }

    /**
     * Deletes a study set.
     *
     * @param studySetName determined which study set to be deleted
     * @see StudySet
     */
    public void deleteStudySet(String studySetName) {
        try {
            PreparedStatement ps = dbConnection.prepareStatement("delete from STUDY_ENTRIES where STUDY_SET_ID = (select ID from STUDY_SETS where NAME=?)");
            ps.setString(1, studySetName);
            ps.executeUpdate();

            ps = dbConnection.prepareStatement("delete from STUDY_SETS where NAME=?");
            ps.setString(1, studySetName);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.getStackTrace();
        }
    }

    public List<String> getPossiblesTranslations(String translation) {
        translation = translation.toLowerCase();

        List<String> rows = Lists.newArrayList();
        rows = splitTranslationOfRows(translation);

        List<String> rowsWithPossiblesTranslations = Lists.newArrayList();
        rowsWithPossiblesTranslations = getRowsWithPossiblesTranslations(rows);

        List<String> rowsWithPossiblesTranslationsWithoutUnneededThings = Lists.newArrayList();
        rowsWithPossiblesTranslationsWithoutUnneededThings = removingAllUnneededThings(rowsWithPossiblesTranslations);

        List<String> possibleTranslations = Lists.newArrayList();
        possibleTranslations = splitAllTranslations(rowsWithPossiblesTranslationsWithoutUnneededThings);

        return possibleTranslations;
    }

    private List<String> splitTranslationOfRows(String translation) {
        List<String> rows = Lists.newArrayList();
        int endIndex = 0;
        while (translation.contains("\n")) {
            endIndex = translation.indexOf("\n");
            rows.add(translation.substring(0, endIndex));
            translation = translation.substring(endIndex + 1);
        }

        return rows;
    }

    private List<String> getRowsWithPossiblesTranslations(List<String> rows) {
        List<String> rowsWithPossiblesTranslations = Lists.newArrayList();
        String id1 = " ";
        String id2 = " ";
        int beginIndex = 0;
        int endIndex = 0;

        String[] identificatorsForRowsWithPossibleTranslations = {
            "1.", "2.", "3.", "4.", "5.", "6.", "7.", "8.", "9.", "10.", "11.", "12.", "13.", "14.", "15.", "16.", "17.", "18.", "19.", "20.",
            "21.", "22.", "23.", "24.", "25.", "26.", "27.", "28.", "29.", "30.", "31.", "32.", "33.", "34.", "35.",
            "n", "pl", "a", "adv", "v", "int", "sl.", "prep", "adj", "обик.", "и",
            "i.", "ii.", "iii.", "iv.", "v.", "vi.", "vii.", "viii.", "ix.", "x.", "xi.", "xii.", "xiii.", "xiv.", "xv.",
            "л", "г", "r", "cj"
        };

        for (String row : rows) {
            endIndex = row.indexOf(" ");
            if (endIndex != -1) {
                id1 = row.substring(0, endIndex);
                beginIndex = endIndex + 1;
                endIndex = row.indexOf(" ", beginIndex);
                if (endIndex != -1) {
                    id2 = row.substring(beginIndex, endIndex);
                }
                for (String identificator : identificatorsForRowsWithPossibleTranslations) {
                    if (id1.equals(identificator) || id2.equals(identificator)) {
                        boolean isLatinWord = false;
                        if (id1.equals("a")) {    //not all rows which begin with "a" are rows with possibles translations
                            isLatinWord = checkingWhetherIsLatinWord(id2);
                        }
                        if (!isLatinWord) {
                            rowsWithPossiblesTranslations.add(row);
                            break;
                        }
                    }
                }
            }
        }
        return rowsWithPossiblesTranslations;
    }

    private boolean checkingWhetherIsLatinWord(String id2) {
        boolean isLatinWord = false;
        char[] alphabet = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
            'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
        };

        for (char character : alphabet) {
            if (id2.charAt(0) == character) {
                isLatinWord = true;
                break;
            }
        }
        return isLatinWord;
    }

    private List<String> removingAllUnneededThings(List<String> rowsWithPossiblesTranslations) {
        List<String> rowsWithPossiblesTranslationsWithoutAbbreviations = Lists.newArrayList();
        String rowWithoutAbbreviations = null;
        int beginIndex = 0;
        int endIndex = 0;
        int countOfBrackets = 0;
        String inTheBrackets = null;
        for (String row : rowsWithPossiblesTranslations) {
            rowWithoutAbbreviations = removeAllAbbreviations(row);
            rowWithoutAbbreviations = rowWithoutAbbreviations.replaceAll("[a-z]", "");
            while (rowWithoutAbbreviations.indexOf("(", beginIndex) != -1) {
                beginIndex = rowWithoutAbbreviations.indexOf("(", beginIndex) + 1;
                countOfBrackets++;
            }
            for (int i = 0; i < countOfBrackets; i++) {
                beginIndex = rowWithoutAbbreviations.indexOf("(");
                if (beginIndex != -1) {
                    endIndex = rowWithoutAbbreviations.indexOf(")") + 1;
                    inTheBrackets = rowWithoutAbbreviations.substring(beginIndex, endIndex);
                    if (!inTheBrackets.equals("( )")) {
                        rowWithoutAbbreviations = rowWithoutAbbreviations.replaceAll(inTheBrackets, "");
                    }
                    rowWithoutAbbreviations = rowWithoutAbbreviations.replaceFirst("\\(", "");
                    rowWithoutAbbreviations = rowWithoutAbbreviations.replaceFirst("\\)", "");
                }
            }
            rowsWithPossiblesTranslationsWithoutAbbreviations.add(rowWithoutAbbreviations);
        }
        return rowsWithPossiblesTranslationsWithoutAbbreviations;
    }

    private String removeAllAbbreviations(String translation) {
        int beginIndex = 0, endIndex = 0;

        String str = null;
        StringBuilder strForRegularExpression = new StringBuilder();
        while (translation.contains(".")) {
            endIndex = translation.indexOf(".");
            beginIndex = endIndex;
            while (translation.charAt(beginIndex) != ' ' && translation.charAt(beginIndex) != '\n' && translation.charAt(beginIndex) != '(' && translation.charAt(beginIndex) != ')' && beginIndex != 0) {
                beginIndex--;
            }
            if (beginIndex == 0) {
                translation = translation.substring(endIndex + 1);
            } else {
                str = translation.substring(beginIndex + 1, endIndex);
                if (str.isEmpty()) {
                    translation = translation.replaceFirst("\\.", "");
                } else {
                    strForRegularExpression = new StringBuilder();
                    strForRegularExpression.append(str);
                    strForRegularExpression.append("\\.");
                    translation = translation.replaceAll(strForRegularExpression.toString(), "");
                }
            }
        }
        //translation = removeSpacesInTheBeginningAndEnd(translation);
        return translation;
    }

    private List<String> splitAllTranslations(List<String> rowsWithPossiblesTranslationsWithoutAbbreviations) {
        String[] translationsFromRow = null;
        List<String> possibleTranslations = Lists.newArrayList();
        for (String row : rowsWithPossiblesTranslationsWithoutAbbreviations) {
            translationsFromRow = row.split("[,|!|?]+");
            for (String possibleTranslation : translationsFromRow) {
                //if (!possibleTranslation.isEmpty()) {

                possibleTranslation = removeSpacesInTheBeginningAndEnd(possibleTranslation);
                if (!possibleTranslation.isEmpty()) {
                    possibleTranslations.add(possibleTranslation);
                }
                //}
            }
        }
        return possibleTranslations;
    }

    public String removeSpacesInTheBeginningAndEnd(String word) {
        while (word.length() != 0 && word.charAt(0) == ' ') {
            word = word.substring(1);
        }
        int endIndex = word.length() - 1;
        while (word.length() != 0 && word.charAt(endIndex) == ' ') {
            endIndex--;
        }
        if (endIndex != word.length() - 1) {
            word = word.substring(0, endIndex + 1);
        }
        return word;
    }

    public void possibleAnswers(String translation) {

        translations = Lists.newArrayList();
        translation = translation.toLowerCase();

        //Removes the uneeded characters from the translation
        String t = translation.replaceAll("\\b(n|a|v|(attr)|(adv)|[0-9]+)\\b\\s?", "");

        //Splits the translation around matches of the pattern
        String[] s = Pattern.compile("\\s*[,|;|.|\\n]\\s*").split(t, 0);

        for (int i = 0; i < s.length; i++) {

            if (s[i].isEmpty()) {
                continue;
            }

            if (s[i].contains("(")) {
                //removes the parenthesis and everything inside them
                slash(s[i].replaceAll("\\(([^()]*)\\)?", ""));
                //removes the parenthesis only
                slash(s[i].replaceAll("\\(([^()]*)\\)", "$1"));
            } else {
                slash(s[i]);
            }
        }
    }

    private void slash(String s) {
        String first = "";
        String last = "";

        if (s.contains("/")) {
            String[] slash = s.split("/");
            //Using this loop we make all possible combinations for correct answer
            for (int j = 0; j < slash.length; j++) {
                translations.add(slash[j]);

                if (j != slash.length - 1) {

                    //Combines the whole last string(the string after the last forward slash) with every other string
                    translations.add(slash[j] + " " + slash[slash.length - 1]);
                }
            }

            //removes the last word from the first string
            if ((slash[0].contains(" "))) {
                first = slash[0].substring(0, slash[0].lastIndexOf(" "));
            }

            //removes the first word from the last string
            if ((slash[slash.length - 1].contains(" "))) {
                last = slash[slash.length - 1].substring(slash[slash.length - 1].indexOf(" ") + 1);
            }

            if (!(first.isEmpty())) {

                //Combines the new fist string with every other string
                for (int j = 0; j < slash.length; j++) {
                    translations.add(first + " " + slash[j]);
                }
            }

            if (!(last.isEmpty())) {

                //Combines the new last string with every other string
                for (int j = 0; j < slash.length; j++) {
                    translations.add(slash[j] + " " + last);
                }
            }
        } else {
            translations.add(s);
        }

        Set<String> set = new LinkedHashSet<String>(translations);
        translations = new ArrayList<String>(set);
    }

    public String combinePossiblesTranslationsForTheTable(List<String> translations) {
        StringBuilder translation = new StringBuilder();

        for (int i = 0; i < translations.size(); i++) {
            translation.append(translations.get(i));
            if (i != translations.size() - 1) {
                translation.append(", ");
            }
        }
        return translation.toString();
    }
}
