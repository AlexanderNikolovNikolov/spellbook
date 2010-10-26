package bg.drow.spellbook.core.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 */
public abstract class AbstractEntity implements Serializable {
    private long id;

    private Date created;

    private Date modified;

    protected AbstractEntity() {
    }

    protected AbstractEntity(ResultSet rs) throws SQLException {
        setId(rs.getLong("ID"));
        setCreated(rs.getDate("CREATED"));
        setModified(rs.getDate("MODIFIED"));
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }
}
