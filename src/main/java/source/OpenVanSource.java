package source;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import exception.ApiResponseException;
import http.HttpService;
import model.AggregatedRecord;
import parser.JsonResponseParser;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

public class OpenVanSource implements DataSource {
    private final HttpService httpService_;
    private final String country_;
    private final JsonResponseParser jsonParser_;

    public OpenVanSource(HttpService httpService, String country, JsonResponseParser parser) {
        httpService_ = httpService;
        country_ = country;
        jsonParser_ = parser;
    }

    @Override
    public String getName() {
        return "openvan";
    }

    @Override
    public List< AggregatedRecord > fetch() throws IOException, InterruptedException {
        String url = "https://openvan.camp/api/fuel/prices?country=" + country_;
        String responseBody = httpService_.get(url);

        JsonObject root = jsonParser_.parse(responseBody);
        if (!root.get("success").getAsBoolean()) {
            throw new ApiResponseException("OpenVan сообщил об ошибке");
        }

        JsonObject data = root.getAsJsonObject("data");
        if (data == null) {
            throw new ApiResponseException("В ответе OpenVan отсутствуют данные");
        }

        String countryCode = country_.toUpperCase(Locale.ROOT);
        JsonObject countryData = data.getAsJsonObject(countryCode);
        if (countryData == null) {
            throw new ApiResponseException("В ответе нет данных для страны: " + countryCode);
        }

        JsonObject prices = countryData.getAsJsonObject("prices");
        if (prices == null) {
            throw new ApiResponseException("В ответе нет цен на топливо для страны: " + countryCode);
        }

        List< AggregatedRecord > records = new ArrayList< >();
        Instant fetchedAt = Instant.now();

        for (Map.Entry< String, JsonElement > entry : prices.entrySet()) {
            String fuelType = entry.getKey();
            JsonElement priceElement = entry.getValue();

            if (priceElement.isJsonNull()) {
                continue;
            }
            String price = priceElement.getAsString();

            Map< String, String > attributes = new HashMap< >();
            attributes.put("country", countryCode);
            attributes.put("fuelType", fuelType);
            attributes.put("price", price);

            AggregatedRecord record = new AggregatedRecord(getName(), "fuel", fuelType + " - " + countryCode, fetchedAt, attributes);

            records.add(record);
        }
        return records;
    }
}
