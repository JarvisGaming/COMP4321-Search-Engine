package comp4321.searchengine;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

public final class DBInterface {
    public static final Connection connection;
    public static final QueryRunner run = new QueryRunner();
    private static String databasePath = System.getProperty("user.dir") + "/data.db";

    // Initialize the database, and the connection to the database.
    static {
        try {
            createDatabase();
            connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
            initDatabase();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createDatabase() {
        Path dbPath = Path.of(databasePath);

        try { Files.createFile(dbPath); }
        catch (FileAlreadyExistsException ignored) {}
        catch (IOException e) {e.printStackTrace(System.err);}
    }

    /**
     * Create tables of the database if they don't already exist.
     * For lastModified, it is stored as text in "YYYY-MM-DDTHH:MM:SS" format.
     */
    private static void initDatabase() throws SQLException {
        run.update(connection,
            """
                    CREATE TABLE IF NOT EXISTS "documents" (
                        "id"	INTEGER,
                        "url"	TEXT NOT NULL UNIQUE,
                        "title"	TEXT NOT NULL,
                        "lastModified"	TEXT NOT NULL,
                        "text"	TEXT,
                        "pageSize"	INTEGER NOT NULL,
                        "childUrls"	ARRAY,
                        PRIMARY KEY("id" AUTOINCREMENT)
                    );
                """
        );
    }

    public static void addDocument(HTMLPage page) throws SQLException {
        run.update(connection,
            """
                    INSERT INTO "documents" (
                        url,
                        title,
                        lastModified,
                        text,
                        pageSize,
                        childUrls
                    ) VALUES (?, ?, ?, ?, ?, ?);
                """, page.url(), page.title(), page.lastModified(), page.text(), page.pageSizeInBytes(), page.childUrls()
        );
    }

    /** {@code ResultSetHandler} implementation to convert each row of a {@code ResultSet} into a {@code User}. */
//    private static final ResultSetHandler<ArrayList<HTMLPage>> userHandler = rs -> {
//        ArrayList<HTMLPage> result = new ArrayList<>();
//
//        while (rs.next()) {result.add(new HTMLPage(
//                rs.getString("url"),
//                rs.getString("title"),
//                rs.getDate("lastModified"),
//                rs.getString("text"),
//                rs.getString("pageSize"),
//                rs.getArray("childUrls")
//                ));
//        }
//        return result;
//    };
}
