package hk.ust.cse.comp4321.project.crawl;

import hk.ust.cse.comp4321.project.index.Indexer;
import org.rocksdb.RocksDBException;
import picocli.CommandLine.*;

import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;


@Command(name = "crawl", description = "Crawl pages from a starting link.")
public class CrawlCommand implements Runnable {
    @Option(names = {"--url"}, description = "The URL to start crawling from.", defaultValue = "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm")
    private String rootURL;

    @Option(names = {"--pages"}, description = "The maximum number of unique pages to crawl.", defaultValue = "300")
    private int maxPages;

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
            crawler = new Crawler(url, maxPages);
        } catch (RocksDBException ignored) {
            System.err.println("error: failed to initialize crawler document record index");
            return;
        }

        crawler.crawl();

        List<DocumentRecord> records = crawler.documentRecords();
        int numRecords = records.size();
        System.out.println("info: crawler has retrieved " + numRecords + " documents after crawling");

        // Get document frequencies of all terms across all docs
        ArrayList<Set<String>> titleTerms = records.stream()
            .map(record -> record.titleTermFrequencies().keySet())
            .collect(Collectors.toCollection(ArrayList::new));

        ArrayList<Set<String>> bodyTerms = records.stream()
            .map(record -> record.bodyTermFrequencies().keySet())
            .collect(Collectors.toCollection(ArrayList::new));

        Map<String, Long> titleDFs = Indexer.getDocumentFrequencies(titleTerms);
        Map<String, Long> bodyDFs = Indexer.getDocumentFrequencies(bodyTerms);

        // Calculate term weights (for both title and body terms)
        Indexer.populateTermWeights(records, titleDFs, bodyDFs);

        crawler.updateIndexes();
    }
}
