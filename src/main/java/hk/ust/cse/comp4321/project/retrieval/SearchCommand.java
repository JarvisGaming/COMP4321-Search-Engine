package hk.ust.cse.comp4321.project.retrieval;

import hk.ust.cse.comp4321.project.crawl.DocumentRecord;
import hk.ust.cse.comp4321.project.index.RecordIndex;
import hk.ust.cse.comp4321.project.util.NLPUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.rocksdb.RocksDBException;
import picocli.CommandLine.Command;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Command(name = "search", description = "Performs a search query.")
public class SearchCommand implements Runnable {
    Scanner scanner = new Scanner(System.in);

    @Override
    public void run() {
        System.out.println("Enter query: ");
        String queryInput = scanner.nextLine();  // Read user input

        List<String> searchQuery = NLPUtil.standardizeWords(NLPUtil.extractWords(queryInput));
        System.out.println("searchQuery: " + searchQuery);

        // Find phrase search terms
        List<String> textInQuotes = new ArrayList<>();
        Pattern pattern = Pattern.compile("\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(queryInput);
        while (matcher.find()) {
            textInQuotes.add(matcher.group(1)); // group(1) is the content without quotes
        }

        // word positions in DocumentRecord are already excluding stopwords
        // ie the text of the webpage are after NLP steps

        // Parse all words in quotes
        List<List<String>> phrases = textInQuotes.stream()
            .map(item -> NLPUtil.standardizeWords(NLPUtil.extractWords(item)))
            .toList();

        System.out.println("phrases: " + phrases);

        try {
            List<DocumentRecord> records = RecordIndex.getInstance()
                .stream()
                .map(Map.Entry::getValue)
                // Filter out docs that don't match in phrase search
                .filter(record -> Retriever.webpageContainsAllPhrases(record, phrases))
                .toList();

            PriorityQueue<Pair<Double, DocumentRecord>> similarityScores = Retriever.rankDocsByDescendingSimilarityToQuery(searchQuery, records);

            System.out.println("similarityScores");
            while (!similarityScores.isEmpty()) {
                var pair = similarityScores.poll();
                System.out.println(pair.getLeft() + ": " + pair.getRight().url());
            }

        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }
}
