package comp4321.searchengine;

import java.io.IOException;
import java.util.ArrayList;

public class Launcher {
    public static void main(String[] args) throws IOException {
        ArrayList<HTMLPage> res = Crawler.parse(
                "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm",
                5
        );
        System.out.println(res);
    }
}
