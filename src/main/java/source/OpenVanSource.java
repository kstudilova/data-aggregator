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

        JsonElement success = root.get("success");
        if (success == null || !success.isJsonPrimitive() || !success.getAsJsonPrimitive().isBoolean()) {
            throw new ApiResponseException("В ответе OpenVan отсутствует или некорректно поле success");
        }
        if (!success.getAsBoolean()) {
            throw new ApiResponseException("OpenVan сообщил об ошибке");
        }

        JsonObject data = jsonParser_.getRequiredObject(root, "data");

        String countryCode = country_.toUpperCase(Locale.ROOT);
        JsonObject countryData = jsonParser_.getRequiredObject(data, countryCode);

        JsonObject prices = jsonParser_.getRequiredObject(countryData, "prices");

        JsonObject currencies = jsonParser_.getRequiredObject(countryData, "currencies");
        JsonObject units = jsonParser_.getRequiredObject(countryData, "units");

        List< AggregatedRecord > records = new ArrayList< >();
        Instant fetchedAt = Instant.now();

        for (Map.Entry< String, JsonElement > entry : prices.entrySet()) {
            String fuelType = entry.getKey();
            JsonElement priceElement = entry.getValue();

            if (priceElement.isJsonNull()) {
                continue;
            }

            if (!priceElement.isJsonPrimitive() || !priceElement.getAsJsonPrimitive().isNumber()) {
                throw new ApiResponseException("Некорректная цена для: " + fuelType);
            }
            String price = priceElement.getAsString();

            JsonElement currencyElement = currencies.get(fuelType);
            JsonElement unitElement = units.get(fuelType);
            if (currencyElement == null || currencyElement.isJsonNull() || unitElement == null || unitElement.isJsonNull()) {
                throw new ApiResponseException("Не указаны валюта или единица измерения для: " + fuelType);
            }
            if (!currencyElement.isJsonPrimitive() || !currencyElement.getAsJsonPrimitive().isString() || !unitElement.isJsonPrimitive() || !unitElement.getAsJsonPrimitive().isString()) {
                throw new ApiResponseException("Некорректная валюта или единица измерения для: " + fuelType);
            }

            String currency = currencyElement.getAsString();
            String unit = unitElement.getAsString();

            Map< String, String > attributes = new HashMap< >();
            attributes.put("country", countryCode);
            attributes.put("fuelType", fuelType);
            attributes.put("price", price);
            attributes.put("currency", currency);
            attributes.put("unit", unit);

            AggregatedRecord record = new AggregatedRecord(getName(), "fuel", fuelType + " - " + countryCode, fetchedAt, attributes);

            records.add(record);
        }

        if (records.isEmpty()) {
            throw new ApiResponseException("Нет доступных цен на топливо для страны " + countryCode);
        }
        return records;
    }
}
