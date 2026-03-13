import java.time.LocalDateTime;
import java.util.ArrayList;

public record HTMLPage(
        String url,
        String title,
        LocalDateTime lastModified,
        String text,
        int pageSize,
        ArrayList<String> childUrls
) {
}
