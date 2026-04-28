package hk.ust.cse.comp4321.project.retrieval;

import hk.ust.cse.comp4321.project.crawl.DocumentRecord;
import org.apache.commons.lang3.tuple.Pair;
import picocli.CommandLine.Command;

import java.util.*;

@Command(name = "search", description = "Performs a search query.")
public class SearchCommand implements Runnable {
    final Scanner scanner = new Scanner(System.in);

    @Override
    public void run() {
        System.out.println("Enter query: ");
        String queryInput = scanner.nextLine();  // Read user input

        PriorityQueue<Pair<Double, DocumentRecord>> similarityScores = Retriever.search(queryInput);

        System.out.println("similarityScores");
        while (!similarityScores.isEmpty()) {
            var pair = similarityScores.poll();
            System.out.println(pair.getLeft() + ": " + pair.getRight().url());
        }
    }
}
