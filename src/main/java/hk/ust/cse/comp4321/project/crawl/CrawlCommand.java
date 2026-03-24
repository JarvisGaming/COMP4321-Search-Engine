package hk.ust.cse.comp4321.project.crawl;

import picocli.CommandLine.*;

import java.net.URI;
import java.net.URL;
import java.util.List;


@Command(name = "crawl")
public class CrawlCommand implements Runnable {
    @Option(names = {"--url"})
    private String rootURL = "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm";

    @Option(names = {"--pages"})
    private int maxPages = 30;

    @Option(names = {"-d", "--depth"})
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
