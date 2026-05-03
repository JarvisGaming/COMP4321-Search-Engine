package hk.ust.cse.comp4321.project.index;

import hk.ust.cse.comp4321.project.database.RocksDatabaseMap;
import org.jetbrains.annotations.NotNull;
import org.rocksdb.RocksDBException;

import java.util.Map;
import java.util.Optional;


@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class DocumentIndex extends RocksDatabaseMap<String, Integer> {
    private static Optional<DocumentIndex> INSTANCE = Optional.empty();
    private static final String NEXT_ID_KEY = "__next_document_id__";

    public static synchronized @NotNull DocumentIndex getInstance() throws RocksDBException {
        if (INSTANCE.isPresent()) return INSTANCE.get();

        INSTANCE = Optional.of(new DocumentIndex());
        return INSTANCE.get();
    }

    private DocumentIndex() throws RocksDBException {
        super("DocumentIndex");
    }

    /**
     * Returns the next available document ID and persists it.
     */
    public synchronized int incrementID() throws RocksDBException {
        int next = getNextId();
        put(NEXT_ID_KEY, next + 1);
        return next;
    }

    private int getNextId() throws RocksDBException {
        Optional<Integer> persisted = get(NEXT_ID_KEY);
        if (persisted.isPresent()) {
            return persisted.get();
        }

        int maxExisting = stream()
                .filter(e -> !NEXT_ID_KEY.equals(e.getKey()))   // ignore counter key
                .map(Map.Entry::getValue)
                .max(Integer::compareTo)
                .orElse(0);

        put(NEXT_ID_KEY, maxExisting + 1);
        return maxExisting + 1;
    }
}
