package hk.ust.cse.comp4321.project.index;

import hk.ust.cse.comp4321.project.crawl.DocumentRecord;
import hk.ust.cse.comp4321.project.database.RocksDatabaseMap;
import org.apache.commons.lang3.tuple.Pair;
import org.rocksdb.RocksDBException;

import java.util.*;
import java.util.stream.Collectors;

public class Indexer {
    public static Map<String, Long> getDocumentFrequencies(RocksDatabaseMap<String, TreeSet<Pair<Integer, Long>>> invertedIndex){
        return invertedIndex.stream()
            .map(entry -> {
                String term = entry.getKey();
                TreeSet<Pair<Integer, Long>> postings = entry.getValue();
                long df = postings.stream()
                    .map(Pair::getLeft)
                    .distinct()
                    .count();
                return new AbstractMap.SimpleEntry<>(term, df);
            })
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static void populateTermWeights() throws RocksDBException {
        TitleInvertedIndex titleInvertedIndex = TitleInvertedIndex.getInstance();
        BodyInvertedIndex bodyInvertedIndex = BodyInvertedIndex.getInstance();

        Map<String, Long> titleDFs = getDocumentFrequencies(titleInvertedIndex);
        Map<String, Long> bodyDFs = getDocumentFrequencies(bodyInvertedIndex);

        titleInvertedIndex.close();
        bodyInvertedIndex.close();

        // Retrieve all records
        RecordIndex recordIndex = RecordIndex.getInstance();
        List<Map.Entry<Integer, DocumentRecord>> records = recordIndex.stream().toList();

        int numRecords = records.size();
        for (Map.Entry<Integer, DocumentRecord> entry : records) {
            int documentID = entry.getKey();
            DocumentRecord record = entry.getValue();

            computeAndStoreWeights(record.titleTermFrequencies(),
                titleDFs,
                numRecords,
                record.titleTermWeights());
            computeAndStoreWeights(record.bodyTermFrequencies(),
                bodyDFs,
                numRecords,
                record.bodyTermWeights());

            // Update RecordIndex with new record with term weights
            recordIndex.put(documentID, record);
        }

        recordIndex.close();
    }

    private static void computeAndStoreWeights(Map<String, Long> termFrequencies,
                                               Map<String, Long> dfs,
                                               int numRecords,
                                               Map<String, Double> targetWeights) {
        // Empty out targetWeights first, in case of webpage content changes
        targetWeights.clear();

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
