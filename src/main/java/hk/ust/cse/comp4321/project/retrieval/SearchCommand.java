package hk.ust.cse.comp4321.project.retrieval;

import hk.ust.cse.comp4321.project.crawl.DocumentRecord;
import org.apache.commons.lang3.tuple.Pair;
import picocli.CommandLine.Command;

import java.util.*;

@Command(name = "search", description = "Performs a search query.")
public class SearchCommand implements Runnable {
    final Scanner scanner = new Scanner(System.in);
    private PriorityQueue<Pair<Double, DocumentRecord>> results;
    @Override
    public void run() {
        System.out.println("Enter query: ");
        String queryInput = scanner.nextLine();
        results = Retriever.search(queryInput);
    }

    public PriorityQueue<Pair<Double, DocumentRecord>> query(String queryInput){
            results = Retriever.search(queryInput);
            return results;
    }
    public PriorityQueue<Pair<Double, DocumentRecord>> getResults() {
        return results;
    }
}
