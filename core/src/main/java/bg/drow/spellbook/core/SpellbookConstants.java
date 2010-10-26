package bg.drow.spellbook.core;

import java.io.File;

public final class SpellbookConstants {
    public static final String SPELLBOOK_HOME = System.getProperty("user.home") + File.separator + ".spellbook";
    public static final String SPELLBOOK_DB_PATH = SPELLBOOK_HOME + File.separator + "db" + File.separator + "spellbook.h2.db";
    public static final String REPORT_ISSUE_URL = "http://code.google.com/p/spellbook-dictionary/issues/entry";
    public static final String HELP_URL = "http://code.google.com/p/spellbook-dictionary/wiki/UsersManual";
}
