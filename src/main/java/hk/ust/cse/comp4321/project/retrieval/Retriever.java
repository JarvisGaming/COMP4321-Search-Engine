package hk.ust.cse.comp4321.project.retrieval;

import hk.ust.cse.comp4321.project.crawl.DocumentRecord;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class Retriever {
    public static PriorityQueue<Pair<Double, DocumentRecord>> rankDocsByDescendingSimilarityToQuery(List<String> searchQuery, List<DocumentRecord> records){
        PriorityQueue<Pair<Double, DocumentRecord>> similarityScores = new PriorityQueue<>(
            Map.Entry.<Double, DocumentRecord>comparingByKey().reversed()
        );

        for (DocumentRecord record : records){
            // Calculate cosine similarity between document and query (for both title and body)
            // Use unweighted query terms

            Set<String> documentTitleTerms = record.titleTermWeights().keySet();
            double titleSimilarityScore = calculateSimilarityScore(searchQuery, documentTitleTerms, record.titleTermWeights());

            Set<String> documentBodyTerms = record.bodyTermWeights().keySet();
            double bodySimilarityScore = calculateSimilarityScore(searchQuery, documentBodyTerms, record.bodyTermWeights());

            // Derive and implement a mechanism to favor matches in the title. For example, a match in
            // the title would significantly boost the rank of a page
            double totalScore = titleSimilarityScore + bodySimilarityScore;
            similarityScores.add(new ImmutablePair<>(totalScore, record));
        }

        return similarityScores;
    }

    private static double calculateSimilarityScore(List<String> searchQuery, Set<String> documentTerms, Map<String, Double> termWeights) {
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
