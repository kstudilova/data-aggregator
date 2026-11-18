package parser;

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
            throw new ApiResponseException(" ответе отсутствует объект или указан невеный тип: " + fieldName);
        }
        return element.getAsJsonObject();
    }
}
