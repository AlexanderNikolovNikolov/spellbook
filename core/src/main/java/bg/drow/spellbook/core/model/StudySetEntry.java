package bg.drow.spellbook.core.model;

/**
 *
 * @author Sasho
 */
public class StudySetEntry extends AbstractEntity {

    public static final String TABLE_NAME = "STUDY_ENTRIES";
    private StudySet studySet;
    private DictionaryEntry dictionaryEntry;

    public StudySetEntry() {
    }

    public StudySet getStudySet() {
        return studySet;
    }

    public void setStudySet(StudySet studySet) {
        this.studySet = studySet;
    }

    public DictionaryEntry getDictionaryEntry() {
        return dictionaryEntry;
    }

    public void setDictionaryEntry(DictionaryEntry dictionaryEntry) {
        this.dictionaryEntry = dictionaryEntry;
    }
}
