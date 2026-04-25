package hk.ust.cse.comp4321.project.crawl;

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
        int numRecords = records.size();
        System.out.println("info: crawler has retrieved " + numRecords + " documents after crawling");

        // Get parent links
        Map<URL, DocumentRecord> recordMap = records.stream().collect(
            Collectors.toMap(item -> item.url(), item -> item)
        );

        for (DocumentRecord parentRecord : records){
            for (URL childURL : parentRecord.childURLs()){
                DocumentRecord childRecord = recordMap.get(childURL);
                if (childRecord == null) continue;
                childRecord.parentURLs().add(parentRecord.url());
            }
        }

        // Get document frequencies of all terms across all docs
        HashSet<String> titleTerms = records.stream()
            .map(record -> record.titleTermFrequencies().keySet())
            .flatMap(Set::stream)
            .collect(Collectors.toCollection(HashSet::new));

        HashSet<String> bodyTerms = records.stream()
            .map(record -> record.bodyTermFrequencies().keySet())
            .flatMap(Set::stream)
            .collect(Collectors.toCollection(HashSet::new));

        Map<String, Long> titleDFs = new HashMap<>();
        Map<String, Long> bodyDFs = new HashMap<>();

        for (String term : titleTerms){
            long df = 0;
            for (DocumentRecord record : records){
                if (record.titleTermFrequencies().keySet().contains(term))
                    df++;
            }
            titleDFs.put(term, df);
        }

        for (String term : bodyTerms){
            long df = 0;
            for (DocumentRecord record : records){
                if (record.bodyTermFrequencies().keySet().contains(term))
                    df++;
            }
            bodyDFs.put(term, df);
        }

        System.out.println(titleTerms);
        System.out.println(titleDFs);
        System.out.println(bodyTerms);
        System.out.println(bodyDFs);

        // Calculate term weights (for both title and body terms)
//        for (DocumentRecord record : records){
//            for (String titleTerm : record.titleTermFrequencies().keySet()){
//
//            }
//        }


        crawler.updateIndexes();
    }
}
