package hk.ust.cse.comp4321.project.retrieval;

import hk.ust.cse.comp4321.project.crawl.DocumentRecord;
import hk.ust.cse.comp4321.project.index.RecordIndex;
import hk.ust.cse.comp4321.project.util.NLPUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.rocksdb.RocksDBException;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Command(name = "search", description = "Performs a search query.")
public class SearchCommand implements Runnable {
    Scanner scanner = new Scanner(System.in);

    @Override
    public void run() {
        System.out.println("Enter query: ");
        String queryInput = scanner.nextLine();  // Read user input

        List<String> searchQuery = NLPUtil.standardizeWords(NLPUtil.extractWords(queryInput));
        System.out.println(searchQuery);

        // Find phrase search terms
        List<String> textInQuotes = new ArrayList<>();
        Pattern pattern = Pattern.compile("\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(queryInput);
        while (matcher.find()) {
            textInQuotes.add(matcher.group(1)); // group(1) is the content without quotes
        }

        // Keep track of original stopword positions
        // quote: [word1, word2..., wordN]
        // positions: [#stopwords between word1-2, #stopwords between word2-3..., #stopwords between wordN-1 and N]
        List<List<Integer>> stopwordPositionalCounts = new ArrayList<>();
        for (String quote : textInQuotes){
            List<String> wordsBeforeStopwordRemoval = NLPUtil.extractWords(quote)
                .stream()
                .map(String::toLowerCase)
                .filter(NLPUtil::isAlphaNumeric)
                .toList();

            int runningStopwordCount = 0;
            List<Integer> stopwordPositionsForCurrentQuote = new ArrayList<>();
            for (String word : wordsBeforeStopwordRemoval){
                if (NLPUtil.isStopword(word)) runningStopwordCount++;
                else {
                    stopwordPositionsForCurrentQuote.add(runningStopwordCount);
                    runningStopwordCount = 0;
                }
            }
            stopwordPositionsForCurrentQuote.removeFirst();  // We don't care about the number of stopwords before the first accepted word in the phrase
            stopwordPositionalCounts.add(stopwordPositionsForCurrentQuote);
        }
        System.out.println(stopwordPositionalCounts);

        List<List<String>> phrases = textInQuotes.stream()
            .map(item -> NLPUtil.standardizeWords(NLPUtil.extractWords(item)))
            .toList();

        System.out.println(phrases);

        try {
            List<DocumentRecord> records = RecordIndex.getInstance()
                .stream()
                .map(Map.Entry::getValue)
                .toList();

            // Filter out docs that don't match in phrase search

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
