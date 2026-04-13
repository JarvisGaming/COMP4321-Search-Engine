package hk.ust.cse.comp4321.project.crawl;

import org.rocksdb.RocksDBException;
import picocli.CommandLine.*;

import java.net.URI;
import java.net.URL;
import java.util.List;


@Command(name = "crawl", description = "Crawl pages from a starting link.")
public class CrawlCommand implements Runnable {
    @Option(names = {"--url"}, description = "The URL to start crawling from.", defaultValue = "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm")
    private String rootURL;

    @Option(names = {"--pages"}, description = "The maximum number of unique pages to crawl.", defaultValue = "30")
    private int maxPages;

    @Option(names = {"-d", "--depth"}, description = "The maximum depth of the crawl.", defaultValue = "5")
    private int maxDepth;

    @Override
    public void run() {
        URL url;
        try {
            url = new URI(rootURL).toURL();
        } catch (Exception ignored) {
            System.err.println("error: invalid root url \"" + rootURL + "\"");
            return;
        }

        Crawler crawler;
        try {
            crawler = new Crawler(url, maxPages, maxDepth);
        } catch (RocksDBException ignored) {
            System.err.println("error: failed to initialize crawler document record index");
            return;
        }

        crawler.crawl();
        List<DocumentRecord> records = crawler.documentRecords();
        System.out.println("info: crawler has retrieved " + records.size() + " documents after crawling");

        crawler.updateIndexes();
    }
}
