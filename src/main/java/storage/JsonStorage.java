package storage;

import com.google.gson.*;
import model.AggregatedRecord;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonStorage implements RecordStorage {
    private final Gson gson_;

    public JsonStorage(Gson gson) {
        gson_ = gson;
    }

    @Override
    public void save(Path path, List <AggregatedRecord > records, boolean append) throws IOException {
        List< AggregatedRecord > allRecords = new ArrayList< >();

        if (append && Files.exists(path) && Files.size(path) > 0) {
            List< AggregatedRecord > oldRecords = read(path);
            allRecords.addAll(oldRecords);
        }
        allRecords.addAll(records);

        JsonArray array = new JsonArray();
        for (AggregatedRecord record : allRecords) {
            JsonObject object = toJsonObject(record);
            array.add(object);
        }
        String json = gson_.toJson(array);

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            writer.write(json);
        }
    }

    @Override
    public List< AggregatedRecord > read(Path path) throws IOException {
        List< AggregatedRecord > records = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);

            if (!root.isJsonArray()) {
                throw new IOException("В JSON-файле ожидался массив записей");
            }

            for (JsonElement element : root.getAsJsonArray()) {
                if (!element.isJsonObject()) {
                    throw new IOException("В массиве ожидался объект записи");
                }

                AggregatedRecord record = fromJsonObject(element.getAsJsonObject());
                records.add(record);
            }
        } catch (JsonParseException | DateTimeParseException e) {
            throw new IOException("Некорректные данные в JSON-файле", e);
        }
        return records;
    }

    private JsonObject toJsonObject(AggregatedRecord record) {
        JsonObject object = new JsonObject();

        object.addProperty("source", record.getSource());
        object.addProperty("category", record.getCategory());
        object.addProperty("title", record.getTitle());
        object.addProperty("fetchedAt", record.getFetchedAt().toString());

        object.add("attributes", gson_.toJsonTree(record.getAttributes()));

        return object;
    }

    private AggregatedRecord fromJsonObject(JsonObject object) throws IOException {
        String source = getRequiredString(object, "source");
        String category = getRequiredString(object, "category");
        String title = getRequiredString(object, "title");

        String time = getRequiredString(object, "fetchedAt");
        Instant fetchedAt = Instant.parse(time);

        JsonElement attributesElement = object.get("attributes");

        if (attributesElement == null || !attributesElement.isJsonObject()) {
            throw new IOException("В JSON-файле отсутствует или некорректно поле attributes");
        }

        JsonObject attributesObject = attributesElement.getAsJsonObject();
        Map< String, String > attributes = new HashMap< >();

        for (Map.Entry< String, JsonElement > entry : attributesObject.entrySet()) {
            String key = entry.getKey();
            String value = getRequiredString(attributesObject, key);
            attributes.put(key, value);
        }

        return new AggregatedRecord(source, category, title, fetchedAt, attributes);
    }

    private String getRequiredString(JsonObject object, String field) throws IOException {
        JsonElement element = object.get(field);

        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            throw new IOException("В JSON-файле отсутствует или некорректно поле: " + field);
        }

        return element.getAsString();
    }
}
