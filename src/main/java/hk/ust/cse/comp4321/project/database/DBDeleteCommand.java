package hk.ust.cse.comp4321.project.database;

import picocli.CommandLine.Command;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import org.apache.commons.io.FileUtils;

@Command(name = "db_delete", description = "Deletes all database files.")
public class DBDeleteCommand implements Runnable {
    @Override
    public void run() {
        String databasePath = Optional.ofNullable(System.getenv("COMP4321_DB_DIR")).orElse("database");
        try {
            FileUtils.deleteDirectory(new File(databasePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
