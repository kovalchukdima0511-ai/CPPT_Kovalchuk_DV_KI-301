# Лабораторна робота №4. Потокова обробка даних (Stream API)

Кросплатформні засоби програмування · варіант 12 «Розсадник рослин» · Ковальчук Д. В., КІ-301 · версія 1.3.0

Цикли замінено потоками. `PlantReport`: filter (полив ≤ 2 дні), map (види), groupingBy (середня ціна за типом), summarizingInt (висота), top-5 (висота ↓, вид ↑), Optional (пошук за видом). Тест еквівалентності циклів і потоків.

## Як запустити

**VS Code (найпростіше).** File → Open Folder → ця папка (`lab-04`). Потрібне розширення *Extension Pack for Java*. Відкрийте `src/main/java/ua/lpnu/kzp/Main.java` і натисніть **Run** над `main` (або F5). Тести — вкладка *Testing*.

**Без Maven (Windows):** `run.cmd`

**Maven Wrapper** (Maven завантажиться автоматично під час першого запуску):

```
mvnw.cmd test        # тести (Windows)
mvnw.cmd verify      # тести + SpotBugs
mvnw.cmd package     # JAR у target/
java -jar target/plant-nursery-lab04-1.3.0.jar
./mvnw verify        # macOS / Ubuntu
```

Вхідні дані: `data/input.csv`. Звіт: `out/report.txt`.

## Структура

- PlantReport.java — потокові запити
- PlantSummary.java, CareSummary.java — показники через потоки
- `src/test/java/...` — тести JUnit 5
- `pom.xml` — Java 17, JUnit 5, SpotBugs, Shade (виконуваний JAR)
