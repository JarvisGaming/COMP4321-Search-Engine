package hk.ust.cse.comp4321.project.database;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.rocksdb.RocksIterator;

import java.io.Serializable;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;


public class RocksSpliterator<K extends Serializable, V extends Serializable> implements Spliterator<Map.Entry<K, V>> {
    private final RocksIterator iterator;

    public RocksSpliterator(RocksIterator iterator) {
        this.iterator = iterator;
        this.iterator.seekToFirst();
    }

    public void close() {
        this.iterator.close();
    }

    @Override
    public boolean tryAdvance(Consumer<? super Map.Entry<K, V>> action) {
        if (!this.iterator.isValid())
            return false;

        K key;
        V value;
        try {
            key = SerializationUtils.deserialize(this.iterator.key());
            value = SerializationUtils.deserialize(this.iterator.value());
        } catch (Exception ignored) {
            return false;
        }

        action.accept(Pair.of(key, value));
        iterator.next();
        return true;
    }

    @Override
    public Spliterator<Map.Entry<K, V>> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return DISTINCT | IMMUTABLE | NONNULL;
    }
}
