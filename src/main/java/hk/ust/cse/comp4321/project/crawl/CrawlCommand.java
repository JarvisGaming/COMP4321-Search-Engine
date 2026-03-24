package hk.ust.cse.comp4321.project.crawl;

import picocli.CommandLine.*;

import java.net.URI;
import java.net.URL;
import java.util.List;


@Command(name = "crawl", description = "Crawl pages from a starting link.")
public class CrawlCommand implements Runnable {
    @Option(names = {"--url"}, description = "The URL to start crawling from.")
    private String rootURL = "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm";

    @Option(names = {"--pages"}, description = "The maximum number of unique pages to crawl.")
    private int maxPages = 30;

    @Option(names = {"-d", "--depth"}, description = "The maximum depth of the crawl.")
    private int maxDepth = 5;

    @Override
    public void run() {
        URL url;
        try {
            url = new URI(rootURL).toURL();
        } catch (Exception e) {
            System.err.println("error: invalid root url \"" + rootURL + "\"");
            return;
        }

        Crawler crawler = new Crawler(url, maxPages, maxDepth);
        crawler.crawl();

        List<DocumentRecord> records = crawler.documentRecords();
        System.out.println("info: crawler has retrieved " + records.size() + " documents after crawling");
    }
}
