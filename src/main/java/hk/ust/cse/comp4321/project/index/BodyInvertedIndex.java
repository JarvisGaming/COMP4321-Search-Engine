package hk.ust.cse.comp4321.project.index;

import hk.ust.cse.comp4321.project.database.RocksDatabaseMap;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.rocksdb.RocksDBException;

import java.util.Optional;
import java.util.TreeSet;


public class BodyInvertedIndex extends RocksDatabaseMap<String, TreeSet<Pair<Integer, Long>>> {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static Optional<BodyInvertedIndex> INSTANCE = Optional.empty();

    public static synchronized @NotNull BodyInvertedIndex getInstance() throws RocksDBException {
        if (INSTANCE.isPresent())
            return INSTANCE.get();

        INSTANCE = Optional.of(new BodyInvertedIndex());
        return INSTANCE.get();
    }

    private BodyInvertedIndex() throws RocksDBException {
        super("BodyInvertedIndex");
    }
}
