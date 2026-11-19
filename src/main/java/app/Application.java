package app;

import cli.CliParser;
import cli.ConsoleApplication;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import exception.ApiResponseException;
import exception.CliException;
import exception.RegistryException;
import http.HttpService;
import parser.JsonResponseParser;
import service.DataAggregator;
import source.*;
import storage.CsvStorage;
import storage.JsonStorage;
import storage.StorageRegistry;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class Application {
    public void run(String[] args) {
        HttpService httpService = new HttpService();
        JsonResponseParser jsonParser = new JsonResponseParser();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        SourceRegistry sourceRegistry = new SourceRegistry(List.of(
                new OpenVanSource(httpService, "ru", jsonParser),
                new WikipediaSource(httpService,"Казанский собор", jsonParser),
                new OsrmSource(httpService, jsonParser, 30.3158, 59.9391, 30.3609, 59.9311)
        ));

        StorageRegistry storageRegistry = new StorageRegistry(Map.of("csv", new CsvStorage(gson), "json", new JsonStorage(gson)));

        DataAggregator aggregator = new DataAggregator();

        ConsoleApplication application = new ConsoleApplication(new CliParser(), sourceRegistry, storageRegistry, aggregator);

        try {
            application.run(args);
        } catch (CliException e) {
            System.err.println("Ошибка аргументов: " + e.getMessage());
        } catch (RegistryException e) {
            System.err.println("Ошибка выбора: " + e.getMessage());
        } catch (ApiResponseException e) {
            System.err.println("Ошибка ответа API: " + e.getMessage());
        } catch (JsonParseException e) {
            System.err.println("Ошибка разбора JSON: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Некорректное значение: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Ошибка ввода-вывода: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Выполнение прервано");
        }
    }
}