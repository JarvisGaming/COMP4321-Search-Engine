package hk.ust.cse.comp4321.project.retrieval;

import hk.ust.cse.comp4321.project.crawl.DocumentRecord;
import org.apache.commons.lang3.tuple.Pair;

import java.util.PriorityQueue;

public class SearchService {
    // 建立一個靜態方法方便 JSP 直接呼叫

    public static PriorityQueue<Pair<Double, DocumentRecord>> getQuickResult(String query) {
        SearchCommand temp = new SearchCommand();
        return temp.query(query);
    }
}
