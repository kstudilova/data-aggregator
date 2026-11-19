package storage;

import model.AggregatedRecord;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface RecordStorage {
    void save(Path path, List< AggregatedRecord > records, boolean append) throws IOException;

    List< AggregatedRecord > read(Path path) throws IOException;
}
