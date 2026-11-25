# Лабораторна робота №5. Параметризоване сховище, винятки й CSV-експорт

Кросплатформні засоби програмування · варіант 12 «Розсадник рослин» · Ковальчук Д. В., КІ-301 · версія 1.4.0

`Repository<T>`, анотація `@CsvColumn`, універсальний рефлексивний `CsvExporter`, документний `CsvParser`, власний `DataStorageException` із cause. Дані експортуються в `out/plants.csv` (UTF-8), читаються назад, round-trip дає рівні об'єкти.

## Як запустити

**VS Code (найпростіше).** File → Open Folder → ця папка (`lab-05`). Потрібне розширення *Extension Pack for Java*. Відкрийте `src/main/java/ua/lpnu/kzp/Main.java` і натисніть **Run** над `main` (або F5). Тести — вкладка *Testing*.

**Без Maven (Windows):** `run.cmd`

**Maven Wrapper** (Maven завантажиться автоматично під час першого запуску):

```
mvnw.cmd test        # тести (Windows)
mvnw.cmd verify      # тести + SpotBugs
mvnw.cmd package     # JAR у target/
java -jar target/plant-nursery-lab05-1.4.0.jar
./mvnw verify        # macOS / Ubuntu
```

Вхідні дані: `data/input.csv`. Звіт: `out/report.txt`.

## Структура

- Repository.java, CsvColumn.java, DataStorageException.java
- CsvExporter.java, CsvParser.java
- PlantRecord.java — плоский запис із @CsvColumn
- `src/test/java/...` — тести JUnit 5
- `pom.xml` — Java 17, JUnit 5, SpotBugs, Shade (виконуваний JAR)
