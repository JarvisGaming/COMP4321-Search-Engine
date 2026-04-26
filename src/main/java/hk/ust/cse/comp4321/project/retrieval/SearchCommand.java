package hk.ust.cse.comp4321.project.retrieval;

import hk.ust.cse.comp4321.project.crawl.DocumentRecord;
import hk.ust.cse.comp4321.project.index.RecordIndex;
import hk.ust.cse.comp4321.project.util.NLPUtil;
import org.apache.commons.lang3.tuple.ImmutablePair;
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

            // Sorted in descending similarity
            PriorityQueue<Pair<Double, DocumentRecord>> cosineSimilaritiesTitle = new PriorityQueue<>(
                Map.Entry.<Double, DocumentRecord>comparingByKey().reversed()
            );
            PriorityQueue<Pair<Double, DocumentRecord>> cosineSimilaritiesBody = new PriorityQueue<>(
                Map.Entry.<Double, DocumentRecord>comparingByKey().reversed()
            );

            PriorityQueue<Pair<Double, DocumentRecord>> similarityScores = new PriorityQueue<>(
                Map.Entry.<Double, DocumentRecord>comparingByKey().reversed()
            );

            for (DocumentRecord record : records){
                // Calculate cosine similarity between document and query (for both title and body)
                // Use unweighted query terms

                Set<String> documentTitleTerms = record.titleTermWeights().keySet();
                double titleSimilarityScore = calculateSimilarityScore(searchQuery, documentTitleTerms, record.titleTermWeights());
                cosineSimilaritiesTitle.add(new ImmutablePair<>(titleSimilarityScore, record));

                Set<String> documentBodyTerms = record.bodyTermWeights().keySet();
                double bodySimilarityScore = calculateSimilarityScore(searchQuery, documentBodyTerms, record.bodyTermWeights());
                cosineSimilaritiesBody.add(new ImmutablePair<>(bodySimilarityScore, record));

                // Derive and implement a mechanism to favor matches in the title. For example, a match in
                // the title would significantly boost the rank of a page
                double totalScore = titleSimilarityScore + bodySimilarityScore;
                similarityScores.add(new ImmutablePair<>(totalScore, record));
            }

            System.out.println("cosineSimilaritiesTitle");
            while (!cosineSimilaritiesTitle.isEmpty()) {
                var pair = cosineSimilaritiesTitle.poll();
                System.out.println(pair.getLeft() + ": " + pair.getRight().url());
            }
            System.out.println("cosineSimilaritiesBody");
            while (!cosineSimilaritiesBody.isEmpty()) {
                var pair = cosineSimilaritiesBody.poll();
                System.out.println(pair.getLeft() + ": " + pair.getRight().url());
            }
            System.out.println("similarityScores");
            while (!similarityScores.isEmpty()) {
                var pair = similarityScores.poll();
                System.out.println(pair.getLeft() + ": " + pair.getRight().url());
            }

        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }

    private double calculateSimilarityScore(List<String> searchQuery, Set<String> documentTerms, Map<String, Double> termWeights) {
        double similarityFactor = 0;

        for (String queryTerm : searchQuery){
            if (!documentTerms.contains(queryTerm)) continue;
            similarityFactor += termWeights.get(queryTerm);
        }

        double documentLengthNormalizationFactor = 0;
        for (double termWeight : termWeights.values())
            documentLengthNormalizationFactor += Math.pow(termWeight, 2);
        documentLengthNormalizationFactor = Math.sqrt(documentLengthNormalizationFactor);

        double queryLengthNormalizationFactor = Math.sqrt(searchQuery.size());

        return similarityFactor / (documentLengthNormalizationFactor * queryLengthNormalizationFactor);
    }
}
