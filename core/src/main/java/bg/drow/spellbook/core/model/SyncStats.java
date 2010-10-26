package bg.drow.spellbook.core.model;

/**
 * Contains simple statistics for a synchronization.
 *
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 * @since 0.4
 */
public class SyncStats extends AbstractEntity {
    public static final String TABLE_NAME = "SYNC_STATS";

    private int pulledEntries;
    private int pushedEntries;

    public int getPulledEntries() {
        return pulledEntries;
    }

    public void setPulledEntries(int pulledEntries) {
        this.pulledEntries = pulledEntries;
    }

    public int getPushedEntries() {
        return pushedEntries;
    }

    public void setPushedEntries(int pushedEntries) {
        this.pushedEntries = pushedEntries;
    }
}
