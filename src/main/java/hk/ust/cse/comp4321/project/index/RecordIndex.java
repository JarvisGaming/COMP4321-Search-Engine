package hk.ust.cse.comp4321.project.index;

import hk.ust.cse.comp4321.project.crawl.DocumentRecord;
import hk.ust.cse.comp4321.project.database.RocksDatabaseMap;
import org.jetbrains.annotations.NotNull;
import org.rocksdb.RocksDBException;

import java.util.Optional;


public class RecordIndex extends RocksDatabaseMap<Integer, DocumentRecord> {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static Optional<RecordIndex> INSTANCE = Optional.empty();

    public static synchronized @NotNull RecordIndex getInstance() throws RocksDBException {
        if (INSTANCE.isPresent())
            return INSTANCE.get();

        INSTANCE = Optional.of(new RecordIndex());
        return INSTANCE.get();
    }

    private RecordIndex() throws RocksDBException {
        super("RecordIndex");
    }
}
