package parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import exception.ApiResponseException;

public class JsonResponseParser {
    public JsonObject parse(String responseBody) {
        JsonElement parsed = JsonParser.parseString(responseBody);

        if (!parsed.isJsonObject()) {
            throw new ApiResponseException("Ожидался JSON-объект в ответе API");
        }

        return parsed.getAsJsonObject();
    }

    public JsonObject getRequiredObject(JsonObject parent, String fieldName) {
        JsonElement element = parent.get(fieldName);

        if (element == null || !element.isJsonObject()) {
            throw new ApiResponseException("В ответе отсутствует объект или указан невеный тип: " + fieldName);
        }

        return element.getAsJsonObject();
    }

    public JsonArray getRequiredArray(JsonObject parent, String fieldName) {
        JsonElement element = parent.get(fieldName);

        if (element == null || !element.isJsonArray()) {
            throw new ApiResponseException("В ответе отсутсвует массив или указан неверный тип: " + fieldName);
        }

        return element.getAsJsonArray();
    }

    public String getRequiredString(JsonObject parent, String fieldName) {
        JsonElement element = parent.get(fieldName);

        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            throw new ApiResponseException("В ответе отсутствует строка или указан неверный тип: " + fieldName);
        }

        return element.getAsString();
    }

    public double getRequiredDouble(JsonObject parent, String fieldName) {
        JsonElement element = parent.get(fieldName);

        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            throw new ApiResponseException("В ответе отсутствует число или указан неверный тип: " + fieldName);
        }

        return element.getAsDouble();
    }
}
