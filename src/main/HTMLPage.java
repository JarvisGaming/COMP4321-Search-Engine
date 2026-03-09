package comp4321.searchengine;

import java.time.LocalDateTime;
import java.util.ArrayList;

public record HTMLPage(
    String title,
    String url,
    LocalDateTime lastModified,
    String text,
    ArrayList<String> links
) {
    int pageSize(){
        return text.length();
    }
}
