import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

public class Indexer {
    public static void main(String[] args) throws IOException, SQLException {
        // 呼叫 Crawler 抓取 5 頁
        ArrayList<HTMLPage> res = Crawler.parse(
                "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm",
                5
        );

        for (HTMLPage page : res) {
            System.out.println("==== Page ====");
            System.out.println("URL: " + page.url());
            System.out.println("Title: " + page.title());
            System.out.println("Text: " + page.text());
            System.out.println();
        }
    }
}
