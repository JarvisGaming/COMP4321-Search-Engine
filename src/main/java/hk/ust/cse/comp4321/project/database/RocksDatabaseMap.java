package hk.ust.cse.comp4321.project.database;

import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.File;
import java.io.Serializable;
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

        File file = getDatabaseFile(databaseName);
        String path = file.getAbsolutePath();

        System.out.println("Loading database from: " + path);

        if (!file.exists() && !file.mkdirs())
            throw new RuntimeException("failed to create directory for database: " + path);

        database = RocksDB.open(databaseOptions, path);
    }

    // Reconcile Piccoli commands and tomcat server running from different locations
    public static File getDatabaseFile(String databaseName) {
        // Start at the current working directory
        File current = new File(System.getProperty("user.dir"));

        // Search upwards until we find the folder containing 'build.gradle' or 'database'
        while (current != null) {
            File potentialDbFolder = new File(current, "database");
            if (potentialDbFolder.exists() && potentialDbFolder.isDirectory()) {
                return new File(potentialDbFolder, databaseName);
            }
            current = current.getParentFile();
        }

        // Fallback to basic relative path if nothing found
        return new File("database", databaseName);
    }

    public void close() {
        database.close();
    }

    public void delete(K key) throws SerializationException, RocksDBException {
        database.delete(SerializationUtils.serialize(key));
    }

    public Optional<V> get(K key) throws ClassCastException, SerializationException, RocksDBException {
        return Optional.ofNullable(database.get(SerializationUtils.serialize(key))).map(SerializationUtils::deserialize);
    }

    public void put(K key, V value) throws SerializationException, RocksDBException {
        database.put(SerializationUtils.serialize(key), SerializationUtils.serialize(value));
    }

    public Stream<Map.Entry<K, V>> stream() {
        RocksSpliterator<K, V> iterator = new RocksSpliterator<>(database.newIterator());
        return StreamSupport.stream(iterator, false).onClose(iterator::close);
    }
}
