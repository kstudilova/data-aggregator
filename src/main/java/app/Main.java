package app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import exception.ApiResponseException;
import http.HttpService;
import model.AggregatedRecord;
import parser.JsonResponseParser;
import service.DataAggregator;
import source.DataSource;
import source.OpenVanSource;
import source.OsrmSource;
import source.WikipediaSource;
import storage.CsvStorage;
import storage.JsonStorage;
import storage.RecordStorage;

public class Main {
    public static void main(String[] args) {
        new Application().run(args);
    }
}