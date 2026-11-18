package app;

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

import java.io.IOException;
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

        try {
            List<AggregatedRecord> records = aggregator.collect();

            System.out.println("Получено записей: " + records.size());
            System.out.println("Источники данных:");
            System.out.println("  Цены на топливо — OpenVan: https://openvan.camp/ (CC BY 4.0)");
            System.out.println("  Статьи о местах — Wikipedia: https://ru.wikipedia.org/");
            System.out.println("  Маршруты — OSRM: https://project-osrm.org/");
            System.out.println();

            for (AggregatedRecord record : records) {
                System.out.println("Источник: " + record.getSource());
                System.out.println("Категория: " + record.getCategory());
                System.out.println("Название: " + record.getTitle());
                System.out.println("Характеристики: " + record.getAttributes());
                System.out.println();
            }
        } catch (ApiResponseException e) {
            System.err.println("Ошибка ответа API: " + e.getMessage());
        } catch (JsonSyntaxException e) {
            System.err.println("Некорректный JSON: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Некорректный аргумент: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Ошибка соединения: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Запрос прерван");
        }
    }
}