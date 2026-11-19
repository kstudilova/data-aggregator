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

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        HttpService httpService = new HttpService();
        JsonResponseParser jsonParser = new JsonResponseParser();

        List< DataSource > sources = List.of(
                new OpenVanSource(httpService, "ru", jsonParser),
                new WikipediaSource(httpService,"Казанский собор", jsonParser),
                new OsrmSource(httpService, jsonParser, 30.3158, 59.9391, 30.3609, 59.9311)
        );
        DataAggregator aggregator = new DataAggregator(sources);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        RecordStorage storage = new JsonStorage(gson);
        Path path = Path.of("json-check.json");

        try {
            List<AggregatedRecord> records = aggregator.collect();

            storage.save(path, records, false);

            List<AggregatedRecord> loadedRecords = storage.read(path);

            System.out.println("Файл: " + path.toAbsolutePath());
            System.out.println("Передано на сохранение: " + records.size());
            System.out.println("Прочитано из файла: " + loadedRecords.size());

            for (AggregatedRecord record : loadedRecords) {
                System.out.println("Источник: " + record.getSource());
                System.out.println("Категория: " + record.getCategory());
                System.out.println("Название: " + record.getTitle());
                System.out.println("Время получения: " + record.getFetchedAt());
                System.out.println("Характеристики: " + record.getAttributes());
                System.out.println();
            }

            System.out.println("Получено записей: " + records.size());
            System.out.println("Источники данных:");
            System.out.println("  Цены на топливо — OpenVan: https://openvan.camp/ (CC BY 4.0)");
            System.out.println("  Статьи о местах — Wikipedia: https://ru.wikipedia.org/");
            System.out.println("  Маршруты — OSRM: https://project-osrm.org/");
            System.out.println();

            /*for (AggregatedRecord record : records) {
                System.out.println("Источник: " + record.getSource());
                System.out.println("Категория: " + record.getCategory());
                System.out.println("Название: " + record.getTitle());
                System.out.println("Характеристики: " + record.getAttributes());
                System.out.println();
            }*/
        } catch (ApiResponseException e) {
            System.err.println("Ошибка ответа API: " + e.getMessage());
        } catch (JsonSyntaxException e) {
            System.err.println("Некорректный JSON: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Некорректный аргумент: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Ошибка ввода-вывода: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Запрос прерван");
        }
    }
}