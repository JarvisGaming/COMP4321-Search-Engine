package hk.ust.cse.comp4321.project.crawl;

import java.io.Serializable;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;


public record DocumentRecord(
        String title,
        URL url,
        LocalDateTime lastModificationTimestamp,
        Map<String, Long> wordFrequencyTable,
        Map<String, Long> titleFrequencyTable,
        Map<String, Set<Long>> wordLocations,
        int pageSize,
        HashSet<URL> parentURLs,
        List<URL> childURLs
) implements Serializable {
}
