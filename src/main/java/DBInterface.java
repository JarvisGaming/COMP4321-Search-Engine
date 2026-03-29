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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class DBInterface {
    public static final Connection connection;
    public static final QueryRunner run = new QueryRunner();
    private static final String databasePath = System.getProperty("user.dir") + "/data.db";
    private static final System.Logger logger = System.getLogger(Crawler.class.getName());

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
                            CREATE TABLE IF NOT EXISTS words (
                                wordId INTEGER PRIMARY KEY AUTOINCREMENT,
                                word TEXT UNIQUE
                            );
                        
                            CREATE TABLE IF NOT EXISTS forward_index (
                                docId INTEGER,
                                wordId INTEGER,
                                frequency INTEGER,
                                PRIMARY KEY(docId, wordId)
                            );
                        
                            CREATE TABLE IF NOT EXISTS inverted_index (
                                wordId INTEGER,
                                docId INTEGER,
                                frequency INTEGER,
                                PRIMARY KEY(wordId, docId)
                            );
                        """
        );
    }

    public static void addDocument(HTMLPage page) throws SQLException {
        Optional<HTMLPage> res = getDocument(page.url());
        if (res.isPresent()) {
            LocalDateTime lastModifiedInDB = res.get().lastModified();
            LocalDateTime lastModifiedOnWebsite = page.lastModified();
            if (lastModifiedOnWebsite.isAfter(lastModifiedInDB)) {
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
                logger.log(System.Logger.Level.INFO, page.url() + ": lastModifiedInDB (" + lastModifiedInDB + ") vs lastModifiedOnWebsite (" + lastModifiedOnWebsite + ")");
            } else {
                logger.log(System.Logger.Level.INFO, page.url() + " is in DB, and is up to date");
            }

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
            logger.log(System.Logger.Level.INFO, page.url() + " is not in DB");
        }
    }

    public static Optional<HTMLPage> getDocument(String url) throws SQLException {
        String sql = "SELECT * FROM documents WHERE url = ?";
        ArrayList<HTMLPage> res = run.query(connection, sql, documentHandler, url);
        if (res.isEmpty()) return Optional.empty();
        else return Optional.of(res.getFirst());
    }

    public static ArrayList<HTMLPage> getDocuments(ArrayList<String> urls) throws SQLException {
        // rs.setArray is not implemented in SQLite JDBC
        String sql = "SELECT * FROM documents WHERE url IN (" + "?,".repeat(urls.size());
        if (!urls.isEmpty()) sql = sql.substring(0, sql.length() - 1);  // Remove last comma
        sql += ")";

        return run.query(connection, sql, documentHandler, urls.toArray());
    }

    public static void removeSurplusDocuments(ArrayList<String> urlsToKeep) throws SQLException {
        // rs.setArray is not implemented in SQLite JDBC
        String sql = "DELETE FROM documents WHERE url NOT IN (" + "?,".repeat(urlsToKeep.size());
        if (!urlsToKeep.isEmpty()) sql = sql.substring(0, sql.length() - 1);  // Remove last comma
        sql += ")";

        run.update(connection, sql, urlsToKeep.toArray());
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
    public static void addPosting(String term, String docId, int frequency) throws SQLException {
        run.update(connection,
                "INSERT INTO inverted_index (term, docId, frequency) VALUES (?, ?, ?) " +
                        "ON CONFLICT(term, docId) DO UPDATE SET frequency = excluded.frequency",
                term, docId, frequency
        );
    }
    // word ↔ wordId mapping
    public static int getOrInsertWord(String word) throws SQLException {
        String sqlSelect = "SELECT wordId FROM words WHERE word = ?";
        ResultSetHandler<ArrayList<Integer>> handler = rs -> {
            ArrayList<Integer> result = new ArrayList<>();
            while (rs.next()) result.add(rs.getInt("wordId"));
            return result;
        };
        ArrayList<Integer> res = run.query(connection, sqlSelect, handler, word);
        if (!res.isEmpty()) return res.getFirst();

        run.update(connection, "INSERT INTO words (word) VALUES (?)", word);
        ArrayList<Integer> newRes = run.query(connection, sqlSelect, handler, word);
        return newRes.getFirst();
    }

    // forward index: 存 docId, wordId, frequency
    public static void addForwardIndex(int docId, int wordId, int frequency) throws SQLException {
        run.update(connection,
                "INSERT INTO forward_index (docId, wordId, frequency) VALUES (?, ?, ?) " +
                        "ON CONFLICT(docId, wordId) DO UPDATE SET frequency = excluded.frequency",
                docId, wordId, frequency
        );
    }
    // inverted index (postings list)
    public static void addPosting(int wordId, int docId, int frequency) throws SQLException {
        run.update(connection,
                "INSERT INTO inverted_index (wordId, docId, frequency) VALUES (?, ?, ?) " +
                        "ON CONFLICT(wordId, docId) DO UPDATE SET frequency = excluded.frequency",
                wordId, docId, frequency
        );
    }
    public static int getDocIdByUrl(String url) throws SQLException {
        String sql = "SELECT id FROM documents WHERE url = ?";
        ResultSetHandler<ArrayList<Integer>> handler = rs -> {
            ArrayList<Integer> result = new ArrayList<>();
            while (rs.next()) result.add(rs.getInt("id"));
            return result;
        };
        ArrayList<Integer> res = run.query(connection, sql, handler, url);
        if (res.isEmpty()) throw new SQLException("Document not found for URL: " + url);
        return res.getFirst();
    }


}


