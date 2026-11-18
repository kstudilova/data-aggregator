package source;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import exception.ApiResponseException;
import http.HttpService;
import model.AggregatedRecord;
import parser.JsonResponseParser;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OsrmSource implements DataSource {
    private final HttpService httpService_;
    private final JsonResponseParser jsonParser_;

    private final double startLongitude_;
    private final double startLatitude_;
    private final double endLongitude_;
    private final double endLatitude_;

    public OsrmSource(HttpService httpService, JsonResponseParser parser, double startLongitude, double startLatitude, double endLongitude, double endLatitude) {
        httpService_ = httpService;
        jsonParser_ = parser;
        startLongitude_ = startLongitude;
        startLatitude_ = startLatitude;
        endLongitude_ = endLongitude;
        endLatitude_ = endLatitude;
    }

    @Override
    public String getName() {
        return "osrm";
    }

    @Override
    public List< AggregatedRecord > fetch() throws IOException, InterruptedException {
        String url = "https://router.project-osrm.org/route/v1/driving/" + startLongitude_ + "," + startLatitude_ + ";" + endLongitude_ + "," + endLatitude_ + "?overview=false&alternatives=true";
        String responseBody = httpService_.get(url);
        JsonObject root = jsonParser_.parse(responseBody);

        String code = jsonParser_.getRequiredString(root, "code");
        if (!code.equals("Ok")) {
            throw new ApiResponseException("OSRM не смог построить маршрут: " + code);
        }

        JsonArray routes = jsonParser_.getRequiredArray(root, "routes");
        if (routes.isEmpty()) {
            throw new ApiResponseException("OSRM не вернул ни одного маршрута");
        }

        JsonArray waypoints = jsonParser_.getRequiredArray(root, "waypoints");
        if (waypoints.size() != 2) {
            throw new ApiResponseException("Ожидались начальная и конечная точки маршрута");
        }

        JsonElement startElement = waypoints.get(0);
        JsonElement endElement = waypoints.get(1);

        if (!startElement.isJsonObject() || !endElement.isJsonObject()) {
            throw new ApiResponseException("В массиве waypoints ожидались объекты точек маршрута");
        }

        JsonObject startPoint = startElement.getAsJsonObject();
        JsonObject endPoint = endElement.getAsJsonObject();

        String startName = jsonParser_.getRequiredString(startPoint, "name");
        String endName = jsonParser_.getRequiredString(endPoint, "name");

        String startLabel = startName.isBlank() ? startLongitude_ + ", " + startLatitude_ : startName;
        String endLabel = endName.isBlank() ? endLongitude_ + ", " + endLatitude_ : endName;

        List< AggregatedRecord > records = new ArrayList< >();
        Instant fetchedAt = Instant.now();
        int routeNumber = 1;

        for (JsonElement routeElement : routes) {
            if (!routeElement.isJsonObject()) {
                throw new ApiResponseException("В массиве routes ожидался объект маршрута");
            }
            JsonObject route = routeElement.getAsJsonObject();

            double distance = jsonParser_.getRequiredDouble(route, "distance");
            double duration = jsonParser_.getRequiredDouble(route, "duration");

            Map<String, String> attributes = new HashMap<>();
            attributes.put("distanceMeters", Double.toString(distance));
            attributes.put("durationSeconds", Double.toString(duration));
            attributes.put("startLongitude", Double.toString(startLongitude_));
            attributes.put("startLatitude", Double.toString(startLatitude_));
            attributes.put("endLongitude", Double.toString(endLongitude_));
            attributes.put("endLatitude", Double.toString(endLatitude_));
            attributes.put("startName", startName);
            attributes.put("endName", endName);
            attributes.put("routeNumber", Integer.toString(routeNumber));

            String title = "Маршрут " + routeNumber + ": " + startLabel + " -> " + endLabel;

            AggregatedRecord record = new AggregatedRecord(getName(), "route", title, fetchedAt, attributes);
            records.add(record);
            routeNumber++;
        }
        return records;
    }
}
