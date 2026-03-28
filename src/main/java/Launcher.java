import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Launcher {
    public static void main(String[] args) throws IOException, SQLException {
        ArrayList<HTMLPage> res = Crawler.parse(
                "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm",
                5
        );

        for (HTMLPage doc : res) {
            DBInterface.addDocument(doc);
        }

        ArrayList<String> docUrls = res.stream().map(HTMLPage::url).collect(Collectors.toCollection(ArrayList::new));
        System.out.println("Newly crawled docs (not in DB, or in DB but needs updating):");
        System.out.println(DBInterface.getDocuments(docUrls));

        Connection conn = DriverManager.getConnection("jdbc:sqlite:globalInvertedIndex.db");
        InvertedIndex index = new InvertedIndex(conn); //

        StopStem stopStem = new StopStem("./src/main/java/IRUtilities/stopwords.txt");
        for (HTMLPage doc : res) {
            stopStem.processBody(doc.title(), doc.text(), index);
        }

        System.out.println(index.getAllPostings()); // To get the global inverted list
        System.out.println(index.getAllPostings().get("new")); // To get the global inverted list, query for keyword "new"
        System.out.println(index.getTermsForDoc("Test page")); //To get the local inverted list for a specific file
        System.out.println(index.getTermsForDoc("Test page").get("new")); //To get the local inverted list for a specific file, query about the frequency of a specific term
    }
}
