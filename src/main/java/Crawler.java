import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.lang.System.Logger;


public final class Crawler {
    private static final Logger logger = System.getLogger(Crawler.class.getName());

    public static ArrayList<HTMLPage> parse(String startingLink, int numPagesToCrawl) throws IOException {
        ArrayList<HTMLPage> pages = new ArrayList<>();
        Queue<String> linksToVisit = new LinkedList<>();
        linksToVisit.add(startingLink);

        HashSet<String> visitedLinks = new HashSet<>();

        // Crawl up to numPagesToCrawl pages using BFS
        while (!linksToVisit.isEmpty() && visitedLinks.size() < numPagesToCrawl) {
            String link = linksToVisit.remove();

            // Check if the site is already visited during THIS crawl
            if (visitedLinks.contains(link)) {
                logger.log(Logger.Level.INFO, link + " is already visited");
                continue;
            }

            // ignoreContentType allows reading of HTTP headers
            Connection.Response response = Jsoup.connect(link).ignoreContentType(true).execute();

            // We want to crawl children, regardless of whether the doc is up-to-date in db
            Optional<HTMLPage> parseResult = parseOnePage(link, response);

            if (parseResult.isPresent()) {
                HTMLPage page = parseResult.get();
                visitedLinks.add(link);
                linksToVisit.addAll(page.childUrls());

                //if (!docUpToDateInDB(link, response)) pages.add(page);
                pages.add(page);
            } else {
                logger.log(Logger.Level.ERROR, "Crawling %s failed\n", link);
            }
        }

        return pages;
    }

    private static Optional<HTMLPage> parseOnePage(String link, Connection.Response response) throws IOException {
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

    private static ArrayList<String> getLinks(Document doc) {
        ArrayList<String> links = new ArrayList<>();
        Elements anchors = doc.select("a[href]");
        for (Element anchor : anchors) {
            links.add(anchor.absUrl("href"));
        }
        return links;
    }

    private static LocalDateTime getLastModified(Connection.Response response) {
        // Website responds with a format of: "Tue, 16 May 2023 05:03:16 GMT"
        String websiteResponse = response.header("Last-Modified");
        if (websiteResponse == null) websiteResponse = response.header("Date");
        assert websiteResponse != null : "Both Last-Modified and Date headers are missing";
        return LocalDateTime.parse(websiteResponse, DateTimeFormatter.RFC_1123_DATE_TIME);
    }

    private static int getPageSize(Connection.Response response, Document doc) {
        String websiteResponse = response.header("Content-Length");
        if (websiteResponse == null) return doc.html().length();
        else return Integer.parseInt(websiteResponse);
    }
}
