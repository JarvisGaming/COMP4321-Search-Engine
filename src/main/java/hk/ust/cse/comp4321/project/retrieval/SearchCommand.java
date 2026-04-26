package hk.ust.cse.comp4321.project.retrieval;

import hk.ust.cse.comp4321.project.crawl.DocumentRecord;
import hk.ust.cse.comp4321.project.index.RecordIndex;
import hk.ust.cse.comp4321.project.util.NLPUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.rocksdb.RocksDBException;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.*;

@Command(name = "search", description = "Performs a search query.")
public class SearchCommand implements Runnable {
    @CommandLine.Parameters(description = "Any number of words to form the search query.")
    private List<String> queryInput = Collections.emptyList();

    @Override
    public void run() {
        List<String> searchQuery = NLPUtil.standardizeWords(queryInput);
        System.out.println(searchQuery);

        // Eventually, keep track of stopword locations in query to enable phrase search?


        try {
            List<DocumentRecord> records = RecordIndex.getInstance()
                .stream()
                .map(Map.Entry::getValue)
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
