package cli;

import exception.CliException;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.*;

public class CliParser {
    public CliOptions parse(String[] args) {
        if (args.length == 0) {
            throw new CliException("Укажите команду: collect или view");
        }

        String command = args[0];

        if (!command.equals("collect") && !command.equals("view")) {
            throw new CliException("Неизвестная команда: " + command);
        }

        Map< String, String > parameters = parseParameters(args);

        Set< String > allowedParameters = command.equals("collect") ? Set.of("--sources", "--format", "--mode", "--file") : Set.of("--format", "--file", "--source");

        for (String name : parameters.keySet()) {
            if (!allowedParameters.contains(name)) {
                throw new CliException("Неизвестный параметр для команды " + command + ": " + name);
            }
        }

        String format = getRequiredParameter(parameters, "--format");

        if (!format.equals("csv") && !format.equals("json")) {
            throw new CliException("Формат должен быть csv или json");
        }

        String file = getRequiredParameter(parameters, "--file");
        Path path;
        try {
            path = Path.of(file);
        } catch (InvalidPathException e) {
            throw new CliException("Некорректный путь к файлу: " + file);
        }

        List< String > sources = new ArrayList< >();
        boolean append = false;
        String sourceFilter = null;

        if (command.equals("collect")) {
            String sourceNames = getRequiredParameter(parameters, "--sources");

            for (String name : sourceNames.split(",", -1)) {
                String source = name.strip();

                if (source.isEmpty()) {
                    throw new CliException("В списке источников есть пустое название");
                }

                if (sources.contains(source)) {
                    throw new CliException("Источник указан повторно: " + source);
                }

                sources.add(source);
            }

            String mode = getRequiredParameter(parameters, "--mode");

            if (!mode.equals("new") && !mode.equals("append")) {
                throw new CliException("Режим должен быть new или append");
            }

            append = mode.equals("append");
        } else {
            sourceFilter = parameters.get("--source");
        }

        return new CliOptions(command, sources, format, append, path, sourceFilter);
    }

    private Map< String, String > parseParameters(String[] args) {
        Map< String, String > parameters = new HashMap< >();

        for (int i = 1; i < args.length; i += 2) {
            String name = args[i];

            if (!name.startsWith("--")) {
                throw new CliException("Ожидалось название параметра с --: " + name);
            }

            if (i + 1 >= args.length || args[i + 1].startsWith("--")) {
                throw new CliException("Не указано значение параметра: " + name);
            }

            String value = args[i + 1];

            if (value.isBlank()) {
                throw new CliException("Пустое значение параметра: " + name);
            }

            if (parameters.containsKey(name)) {
                throw new CliException("Параметр указан повторно: " + name);
            }

            parameters.put(name, value);
        }

        return parameters;
    }

    private String getRequiredParameter(Map< String, String > parameters, String name) {
        String value = parameters.get(name);

        if (value == null) {
            throw new CliException("Не указан обязательный параметр: " + name);
        }

        return value;
    }
}