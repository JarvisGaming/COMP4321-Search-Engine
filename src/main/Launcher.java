package comp4321.searchengine;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

public class Launcher {
    public static void main(String[] args) throws IOException, SQLException {
        DBInterface db = new DBInterface();

        ArrayList<HTMLPage> res = Crawler.parse(
                "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm",
                5
        );
        System.out.println(res);

        for (HTMLPage doc : res){
            DBInterface.addDocument(doc);
        }
    }
}
