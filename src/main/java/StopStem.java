import IRUtilities.Porter;
import java.io.*;
import java.sql.SQLException;
import java.util.*;

public class StopStem {
    private Porter porter;
    private HashSet<String> stopWords;

    public StopStem(String stopWordsFile) {
        porter = new Porter();
        stopWords = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(stopWordsFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                stopWords.add(line.trim().toLowerCase());
            }
        } catch (IOException e) {
            System.err.println("Error loading stopwords: " + e.getMessage());
        }
    }

    private boolean isStopWord(String str) {
        return stopWords.contains(str.toLowerCase());
    }

    private String stem(String str) {
        return porter.stripAffixes(str);
    }

    // 處理 body 並存到 inverted_index
    public void processBody(String title, String text, InvertedIndex index) throws SQLException {
        Map<String, Integer> termFreq = new HashMap<>();
        String[] tokens = text.split("\\s+");
        for (String token : tokens) {
            if (!isStopWord(token)) {
                String stemmed = stem(token);
                termFreq.put(stemmed, termFreq.getOrDefault(stemmed, 0) + 1);
            }
        }
        for (Map.Entry<String, Integer> entry : termFreq.entrySet()) {
            index.addPosting(entry.getKey(), title, entry.getValue());
        }
    }


}
