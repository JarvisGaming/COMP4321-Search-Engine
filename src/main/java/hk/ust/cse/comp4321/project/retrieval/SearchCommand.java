package hk.ust.cse.comp4321.project.retrieval;

import hk.ust.cse.comp4321.project.util.NLPUtil;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.Collections;
import java.util.List;

@Command(name = "search", description = "Performs a search query.")
public class SearchCommand implements Runnable {
    @CommandLine.Parameters(description = "Any number of words to form the search query.")
    private List<String> searchQuery = Collections.emptyList();

    @Override
    public void run() {
        System.out.println(NLPUtil.standardizeWords(searchQuery));
        // Eventually, keep track of stopword locations in query to enable phrase search

        // Calculate term weights of all terms in each document first, and store them in DocumentRecord
        // You also need to create parent links for each document, for whatever reason

        // Get tf idf scores
    }
}
