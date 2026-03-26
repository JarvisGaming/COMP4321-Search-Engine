package hk.ust.cse.comp4321.project.index;

import hk.ust.cse.comp4321.project.database.RocksDatabaseMap;
import org.jetbrains.annotations.NotNull;
import org.rocksdb.RocksDBException;

import java.util.Map;
import java.util.Optional;


@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class DocumentIndex extends RocksDatabaseMap<String, Integer> {
    private static Optional<DocumentIndex> INSTANCE = Optional.empty();

    private Integer nextID = null;

    public static synchronized @NotNull DocumentIndex getInstance() throws RocksDBException {
        if (INSTANCE.isPresent())
            return INSTANCE.get();

        INSTANCE = Optional.of(new DocumentIndex());
        return INSTANCE.get();
    }

    private DocumentIndex() throws RocksDBException {
        super("DocumentIndex");
    }

    public synchronized int incrementID() {
        nextID = this.nextID();
        return nextID++;
    }

    public synchronized int nextID() {
        if (nextID != null)
            return nextID;

        nextID = this.stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getValue).orElse(0);
        return nextID;
    }
}
