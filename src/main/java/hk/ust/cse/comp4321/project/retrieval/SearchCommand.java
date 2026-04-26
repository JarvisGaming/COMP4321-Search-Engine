package hk.ust.cse.comp4321.project.retrieval;

import picocli.CommandLine.Command;

import java.util.*;

@Command(name = "search", description = "Performs a search query.")
public class SearchCommand implements Runnable {
    final Scanner scanner = new Scanner(System.in);

    @Override
    public void run() {
        System.out.println("Enter query: ");
        String queryInput = scanner.nextLine();  // Read user input

        Retriever.search(queryInput);
    }
}
