import java.sql.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.AbstractMap;

public class InvertedIndex {
    private Connection conn;

    public InvertedIndex(Connection conn) throws SQLException {
        this.conn = conn;
        createTableIfNotExists();
    }

    private void createTableIfNotExists() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS inverted_index (" +
                            "term TEXT NOT NULL, " +
                            "docId TEXT NOT NULL, " +
                            "frequency INTEGER NOT NULL, " +
                            "PRIMARY KEY (term, docId))"
            );
        }
    }

    // 插入或更新 posting
    public void addPosting(String term, String docId, int frequency) throws SQLException {
        String sql = "INSERT INTO inverted_index (term, docId, frequency) " +
                "VALUES (?, ?, ?) " +
                "ON CONFLICT(term, docId) DO UPDATE SET frequency = excluded.frequency";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, term.toLowerCase());
            pstmt.setString(2, docId);
            pstmt.setInt(3, frequency);
            pstmt.executeUpdate();
        }
    }


    // 查詢某個 doc 的所有 terms → Map<Term, freq>
    public Map<String, Integer> getTermsForDoc(String docId) throws SQLException {
        Map<String, Integer> terms = new HashMap<>();
        String sql = "SELECT term, frequency FROM inverted_index WHERE docId = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, docId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                terms.put(rs.getString("term"), rs.getInt("frequency"));
            }
        }
        return terms;
    }

    // 查詢所有 postings → Map<Term, List<(DocID,freq)>>
    public Map<String, List<Map.Entry<String, Integer>>> getAllPostings() throws SQLException {
        Map<String, List<Map.Entry<String, Integer>>> postings = new HashMap<>();
        String sql = "SELECT term, docId, frequency FROM inverted_index";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String term = rs.getString("term");
                String docId = rs.getString("docId");
                int freq = rs.getInt("frequency");

                postings.putIfAbsent(term, new ArrayList<>());
                postings.get(term).add(new AbstractMap.SimpleEntry<>(docId, freq));
            }
        }
        return postings;
    }

    // 查詢某個 doc 的某個 term → freq
    /*
    public int getTermForDoc(String docId, String term) throws SQLException {
        String sql = "SELECT frequency FROM inverted_index WHERE docId = ? AND term = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, docId);
            pstmt.setString(2, term.toLowerCase());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("frequency");
            } else {
                return 0;
            }
        }
    }
    // 查詢某個 term 的所有 postings → Map<DocID, freq>
    public Map<String, Integer> getPostings(String term) throws SQLException {
        Map<String, Integer> postings = new HashMap<>();
        String sql = "SELECT docId, frequency FROM inverted_index WHERE term = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, term.toLowerCase());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                postings.put(rs.getString("docId"), rs.getInt("frequency"));
            }
        }
        return postings;
    }
    */
}
