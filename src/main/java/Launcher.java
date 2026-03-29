import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Launcher {
    public static void main(String[] args) throws IOException, SQLException {
        // Step 1: Crawl 30 pages
        ArrayList<HTMLPage> res = Crawler.parse(
                "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm",
                30
        );

        // Step 2: Insert/update documents table
        for (HTMLPage doc : res) {
            DBInterface.addDocument(doc);
        }

        ArrayList<String> docUrls = res.stream()
                .map(HTMLPage::url)
                .collect(Collectors.toCollection(ArrayList::new));
        DBInterface.removeSurplusDocuments(docUrls);

        System.out.println("Crawled docs stored in data.db");
// Step 3: Indexing (forward + inverted index)
        StopStem stopStem = new StopStem("./src/main/java/IRUtilities/stopwords.txt");

        for (HTMLPage doc : res) {

            Map<String, Integer> termFreq = stopStem.processBody(doc.title(), doc.text());

            int docId = DBInterface.getDocIdByUrl(doc.url());

            for (Map.Entry<String, Integer> entry : termFreq.entrySet()) {
                String term = entry.getKey();
                int frequency = entry.getValue();

                // word ↔ wordId mapping
                int wordId = DBInterface.getOrInsertWord(term);

                // forward index
                DBInterface.addForwardIndex(docId, wordId, frequency);

                // inverted index
                DBInterface.addPosting(wordId, docId, frequency);
            }
        }

        try (PrintWriter out = new PrintWriter(new FileWriter("spider_result.txt"))) {
            for (HTMLPage doc : res) {
                int docId = DBInterface.getDocIdByUrl(doc.url());

                // Page title
                out.println(doc.title());
                // URL
                out.println(doc.url());
                // Last modification date + size
                out.println("Last modified: " + doc.lastModified() + ", Size: " + doc.pageSize());

                // Top 10 keywords (sorted by frequency)
                Map<String, Integer> termFreq = stopStem.processBody(doc.title(), doc.text());
                termFreq.entrySet().stream()
                        .sorted((a, b) -> b.getValue() - a.getValue())
                        .limit(10)
                        .forEach(e -> out.print(e.getKey() + " " + e.getValue() + "; "));
                out.println();

                // Up to 10 child links
                doc.childUrls().stream()
                    .limit(10)
                    .forEach(e -> out.println(e));

                out.println("\n---\n");
            }
            System.out.println("output spider_result.txt finished");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
