# Data Aggregator

Консольное Java-приложение для получения данных из трёх REST API,
преобразования в единую модель и сохранения в CSV или JSON.

## Источники данных

| Название для CLI | Сервис | Данные |
|---|---|---|
| `openvan` | [OpenVan](https://openvan.camp/) | Цены на топливо |
| `wikipedia` | [Wikipedia](https://ru.wikipedia.org/) | Результаты поиска статей |
| `osrm` | [OSRM](https://project-osrm.org/) | Маршруты между двумя точками |

Данные OpenVan предоставляются по лицензии CC BY 4.0.
При отображении этих данных необходимо указывать источник:
https://openvan.camp/.

Текущие параметры запросов заданы в классе `Application`:

- OpenVan: страна `ru`.
- Wikipedia: запрос «Казанский собор».
- OSRM: маршрут от точки `30.3158, 59.9391`
  до точки `30.3609, 59.9311` (долгота, широта).

## Технологии

- Java 26.
- Gradle с Kotlin DSL.
- Java HttpClient для HTTP-запросов.
- Gson для работы с JSON.
- Apache Commons CSV для работы с CSV.

Gradle Wrapper включён в репозиторий:
отдельно устанавливать Gradle не требуется.

## Сборка

Команды выполняются из корня проекта.

Windows PowerShell:

```powershell
.\gradlew.bat build
```

Linux / macOS:

```bash
./gradlew build
```

Для сборки требуется JDK 26. Для получения данных нужен доступ к интернету.

## Запуск

Примеры ниже приведены для Windows PowerShell.
В Linux / macOS вместо `.\gradlew.bat` используйте `./gradlew`.

### Справка

```powershell
.\gradlew.bat run --args="--help"
```

### Сбор и сохранение данных

```powershell
.\gradlew.bat run --args="collect --sources openvan,wikipedia,osrm --format json --mode new --file records.json"
```

Параметры команды `collect`:

| Параметр | Назначение |
|---|---|
| `--sources` | Источники через запятую: `openvan`, `wikipedia`, `osrm` |
| `--format` | Формат файла: `csv` или `json` |
| `--mode` | Режим: `new` или `append` |
| `--file` | Путь к файлу |

Все четыре параметра обязательны.

Режим `new` создаёт файл или перезаписывает существующий.
Режим `append` добавляет записи, сохраняя предыдущие.
Повторяющиеся записи автоматически не удаляются.

Пример сохранения в CSV:

```powershell
.\gradlew.bat run --args="collect --sources wikipedia --format csv --mode new --file records.csv"
```

Пример добавления в JSON:

```powershell
.\gradlew.bat run --args="collect --sources wikipedia --format json --mode append --file records.json"
```

При ошибке получения данных от одного из выбранных источников
сбор прерывается, сохранение полученного набора не выполняется.

### Просмотр сохранённых данных

Все записи:

```powershell
.\gradlew.bat run --args="view --format json --file records.json"
```

Только записи выбранного источника:

```powershell
.\gradlew.bat run --args="view --format csv --file records.csv --source wikipedia"
```

Параметры команды `view`:

| Параметр | Назначение |
|---|---|
| `--format` | Формат читаемого файла: `csv` или `json` |
| `--file` | Путь к существующему файлу |
| `--source` | Необязательный фильтр по источнику |

Команда `view` читает файл без обращения к API.
Формат задаётся параметром `--format` и должен соответствовать содержимому файла.

## Модель записи

Каждая запись содержит:

| Поле | Содержимое |
|---|---|
| `source` | Название источника |
| `category` | Категория: `fuel`, `place` или `route` |
| `title` | Название записи |
| `fetchedAt` | Время получения записи приложением |
| `attributes` | Характеристики в виде пар «строка — строка» |

В CSV характеристики сохраняются как JSON-текст в отдельном столбце.
В JSON характеристики сохраняются как вложенный объект.
Файлы читаются и записываются в UTF-8.

## Структура проекта

| Пакет | Назначение |
|---|---|
| `app` | Точка входа и сборка объектов приложения |
| `cli` | Разбор аргументов, выполнение команд и вывод |
| `http` | Отправка HTTP-запросов |
| `parser` | Разбор и проверка JSON-ответов API |
| `source` | Источники данных, преобразование ответов в записи и реестр источников |
| `model` | Единая модель `AggregatedRecord` |
| `service` | Агрегация записей из выбранных источников |
| `storage` | Интерфейс хранилища, реализации CSV/JSON и реестр |
| `exception` | Собственные классы исключений |

`Main` запускает `Application`.
В `Application` создаются объекты и передаются зависимости.
`ConsoleApplication` выполняет выбранную команду.

## Отображение кириллицы в PowerShell

Если вместо русских букв отображаются неправильные символы,
перед запуском выполните:

```powershell
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new()
```

Настройка применяется к текущему сеансу консоли.