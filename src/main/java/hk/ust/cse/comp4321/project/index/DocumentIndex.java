package hk.ust.cse.comp4321.project.index;

import hk.ust.cse.comp4321.project.database.RocksDatabaseMap;
import org.jetbrains.annotations.NotNull;
import org.rocksdb.RocksDBException;

import java.util.Optional;


public class DocumentIndex extends RocksDatabaseMap<String, Integer> {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static Optional<DocumentIndex> INSTANCE = Optional.empty();

    public static synchronized @NotNull DocumentIndex getInstance() throws RocksDBException {
        if (INSTANCE.isPresent())
            return INSTANCE.get();

        INSTANCE = Optional.of(new DocumentIndex());
        return INSTANCE.get();
    }

    private DocumentIndex() throws RocksDBException {
        super("DocumentIndex");
    }
}
