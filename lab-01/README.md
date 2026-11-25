# Лабораторна робота №1. Maven-проєкт і CSV-звіт

Кросплатформні засоби програмування · варіант 12 «Розсадник рослин» · Ковальчук Д. В., КІ-301 · версія 1.0.0

Програма читає `data/input.csv` (поля `вид;вік;висота;ціна;полив`), пропускає хибні рядки з поясненням і формує звіт: кількість записів, загальна вартість, середні ціна й висота, найвища рослина, частий полив, унікальні види.

## Як запустити

**VS Code (найпростіше).** File → Open Folder → ця папка (`lab-01`). Потрібне розширення *Extension Pack for Java*. Відкрийте `src/main/java/ua/lpnu/kzp/Main.java` і натисніть **Run** над `main` (або F5). Тести — вкладка *Testing*.

**Без Maven (Windows):** `run.cmd`

**Maven Wrapper** (Maven завантажиться автоматично під час першого запуску):

```
mvnw.cmd test        # тести (Windows)
mvnw.cmd verify      # тести + SpotBugs
mvnw.cmd package     # JAR у target/
java -jar target/plant-nursery-lab01-1.0.0.jar
./mvnw verify        # macOS / Ubuntu
```

Вхідні дані: `data/input.csv`. Звіт: `out/report.txt`.

## Структура

- Main.java — читання, перевірка за індексами полів, звіт
- `src/test/java/...` — тести JUnit 5
- `pom.xml` — Java 17, JUnit 5, SpotBugs, Shade (виконуваний JAR)
