package bg.drow.spellbook.core.service;

import bg.drow.spellbook.core.SpellbookConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Base service class. Provides access to the application's database.
 *
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 * @since 0.3
 */
public class AbstractPersistenceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPersistenceService.class);

    //accessible in subclasses directly
    protected static Connection dbConnection;

    protected AbstractPersistenceService() {
        // there can be only one db connection ;-)
        if (dbConnection == null) {
            initDbConnection(SpellbookConstants.SPELLBOOK_DB_PATH);
        } else {
            LOGGER.info("Entity manager is already initialized");
        }
    }

    private static void initDbConnection(String dbFile) {
        LOGGER.info("dictionary database: " + dbFile.replace(".h2.db", ""));

        String url = "jdbc:h2:" + dbFile.replace(".h2.db", "");

        try {
            dbConnection = DriverManager.getConnection(url, "spellbook", "spellbook");
        } catch (SQLException e) {
            e.printStackTrace();

            System.exit(-1);
        }
    }
}
