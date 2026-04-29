package hk.ust.cse.comp4321.project.index;

import hk.ust.cse.comp4321.project.database.RocksDatabaseMap;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.rocksdb.RocksDBException;

import java.util.Optional;
import java.util.TreeSet;


public class TitleInvertedIndex extends RocksDatabaseMap<String, TreeSet<Pair<Integer, Long>>> {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static Optional<TitleInvertedIndex> INSTANCE = Optional.empty();

    public static synchronized @NotNull TitleInvertedIndex getInstance() throws RocksDBException {
        if (INSTANCE.isPresent())
            return INSTANCE.get();

        INSTANCE = Optional.of(new TitleInvertedIndex());
        return INSTANCE.get();
    }

    private TitleInvertedIndex() throws RocksDBException {
        super("TitleInvertedIndex");
    }
}
