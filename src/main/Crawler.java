package comp4321.searchengine;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import comp4321.searchengine.HTMLPage;

public class Crawler {
    public static void parse(int numPagesToCrawl) throws IOException{
        String link = "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm";
        Optional<HTMLPage> parseResult = parseOnePage(link);
        if (parseResult.isPresent()) System.out.println(parseOnePage(link));
    }

    public static Optional<HTMLPage> parseOnePage(String link) throws IOException {
        Document doc = Jsoup.connect(link).get();
        Element body = doc.getElementsByTag("body").first();

        if (body == null) return Optional.empty();

        return Optional.of(new HTMLPage(
                doc.title(),
                link,
                LocalDateTime.parse("2023-05-16T13:03:16"),
                body.text(),
                getLinks(doc)
        ));
    }

    private static ArrayList<String> getLinks(Document doc){
        ArrayList<String> links = new ArrayList<>();
        Elements anchors = doc.select("a[href]");
        for (Element anchor : anchors){
            links.add(anchor.absUrl("href"));
        }
        return links;
    }
}
