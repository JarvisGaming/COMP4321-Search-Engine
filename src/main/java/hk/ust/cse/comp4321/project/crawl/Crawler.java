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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
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
    private final int maxDepth;

    private int retrieved = 0;

    public Crawler(URL rootURL, int maxPages, int maxDepth) throws RocksDBException {
        this.documentIndex = DocumentIndex.getInstance();
        this.invertedIndex = InvertedIndex.getInstance();
        this.recordIndex = RecordIndex.getInstance();
        this.records = new ArrayList<>();
        this.maxPages = maxPages;
        this.maxDepth = maxDepth;
        this.visited = new HashSet<>();

        this.urlQueue = new ArrayDeque<>();
        this.urlQueue.add(new PendingURL(rootURL, 0));
    }

    public void crawl() {
        while (!urlQueue.isEmpty()) {
            PendingURL current = urlQueue.poll();
            if (current.depth > maxDepth)
                break;
            if (visited.contains(current.url))
                continue;

            if (retrieved >= maxPages)
                break;
            else
                retrieved++;

            Connection.Response response;

            try {
                response = Jsoup.connect(current.url.toString()).maxBodySize(0).followRedirects(false).execute();

                Document document = response.parse();
                String title = document.title();
                List<String> words = NLPUtil.extractWords(document)
                        .stream()
                        .map(String::toLowerCase)
                        .filter(NLPUtil::isAlphaNumeric)
                        .filter(NLPUtil::isNotStopword)
                        .map(NLPUtil::stem)
                        .toList();
                List<String> titleWords = NLPUtil.extractWords(title)
                        .stream()
                        .map(String::toLowerCase)
                        .filter(NLPUtil::isAlphaNumeric)
                        .filter(NLPUtil::isNotStopword)
                        .map(NLPUtil::stem)
                        .toList();
                Map<String, Long> frequencyTable = words
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
                        frequencyTable,
                        titleFrequencyTable,
                        wordPositions,
                        pageSizeOfResponseOrDocument(response, document),
                        childUrls
                );
                visited.add(current.url);
                records.add(rec);

                urlQueue.addAll(childUrls.stream().map(it -> new PendingURL(it, current.depth + 1)).toList());
            } catch (IOException ignored) {
            }
        }
    }

    public List<DocumentRecord> documentRecords() {
        return this.records;
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
