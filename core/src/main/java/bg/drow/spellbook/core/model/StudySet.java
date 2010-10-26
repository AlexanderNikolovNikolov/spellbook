package bg.drow.spellbook.core.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import com.google.common.collect.Lists;
import java.util.List;

/**
 *
 * @author Sasho
 */
public class StudySet extends AbstractEntity {

    private String name;
    private Dictionary dictionary;
    private List<StudySetEntry> studySetEntries = Lists.newArrayList();

    public StudySet() {
    }

    public StudySet(ResultSet rs) throws SQLException {
        super(rs);

        setName(rs.getString("NAME"));

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    public List<StudySetEntry> getStudySetEntries() {
        return studySetEntries;
    }

    public void setStudySetEntry(StudySetEntry studySetEntry) {
        this.studySetEntries.add(studySetEntry);
    }

    public void setStudySetEntries(List<StudySetEntry> studySetEntries) {
        for (StudySetEntry sse : studySetEntries) {
            this.studySetEntries.add(sse);
        }
    }
}
