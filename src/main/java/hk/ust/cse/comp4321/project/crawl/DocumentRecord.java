package hk.ust.cse.comp4321.project.crawl;

import java.io.Serializable;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;


public record DocumentRecord(
        String title,
        URL url,
        LocalDateTime lastModificationTimestamp,
        Map<String, Long> wordFrequencyTable,
        Map<String, Long> titleFrequencyTable,
        Map<String, Set<Long>> wordLocations,
        int pageSize,
        List<URL> childURLs
) implements Serializable {
}
