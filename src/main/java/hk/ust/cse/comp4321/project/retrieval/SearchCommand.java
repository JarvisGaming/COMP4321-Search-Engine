package hk.ust.cse.comp4321.project.retrieval;

import hk.ust.cse.comp4321.project.crawl.DocumentRecord;
import org.apache.commons.lang3.tuple.Pair;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.*;

@Command(name = "search", description = "Performs a search query.")
public class SearchCommand implements Runnable {
    @CommandLine.Option(names = {"--exact"}, description = "Whether to perform exact phrase search", defaultValue = "false")
    private boolean exactPhraseSearch;

    final Scanner scanner = new Scanner(System.in);

    @Override
    public void run() {
        System.out.println("Enter query: ");
        String queryInput = scanner.nextLine();  // Read user input
        System.out.println(exactPhraseSearch);
        PriorityQueue<Pair<Double, DocumentRecord>> similarityScores = Retriever.search(queryInput, exactPhraseSearch);

        System.out.println("similarityScores");
        while (!similarityScores.isEmpty()) {
            var pair = similarityScores.poll();
            System.out.println(pair.getLeft() + ": " + pair.getRight().url());
        }
    }
}
