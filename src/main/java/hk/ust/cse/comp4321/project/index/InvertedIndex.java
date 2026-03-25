package hk.ust.cse.comp4321.project.index;

import hk.ust.cse.comp4321.project.database.RocksDatabaseMap;
import org.apache.commons.lang3.tuple.Pair;
import org.rocksdb.RocksDBException;

import java.util.TreeSet;

public class InvertedIndex extends RocksDatabaseMap<String, TreeSet<Pair<Integer, Integer>>> {
    private InvertedIndex(String databaseName) throws RocksDBException {
        super(databaseName);
    }
}
