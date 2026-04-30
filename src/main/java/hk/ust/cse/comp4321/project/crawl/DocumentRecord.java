package hk.ust.cse.comp4321.project.crawl;

import java.io.Serializable;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;


public record DocumentRecord(
        String title,
        URL url,
        LocalDateTime lastModificationTimestamp,
        Map<String, Long> titleTermFrequencies,
        Map<String, Long> bodyTermFrequencies,
        Map<String, Double> titleTermWeights,
        Map<String, Double> bodyTermWeights,
        Map<String, Set<Long>> titleWordLocations,
        Map<String, Set<Long>> bodyWordLocations,
        Map<String, Set<Long>> stemmedTitleWordLocations,
        Map<String, Set<Long>> stemmedBodyWordLocations,
        int pageSize,
        HashSet<URL> parentURLs,
        List<URL> childURLs
) implements Serializable {
}
