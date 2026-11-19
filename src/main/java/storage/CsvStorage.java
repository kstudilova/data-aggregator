package storage;

import com.google.gson.Gson;
import model.AggregatedRecord;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class CsvStorage implements RecordStorage {
    private final Gson gson_;

    public CsvStorage(Gson gson) {
        gson_ = gson;
    }

    @Override
    public void save(Path path, List<AggregatedRecord> records, boolean append) throws IOException {
        boolean writeHeader = !append || !Files.exists(path) || Files.size(path) == 0;

        StandardOpenOption mode = append ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING;

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE, mode)) {
            if (writeHeader) {
                writer.write("source,category,title,fetchedAt,attributes");
                writer.newLine();
            }

            for (AggregatedRecord record : records) {
                String attributesJson = gson_.toJson(record.getAttributes());

                String line = String.join(",",
                        escapeCsv(record.getSource()),
                        escapeCsv(record.getCategory()),
                        escapeCsv(record.getTitle()),
                        escapeCsv(record.getFetchedAt().toString()),
                        escapeCsv(attributesJson)
                );

                writer.write(line);
                writer.newLine();
            }
        }
    }

    @Override
    public List< AggregatedRecord > read(Path path) throws IOException {
        throw new IOException("Чтение CSV пока не реализовано");
    }

    private String escapeCsv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
