package cli;

import exception.CliException;
import model.AggregatedRecord;
import service.DataAggregator;
import source.DataSource;
import source.SourceRegistry;
import storage.RecordStorage;
import storage.StorageRegistry;

import java.io.IOException;
import java.util.List;

public class ConsoleApplication {
    private final CliParser parser_;
    private final SourceRegistry sourceRegistry_;
    private final StorageRegistry storageRegistry_;
    private final DataAggregator aggregator_;

    public ConsoleApplication(CliParser parser, SourceRegistry sourceRegistry, StorageRegistry storageRegistry, DataAggregator aggregator) {
        parser_ = parser;
        sourceRegistry_ = sourceRegistry;
        storageRegistry_ = storageRegistry;
        aggregator_ = aggregator;
    }

    private void collect(CliOptions options) throws IOException, InterruptedException {
        List< DataSource > sources = sourceRegistry_.select(options.getSources());
        RecordStorage storage = storageRegistry_.select(options.getFormat());

        List<AggregatedRecord> records = aggregator_.collect(sources);

        storage.save(options.getPath(), records, options.isAppend());

        System.out.println("Сохранено записей: " + records.size());
        System.out.println("Файл: " + options.getPath().toAbsolutePath());
    }

    private void view(CliOptions options) throws IOException {
        RecordStorage storage = storageRegistry_.select(options.getFormat());

        List< AggregatedRecord > records = storage.read(options.getPath());

        String sourceFilter = options.getSourceFilter();
        int shown = 0;

        for (AggregatedRecord record : records) {
            if (sourceFilter != null && !sourceFilter.equals(record.getSource())) {
                continue;
            }

            printRecord(record);
            shown++;
        }

        System.out.println("Показано записей: " + shown);
    }

    public void run(String[] args) throws IOException, InterruptedException {
        if (args.length == 1 && args[0].equals("--help")) {
            printHelp();
            return;
        }

        CliOptions options = parser_.parse(args);

        switch (options.getCommand()) {
            case "collect" -> collect(options);
            case "view" -> view(options);
            default -> throw new CliException("Неизвестная команда: " + options.getCommand());
        }
    }

    private void printRecord(AggregatedRecord record) {
        System.out.println("Источник: " + record.getSource());
        System.out.println("Категория: " + record.getCategory());
        System.out.println("Название: " + record.getTitle());
        System.out.println("Время получения: " + record.getFetchedAt());
        System.out.println("Характеристики: " + record.getAttributes());
        System.out.println();
    }

    private void printHelp() {
        System.out.println("""
            Сбор данных:
              collect --sources <источники> --format <csv|json> --mode <new|append> --file <путь>

            Просмотр файла:
              view --format <csv|json> --file <путь> [--source <источник>]

            Источники: openvan, wikipedia, osrm
            Несколько источников указываются через запятую.

            Режимы записи:
              new    — создать или перезаписать файл
              append — добавить записи

            Пример:
              collect --sources openvan,wikipedia --format json --mode new --file records.json
            """);
    }
}