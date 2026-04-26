package hk.ust.cse.comp4321.project.index;

import hk.ust.cse.comp4321.project.crawl.DocumentRecord;

import java.util.*;
import java.util.stream.Collectors;

public class Indexer {
    public static Map<String, Long> getDocumentFrequencies(List<Set<String>> perDocumentTerms){
        HashMap<String, Long> dfs = new HashMap<>();
        HashSet<String> uniqueTerms = perDocumentTerms.stream()
            .flatMap(Set::stream)
            .collect(Collectors.toCollection(HashSet::new));

        for (String term : uniqueTerms){
            long df = 0;
            for (Set<String> documentSpecificTerms : perDocumentTerms){
                if (documentSpecificTerms.contains(term))
                    df++;
            }
            dfs.put(term, df);
        }

        return dfs;
    }

    public static void populateTermWeights(List<DocumentRecord> records,
                                           Map<String, Long> titleDFs,
                                           Map<String, Long> bodyDFs) {
        int numRecords = records.size();
        for (DocumentRecord record : records) {
            computeAndStoreWeights(record.titleTermFrequencies(),
                titleDFs,
                numRecords,
                record.titleTermWeights());
            computeAndStoreWeights(record.bodyTermFrequencies(),
                bodyDFs,
                numRecords,
                record.bodyTermWeights());
        }
    }

    private static void computeAndStoreWeights(Map<String, Long> termFrequencies,
                                               Map<String, Long> dfs,
                                               int numRecords,
                                               Map<String, Double> targetWeights) {
        if (termFrequencies.isEmpty())
            return;

        long maxTf = Collections.max(termFrequencies.values());
        for (Map.Entry<String, Long> entry : termFrequencies.entrySet()) {
            String term = entry.getKey();
            long tf = entry.getValue();
            long df = dfs.get(term);

            // Manually perform log base 2
            double idf = Math.log((double) numRecords / df) / Math.log(2);

            double weight = tf * idf / maxTf;
            targetWeights.put(term, weight);
        }
    }
}
