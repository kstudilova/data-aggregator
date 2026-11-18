package source;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import exception.ApiResponseException;
import http.HttpService;
import model.AggregatedRecord;
import parser.JsonResponseParser;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WikipediaSource implements DataSource {
    private final HttpService httpService_;
    private final String query_;
    private final JsonResponseParser jsonParser_;


    public WikipediaSource(HttpService httpService, String query, JsonResponseParser parser) {
        httpService_ = httpService;
        query_ = query;
        jsonParser_ = parser;
    }

    @Override
    public String getName() {
        return "wikipedia";
    }

    @Override
    public List< AggregatedRecord > fetch() throws IOException, InterruptedException {
        String encodedQuery = URLEncoder.encode(query_, StandardCharsets.UTF_8);
        String url = "https://ru.wikipedia.org/w/rest.php/v1/search/page?q=" + encodedQuery + "&limit=3";

        String responseBody = httpService_.get(url);
        JsonObject root = jsonParser_.parse(responseBody);

        JsonArray pages = jsonParser_.getRequiredArray(root, "pages");

        List< AggregatedRecord > records = new ArrayList< >();
        Instant fetchedAt = Instant.now();

        for (JsonElement element : pages) {
            if (!element.isJsonObject()) {
                throw new ApiResponseException("В массиве pages ожидался объект статьи");
            }
            JsonObject page = element.getAsJsonObject();

            String title = jsonParser_.getRequiredString(page, "title");
            String key = jsonParser_.getRequiredString(page, "key");

            Map< String, String > attributes = new HashMap< >();
            attributes.put("key", key);

            JsonElement descriptionElement = page.get("description");
            if (descriptionElement != null && !descriptionElement.isJsonNull()) {
                String description = jsonParser_.getRequiredString(page, "description");
                attributes.put("description", description);
            }

            JsonElement idElement = page.get("id");
            if (idElement == null || !idElement.isJsonPrimitive() || !idElement.getAsJsonPrimitive().isNumber()) {
                throw new ApiResponseException("В ответе Wikipedia отсутствует или некорректно поле id");
            }
            attributes.put("id", idElement.getAsString());

            AggregatedRecord record = new AggregatedRecord(getName(), "place", title, fetchedAt, attributes);
            records.add(record);
        }
        return records;
    }

}
