package comp4321.searchengine;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;

import comp4321.searchengine.HTMLPage;

public class Crawler {
    public static void parse(int numPagesToCrawl) throws IOException{
        String link = "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm";
        Optional<HTMLPage> parseResult = parseOnePage(link);
        if (parseResult.isPresent()) System.out.println(parseResult);
    }

    public static Optional<HTMLPage> parseOnePage(String link) throws IOException {
        // ignoreContentType allows reading of headers, i.e. we can read Last-Modified
        Connection.Response response = Jsoup.connect(link).ignoreContentType(true).execute();
        Document doc = response.parse();

        Element body = doc.getElementsByTag("body").first();

        if (body == null) return Optional.empty();

        return Optional.of(new HTMLPage(
                doc.title(),
                link,
                getLastModified(response),
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

    private static LocalDateTime getLastModified(Connection.Response response){
        // Website responds with a format of: "Tue, 16 May 2023 05:03:16 GMT"
        String websiteResponse = response.header("Last-Modified");
        assert websiteResponse != null : "Last-Modified header of website is empty";
        return LocalDateTime.parse(websiteResponse, DateTimeFormatter.RFC_1123_DATE_TIME);
    }
}
