package hk.ust.cse.comp4321.project.database;

import hk.ust.cse.comp4321.project.index.DocumentIndex;
import hk.ust.cse.comp4321.project.index.BodyInvertedIndex;
import hk.ust.cse.comp4321.project.index.RecordIndex;
import hk.ust.cse.comp4321.project.index.TitleInvertedIndex;
import picocli.CommandLine.*;


@Command(name = "db_view", description = "Prints out an entire index.")
public class DBViewCommand implements Runnable {
    @Option(names = {"--index"}, description = "The index to print out: document / title / body / record", defaultValue = "document")
    private String indexName;

    @Option(names = {"--limit"}, description = "The maximum number of db entries to display", defaultValue = "99999999")
    private Long maxItemsToDisplay;

    @Override
    public void run() {
        RocksDatabaseMap index;

        try {
            index = switch (indexName) {
                case "document" -> DocumentIndex.getInstance();
                case "title" -> TitleInvertedIndex.getInstance();
                case "body" -> BodyInvertedIndex.getInstance();
                case "record" -> RecordIndex.getInstance();
                default -> throw new Exception("Invalid index name!");
            };

            index.stream().limit(maxItemsToDisplay).forEach(item -> System.out.println(item + "\n"));
            index.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
