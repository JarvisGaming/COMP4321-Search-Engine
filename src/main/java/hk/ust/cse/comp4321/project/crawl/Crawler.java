package hk.ust.cse.comp4321.project.crawl;

import hk.ust.cse.comp4321.project.util.NLPUtil;
import hk.ust.cse.comp4321.project.util.URIUtil;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

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
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;


@SuppressWarnings("UrlHashCode")
public class Crawler {
    private final List<DocumentRecord> records;
    private final Queue<PendingURL> urlQueue;
    private final Set<URL> visited;
    private final int maxPages;
    private final int maxDepth;

    private int retrieved = 0;

    public Crawler(URL rootURL, int maxPages, int maxDepth) {
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
                List<String> words = NLPUtil.extractWords(document)
                        .stream()
                        .filter(NLPUtil::isAlphaNumeric)
                        .filter(NLPUtil::isNotStopword)
                        .map(NLPUtil::stem)
                        .toList();
                Map<String, Long> frequencyTable = words
                        .stream()
                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

                List<URL> childUrls = linksFromDocument(document);
                DocumentRecord rec = new DocumentRecord(
                        document.title(),
                        current.url,
                        lastModifiedTimeOfResponse(response),
                        frequencyTable,
                        pageSizeOfResponseOrDocument(response, document),
                        childUrls
                );
                visited.add(current.url);
                records.add(rec);

                urlQueue.addAll(childUrls.stream().map(it -> new PendingURL(it, current.depth + 1)).toList());
            } catch (IOException _) {
            }
        }
    }

    public List<DocumentRecord> documentRecords() {
        return this.records;
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
