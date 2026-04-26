package hk.ust.cse.comp4321.project.crawl;

import com.google.common.collect.Streams;
import hk.ust.cse.comp4321.project.index.DocumentIndex;
import hk.ust.cse.comp4321.project.index.InvertedIndex;
import hk.ust.cse.comp4321.project.index.RecordIndex;
import hk.ust.cse.comp4321.project.util.NLPUtil;
import hk.ust.cse.comp4321.project.util.URIUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;


@SuppressWarnings("UrlHashCode")
public class Crawler {
    private final DocumentIndex documentIndex;
    private final InvertedIndex invertedIndex;
    private final RecordIndex recordIndex;
    private final List<DocumentRecord> records;
    private final Queue<PendingURL> urlQueue;
    private final Set<URL> visited;
    private final int maxPages;

    private int retrieved = 0;

    public Crawler(URL rootURL, int maxPages) throws RocksDBException {
        this.documentIndex = DocumentIndex.getInstance();
        this.invertedIndex = InvertedIndex.getInstance();
        this.recordIndex = RecordIndex.getInstance();
        this.records = new ArrayList<>();
        this.maxPages = maxPages;
        this.visited = new HashSet<>();

        this.urlQueue = new ArrayDeque<>();
        this.urlQueue.add(new PendingURL(rootURL, 0));
    }

    public void crawl() {
        while (!urlQueue.isEmpty()) {
            PendingURL current = urlQueue.poll();
            if (visited.contains(current.url))
                continue;

            if (retrieved >= maxPages)
                break;
            else
                retrieved++;

            crawlOnePage(current);
        }

        populateParentURLs();
    }

    private void crawlOnePage(PendingURL current) {
        Connection.Response response;

        try {
            response = Jsoup.connect(current.url.toString()).maxBodySize(0).followRedirects(false).execute();

            Document document = response.parse();
            String title = document.title();
            List<String> words = NLPUtil.standardizeWords(NLPUtil.extractWords(document));
            List<String> titleWords = NLPUtil.standardizeWords(NLPUtil.extractWords(title));
            Map<String, Long> bodyFrequencyTable = words
                    .stream()
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
            Map<String, Long> titleFrequencyTable = titleWords
                    .stream()
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            Map<String, Set<Long>> wordPositions = Streams.mapWithIndex(words.stream(), Pair::of)
                    .collect(Collectors.groupingBy(Pair::getLeft, Collectors.mapping(Pair::getRight, Collectors.toSet())));

            List<URL> childUrls = linksFromDocument(document);
            DocumentRecord rec = new DocumentRecord(
                    title,
                    current.url,
                    lastModifiedTimeOfResponse(response),
                    titleFrequencyTable,
                    bodyFrequencyTable,
                    new HashMap<>(),
                    new HashMap<>(),
                    wordPositions,
                    pageSizeOfResponseOrDocument(response, document),
                    new HashSet<>(),
                    childUrls
            );
            visited.add(current.url);
            records.add(rec);

            urlQueue.addAll(childUrls.stream().map(it -> new PendingURL(it, current.depth + 1)).toList());
        } catch (IOException ignored) {
        }
    }

    public List<DocumentRecord> documentRecords() {
        return this.records;
    }

    private void populateParentURLs(){
        Map<URL, DocumentRecord> urlToRecordsMap = this.records.stream().collect(
            Collectors.toMap(DocumentRecord::url, item -> item)
        );

        for (DocumentRecord parentRecord : this.records){
            for (URL childURL : parentRecord.childURLs()){
                DocumentRecord childRecord = urlToRecordsMap.get(childURL);
                if (childRecord == null) continue;
                childRecord.parentURLs().add(parentRecord.url());
            }
        }
    }

    public void updateIndexes() {
        AtomicInteger recordsAdded = new AtomicInteger();
        AtomicInteger recordsModified = new AtomicInteger();
        this.records.forEach(record -> {
            try {
                Optional<Integer> optional = documentIndex.get(record.url().toString());
                final Integer key = optional.orElseGet(documentIndex::incrementID);

                Optional<DocumentRecord> recordInDatabase = recordIndex.get(key);
                if (recordInDatabase.isPresent() && !recordInDatabase.get().lastModificationTimestamp().equals(record.lastModificationTimestamp())) {
                    recordsModified.getAndIncrement();
                } else {
                    documentIndex.put(record.url().toString(), key);
                    recordsAdded.getAndIncrement();
                }

                record.wordLocations().forEach((word, locations) -> {
                    try {
                        TreeSet<Pair<Integer, Long>> existingWordLocations = invertedIndex.get(word).orElseGet(TreeSet::new);
                        TreeSet<Pair<Integer, Long>> newWordLocations = locations.stream().map(
                            loc -> Pair.of(key, loc)).collect(Collectors.toCollection(TreeSet::new)
                        );
                        existingWordLocations.addAll(newWordLocations);

                        invertedIndex.put(word, existingWordLocations);
                    } catch (RocksDBException exception) {
                        System.err.println("warning: failed to update invered index for url: " + record.url());
                    }
                });

                recordIndex.put(key, record);
            } catch (RocksDBException ignored) {
                System.err.println("warning: failed to add url " + record.url() + " to document and record indexes");
            }
        });

        System.out.println("info: " + recordsAdded + " added, " + recordsModified + " modified");
    }

    private @NotNull LocalDateTime lastModifiedTimeOfResponse(@NotNull Connection.Response response) {
        String header = response.header("Last-Modified");
        if (header == null)
            header = response.header("Date");

        assert header != null : "both Last-Modified and Date headers are missing";
        return LocalDateTime.parse(header, DateTimeFormatter.RFC_1123_DATE_TIME);
    }

    private @NotNull List<URL> linksFromDocument(@NotNull Document document) {
        return document
                .select("a[href]")
                .stream()
                .map(it -> it.absUrl("href"))
                .map(URIUtil::toURLWithoutFragment)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private int pageSizeOfResponseOrDocument(@NotNull Connection.Response response, Document document) {
        String header = response.header("Content-Length");
        if (header == null)
            return document.html().length();

        return Integer.parseInt(header);
    }

    private record PendingURL(URL url, int depth) {
    }
}
