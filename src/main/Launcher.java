package comp4321.searchengine;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Launcher {
    public static void main(String[] args) throws IOException, SQLException {
        ArrayList<HTMLPage> res = Crawler.parse(
            "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm",
            5
        );

        for (HTMLPage doc : res){
            DBInterface.addDocument(doc);
        }

        ArrayList<String> docUrls = res.stream().map(HTMLPage::url).collect(Collectors.toCollection(ArrayList::new));
        System.out.println(DBInterface.getDocuments(docUrls));
    }
}
