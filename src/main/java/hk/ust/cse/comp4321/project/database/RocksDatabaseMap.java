package hk.ust.cse.comp4321.project.database;

import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class RocksDatabaseMap<K extends Serializable, V extends Serializable> {
    private final RocksDB database;

    public RocksDatabaseMap(String databaseName) throws RocksDBException {
        RocksDB.loadLibrary();

        Options databaseOptions = new Options();
        databaseOptions.setCreateIfMissing(true);
        //important: please change it to your absolute path!
        //String databasePath_absolute_path = "/Users/yiuwah/Library/Mobile Documents/com~apple~CloudDocs/HKUST/2526S/COMP 4321/Project/phase2/COMP4321-Search-Engine/database";
        String databasePath = "../database";
        Path path = Paths.get(databasePath, databaseName);
        File file = path.toFile();
        if (!file.exists() && !file.mkdirs())
            throw new RuntimeException("failed to create directory for database: " + databasePath);

        database = RocksDB.open(databaseOptions, path.toString());
    }

    public Optional<V> get(K key) throws ClassCastException, SerializationException, RocksDBException {
        return Optional.ofNullable(database.get(SerializationUtils.serialize(key))).map(SerializationUtils::deserialize);
    }

    public Stream<Map.Entry<K, V>> stream() {
        RocksSpliterator<K, V> iterator = new RocksSpliterator<>(database.newIterator());
        return StreamSupport.stream(iterator, false).onClose(iterator::close);
    }

    public void put(K key, V value) throws SerializationException, RocksDBException {
        database.put(SerializationUtils.serialize(key), SerializationUtils.serialize(value));
    }

    public void delete(K key) throws SerializationException, RocksDBException {
        database.delete(SerializationUtils.serialize(key));
    }
}
