package hk.ust.cse.comp4321.project.retrieval;

import hk.ust.cse.comp4321.project.crawl.DocumentRecord;
import hk.ust.cse.comp4321.project.index.RecordIndex;
import hk.ust.cse.comp4321.project.util.NLPUtil;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.rocksdb.RocksDBException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Retriever {
    public static PriorityQueue<Pair<Double, DocumentRecord>> search(String queryInput, boolean exactPhraseSearch) {
        // Find phrase search terms
        List<String> textInQuotes = new ArrayList<>();
        Pattern pattern = Pattern.compile("\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(queryInput);
        while (matcher.find()) {
            textInQuotes.add(matcher.group(1)); // group(1) is the content without quotes
        }

        // Parse all words in quotes
        List<List<String>> unstemmedPhrases = textInQuotes.stream()
            .map(item -> NLPUtil.standardizeWords(NLPUtil.extractWords(item)))
            .toList();
        System.out.println("phrases: " + unstemmedPhrases);

        List<List<String>> stemmedPhrases = unstemmedPhrases.stream()
            .map(NLPUtil::removeStopwordsAndStem)
            .toList();
        System.out.println("stemmedPhrases: " + stemmedPhrases);

        List<String> excludedTerms = extractExcludedTerms(queryInput);
        System.out.println("excludedTerms: " + excludedTerms);

        // Remove negated terms from search query
        ArrayList<String> searchQuery = new ArrayList<>(
            NLPUtil.removeStopwordsAndStem(NLPUtil.standardizeWords(NLPUtil.extractWords(queryInput)))
        );
        for (String term : excludedTerms){
            searchQuery.remove(term);
        }
        System.out.println("searchQuery: " + searchQuery);

        try {
            List<DocumentRecord> records = RecordIndex.getInstance()
                .stream()
                .map(Map.Entry::getValue)
                // Filter out docs that don't match in phrase search
                .filter(record -> webpageContainsAllPhrases(record, unstemmedPhrases, stemmedPhrases, exactPhraseSearch))
                // Filter out docs that contain excluded terms
                .filter(record -> webpageDoesNotContainExcludedTerms(record, excludedTerms))
                .toList();

            PriorityQueue<Pair<Double, DocumentRecord>> similarityScores = rankDocsByDescendingSimilarityToQuery(searchQuery, records);
            return similarityScores;

        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<String> extractExcludedTerms(String queryInput){
        List<String> excludedTerms = new ArrayList<>();
        String[] tokens = queryInput.split("\\s+");

        for (String token : tokens) {
            if (token.startsWith("-") && token.length() > 1) {
                // Remove leading hyphen
                excludedTerms.add(token.substring(1));
            }
        }

        return NLPUtil.removeStopwordsAndStem(NLPUtil.standardizeWords(excludedTerms));
    }

    private static PriorityQueue<Pair<Double, DocumentRecord>> rankDocsByDescendingSimilarityToQuery(List<String> searchQuery, List<DocumentRecord> records){
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
            double totalScore = 0.5 * titleSimilarityScore + bodySimilarityScore;
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

    private static boolean webpageContainsAllPhrases(DocumentRecord record,
                                                     List<List<String>> unstemmedPhrases,
                                                     List<List<String>> stemmedPhrases,
                                                     boolean exactPhraseSearch){
        // Exact phrase search
        if (exactPhraseSearch){
            for (List<String> phrase : unstemmedPhrases){
                if (webpageSectionDoesNotContainsPhrase(record.titleWordLocations(), phrase) &&
                    webpageSectionDoesNotContainsPhrase(record.bodyWordLocations(), phrase)) return false;
            }
        }

        // Inexact phrase search with stemmed doc words
        else {
            for (List<String> phrase : stemmedPhrases){
                if (webpageSectionDoesNotContainsPhrase(record.stemmedTitleWordLocations(), phrase) &&
                    webpageSectionDoesNotContainsPhrase(record.stemmedBodyWordLocations(), phrase)) return false;
            }
        }

        return true;
    }

    private static boolean webpageSectionDoesNotContainsPhrase(Map<String, Set<Long>> wordLocations, List<String> phrase){
        if (phrase.isEmpty()) return false;

        List<Set<Long>> phraseWordLocationsInDoc = new ArrayList<>();  // e.g. [(45, 123), (46), (47, 1239)]
        for (String word : phrase){
            // Doc does not contain all the words in the phrase
            if (!wordLocations.containsKey(word))
                return true;

            phraseWordLocationsInDoc.add(wordLocations.get(word));
        }

        // Use DP
        // Problem statement: Given a list of location sets, determine whether there exists an incrementing sequence across the sets.
        // [(45, 123), (46), (47, 1239)] -> Exists: [45, 46, 47]
        // [(1, 2), (3, 4), (6)] -> Does not exist

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
                return true;
            }
            reachable = nextReachable;
        }

        // Successfully reached the last set
        return false;
    }

    private static boolean webpageDoesNotContainExcludedTerms(DocumentRecord record, List<String> excludedTerms){
        var titleTerms = record.titleTermFrequencies().keySet();
        var bodyTerms = record.bodyTermFrequencies().keySet();

        for (String term : excludedTerms){
            if (titleTerms.contains(term) || bodyTerms.contains(term))
                return false;
        }
        return true;
    }
}
