package comp4321.searchengine;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Crawler {
    public static ArrayList<HTMLPage> parse(String startingLink, int numPagesToCrawl) throws IOException {
        ArrayList<HTMLPage> pages = new ArrayList<>();
        Queue<String> linksToVisit = new LinkedList<>();
        linksToVisit.add(startingLink);

        HashSet<String> visitedLinks = new HashSet<>();

        // Crawl up to numPagesToCrawl pages using BFS
        while (!linksToVisit.isEmpty() && visitedLinks.size() < numPagesToCrawl){
            String link = linksToVisit.remove();
            if (visitedLinks.contains(link)) continue;

            Optional<HTMLPage> parseResult = parseOnePage(link);

            if (parseResult.isPresent()){
                HTMLPage page = parseResult.get();
                pages.add(page);
                visitedLinks.add(link);
                linksToVisit.addAll(page.childUrls());
            } else {
                System.err.printf("Crawling %s failed\n", link);
            }
        }

        return pages;
    }

    private static Optional<HTMLPage> parseOnePage(String link) throws IOException {
        // ignoreContentType allows reading of HTTP headers
        Connection.Response response = Jsoup.connect(link).ignoreContentType(true).execute();
        Document doc = response.parse();

        Element body = doc.getElementsByTag("body").first();

        if (body == null) return Optional.empty();

        return Optional.of(new HTMLPage(
                link,
                doc.title(),
                getLastModified(response),
                body.text(),
                getPageSize(response, doc),
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
        if (websiteResponse == null) websiteResponse = response.header("Date");
        assert websiteResponse != null : "Both Last-Modified and Date headers are missing";
        return LocalDateTime.parse(websiteResponse, DateTimeFormatter.RFC_1123_DATE_TIME);
    }

    private static int getPageSize(Connection.Response response, Document doc){
        String websiteResponse = response.header("Content-Length");
        if (websiteResponse == null) return doc.html().length();
        else return Integer.parseInt(websiteResponse);
    }
}
