import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public final class DBInterface {
    public static final Connection connection;
    public static final QueryRunner run = new QueryRunner();
    private static final String databasePath = System.getProperty("user.dir") + "/data.db";

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

        try {
            Files.createFile(dbPath);
        } catch (FileAlreadyExistsException ignored) {
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * Create tables of the database if they don't already exist.
     * For lastModified, it is stored as text in "YYYY-MM-DDTHH:MM:SS" (ISO_LOCAL_DATE_TIME) format.
     */
    private static void initDatabase() throws SQLException {
        run.update(connection,
                """
                            CREATE TABLE IF NOT EXISTS documents (
                                "id"	INTEGER,
                                "url"	TEXT NOT NULL UNIQUE,
                                "title"	TEXT NOT NULL,
                                "lastModified"	TEXT NOT NULL,
                                "text"	TEXT,
                                "pageSize"	INTEGER NOT NULL,
                                "childUrls"	TEXT,
                                PRIMARY KEY("id" AUTOINCREMENT)
                            );
                        """
        );
    }

    public static void addDocument(HTMLPage page) throws SQLException {
        if (getDocument(page.url()).isPresent()) {
            run.update(connection,
                    """
                                UPDATE documents SET
                                    title = ?,
                                    lastModified = ?,
                                    text = ?,
                                    pageSize = ?,
                                    childUrls = ?
                                WHERE url = ?
                            """, page.title(), page.lastModified(), page.text(), page.pageSize(), String.join(", ", page.childUrls()), page.url()
            );
        } else {
            run.update(connection,
                    """
                                INSERT INTO documents (
                                    url,
                                    title,
                                    lastModified,
                                    text,
                                    pageSize,
                                    childUrls
                                ) VALUES (?, ?, ?, ?, ?, ?);
                            """, page.url(), page.title(), page.lastModified(), page.text(), page.pageSize(), String.join(", ", page.childUrls())
            );
        }
    }

    public static Optional<HTMLPage> getDocument(String url) throws SQLException {
        String sql = "SELECT * FROM documents where url = ?";
        ArrayList<HTMLPage> res = run.query(connection, sql, documentHandler, url);
        if (res.isEmpty()) return Optional.empty();
        else return Optional.of(res.getFirst());
    }

    public static ArrayList<HTMLPage> getDocuments(ArrayList<String> urls) throws SQLException {
        // rs.setArray is not implemented in SQLite JDBC
        String sql = "SELECT * FROM documents where url IN (" + "?,".repeat(urls.size());
        if (!urls.isEmpty()) sql = sql.substring(0, sql.length() - 1);  // Remove last comma
        sql += ")";

        return run.query(connection, sql, documentHandler, urls.toArray());
    }

    /**
     * {@code ResultSetHandler} implementation to convert each row of a {@code ResultSet} into a {@code HTMLPage}.
     */
    private static final ResultSetHandler<ArrayList<HTMLPage>> documentHandler = rs -> {
        ArrayList<HTMLPage> result = new ArrayList<>();
        while (rs.next()) {
            result.add(new HTMLPage(
                    rs.getString("url"),
                    rs.getString("title"),
                    LocalDateTime.parse(rs.getString("lastModified"), DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    rs.getString("text"),
                    rs.getInt("pageSize"),

                    // rs.getArray is not implemented in SQLite JDBC
                    new ArrayList<>(Arrays.asList(rs.getString("childUrls").split(", ")))
            ));
        }
        return result;
    };
}
