package hk.ust.cse.comp4321.project.index;

import hk.ust.cse.comp4321.project.database.RocksDatabaseMap;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.rocksdb.RocksDBException;

import java.util.Optional;
import java.util.TreeSet;

public class InvertedIndex extends RocksDatabaseMap<String, TreeSet<Pair<Integer, Integer>>> {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static Optional<InvertedIndex> INSTANCE = Optional.empty();

    public static synchronized @NotNull  InvertedIndex getInstance() throws RocksDBException {
        if (INSTANCE.isPresent())
            return INSTANCE.get();

        INSTANCE = Optional.of(new InvertedIndex());
        return INSTANCE.get();
    }

    private InvertedIndex() throws RocksDBException {
        super("InvertedIndex");
    }
}
