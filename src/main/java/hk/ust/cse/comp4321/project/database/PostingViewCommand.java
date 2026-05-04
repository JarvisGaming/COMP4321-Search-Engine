package hk.ust.cse.comp4321.project.database;

import hk.ust.cse.comp4321.project.index.BodyInvertedIndex;
import hk.ust.cse.comp4321.project.index.TitleInvertedIndex;
import hk.ust.cse.comp4321.project.util.NLPUtil;
import org.apache.commons.lang3.tuple.Pair;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.TreeSet;


@Command(name = "postings_view", description = "Prints out the postings list of a given stem.")
public class PostingViewCommand implements Runnable {
    @Option(names = {"--index"}, description = "The index to print out: title / body", defaultValue = "body")
    private String indexName;

    @Option(names = {"--term"}, description = "The stem to list the postings of", defaultValue = "noTermSpecified!")
    private String term;

    @Override
    public void run() {
        RocksDatabaseMap<String, TreeSet<Pair<Integer, Long>>> index;

        try {
            index = switch (indexName) {
                case "title" -> TitleInvertedIndex.getInstance();
                case "body" -> BodyInvertedIndex.getInstance();
                default -> throw new Exception("Invalid inverted index name!");
            };

            String stemmedTerm = NLPUtil.stem(term);

            var res = index.get(stemmedTerm);
            if (res.isPresent())
                System.out.println(stemmedTerm + ": " + res.get());
            else System.out.println(term + " does not have postings in " + indexName + " inverted index");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
