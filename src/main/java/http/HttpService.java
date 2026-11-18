package http;

import exception.ApiResponseException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpService {
    private final HttpClient client_;

    public HttpService() {
        client_ = HttpClient.newHttpClient();
    }

    public String get(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "DataAggregator/1.0 (https://github.com/kstudilova/data-aggregator)")
                .GET()
                .build();

        HttpResponse< String > response = client_.send(request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        if (status != 200) {
            throw new ApiResponseException("Ожидался HTTP 200, получен: " + status);
        }

        String body = response.body();
        if (body.isBlank()) {
            throw new ApiResponseException("API вернул пустой ответ");
        }
        return body;
    }
}
