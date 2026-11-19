package storage;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import model.AggregatedRecord;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CsvStorage implements RecordStorage {
    private final Gson gson_;

    public CsvStorage(Gson gson) {
        gson_ = gson;
    }

    @Override
    public void save(Path path, List<AggregatedRecord> records, boolean append) throws IOException {
        boolean writeHeader = !append || !Files.exists(path) || Files.size(path) == 0;

        StandardOpenOption mode = append ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING;

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE, mode); CSVPrinter printer = new CSVPrinter(writer, CSVFormat.RFC4180)) {
            if (writeHeader) {
                printer.printRecord("source", "category", "title", "fetchedAt", "attributes");
            }

            for (AggregatedRecord record : records) {
                printer.printRecord(record.getSource(), record.getCategory(), record.getTitle(), record.getFetchedAt().toString(), gson_.toJson(record.getAttributes()));
            }
        }
    }

    @Override
    public List< AggregatedRecord > read(Path path) throws IOException {
        List< AggregatedRecord > records = new ArrayList<>();

        CSVFormat format = CSVFormat.RFC4180.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .get();

        Type attributesType = new TypeToken< Map< String, String > >() { }.getType();

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8); CSVParser parser = format.parse(reader)) {

            List< String > expectedHeader = List.of("source", "category", "title", "fetchedAt", "attributes");

            if (!parser.getHeaderNames().equals(expectedHeader)) {
                throw new IOException("Некорректный заголовок CSV");
            }

            for (CSVRecord row : parser.getRecords()) {
                if (!row.isConsistent()) {
                    throw new IOException("Неверное число столбцов в записи " + row.getRecordNumber());
                }

                String source = row.get("source");
                String category = row.get("category");
                String title = row.get("title");
                Instant fetchedAt = Instant.parse(row.get("fetchedAt"));

                Map< String, String > attributes = gson_.fromJson(row.get("attributes"), attributesType);

                if (attributes == null || attributes.containsValue(null)) {
                    throw new IOException("Некорректные характеристики в записи " + row.getRecordNumber());
                }

                AggregatedRecord record = new AggregatedRecord(source, category, title, fetchedAt, attributes);

                records.add(record);
            }
        } catch (JsonParseException | DateTimeParseException e) {
            throw new IOException("Некорректные данные в CSV: " + e.getMessage(), e);
        }

        return records;
    }
}
