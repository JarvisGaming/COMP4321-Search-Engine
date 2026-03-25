package hk.ust.cse.comp4321.project.index;

import hk.ust.cse.comp4321.project.database.RocksDatabaseMap;
import org.rocksdb.RocksDBException;


public class DocumentIndex extends RocksDatabaseMap<String, Integer> {
    private DocumentIndex(String databaseName) throws RocksDBException {
        super(databaseName);
    }
}
