package hk.ust.cse.comp4321.project.crawl;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


public record DocumentRecord(
        String title,
        URL url,
        LocalDateTime lastModificationTimestamp,
        Map<String, Long> wordFrequencyTable,
        int pageSize,
        List<URL> childURLs
) {}
