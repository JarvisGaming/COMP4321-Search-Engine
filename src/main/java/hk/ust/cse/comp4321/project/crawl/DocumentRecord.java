package hk.ust.cse.comp4321.project.crawl;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;


public record DocumentRecord(
        String title,
        URL url,
        LocalDateTime lastModificationTimestamp,
        int pageSize,
        List<URL> childURLs
) {}
