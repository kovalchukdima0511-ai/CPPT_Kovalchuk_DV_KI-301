# Лабораторна робота №2. Класи, record та інкапсуляція

Кросплатформні засоби програмування · варіант 12 «Розсадник рослин» · Ковальчук Д. В., КІ-301 · версія 1.1.0

Рядок CSV перетворюється на незмінний об'єкт `Plant` (private final, валідація в конструкторі, фабрика `fromCsv`). Допоміжні значення — record `HeightWatering` і `PlantSummary`. Звіт той самий, що в ЛР №1.

## Як запустити

**VS Code (найпростіше).** File → Open Folder → ця папка (`lab-02`). Потрібне розширення *Extension Pack for Java*. Відкрийте `src/main/java/ua/lpnu/kzp/Main.java` і натисніть **Run** над `main` (або F5). Тести — вкладка *Testing*.

**Без Maven (Windows):** `run.cmd`

**Maven Wrapper** (Maven завантажиться автоматично під час першого запуску):

```
mvnw.cmd test        # тести (Windows)
mvnw.cmd verify      # тести + SpotBugs
mvnw.cmd package     # JAR у target/
java -jar target/plant-nursery-lab02-1.1.0.jar
./mvnw verify        # macOS / Ubuntu
```

Вхідні дані: `data/input.csv`. Звіт: `out/report.txt`.

## Структура

- Plant.java — клас-сутність
- HeightWatering.java, PlantSummary.java — record-и
- Main.java — завантаження та звіт
- `src/test/java/...` — тести JUnit 5
- `pom.xml` — Java 17, JUnit 5, SpotBugs, Shade (виконуваний JAR)
