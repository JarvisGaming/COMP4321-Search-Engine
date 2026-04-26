package hk.ust.cse.comp4321.project.retrieval;

import hk.ust.cse.comp4321.project.crawl.DocumentRecord;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

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

    public static boolean webpageContainsAllPhrases(DocumentRecord record, List<List<String>> phrases){
        for (List<String> phrase : phrases){
            if (!webpageBodyContainsPhrase(record.wordLocations(), phrase)) return false;
        }
        return true;
    }

    private static boolean webpageBodyContainsPhrase(Map<String, Set<Long>> wordLocations, List<String> phrase){
        // If not all the stems in the phrase are in the record
        // And we need to do it twice for both title and body

        // We need title AND body word locations separately

        // So check body first
        List<Set<Long>> phraseWordLocationsInDoc = new ArrayList<>();  // [(45, 123), (46), (47, 1239)]

        for (String word : phrase){
            // Doc does not contain all the words in the phrase
            if (!wordLocations.containsKey(word))
                return false;

            phraseWordLocationsInDoc.add(wordLocations.get(word));
        }

        // Use DP
        // Start with all values from the first set as potential sequence starts
        Set<Long> reachable = new HashSet<>(phraseWordLocationsInDoc.getFirst());

        // Iterate through the remaining sets
        for (int i = 1; i < phraseWordLocationsInDoc.size(); i++) {
            Set<Long> currentSet = phraseWordLocationsInDoc.get(i);
            Set<Long> nextReachable = new HashSet<>();

            // For each value we can reach so far, try to extend by +1
            for (long val : reachable) {
                long target = val + 1;
                if (currentSet.contains(target)) {
                    nextReachable.add(target);
                }
            }

            // If no values can be extended, the sequence cannot exist
            if (nextReachable.isEmpty()) {
                return false;
            }
            reachable = nextReachable;
        }

        // Successfully reached the last set
        return true;
    }
}
