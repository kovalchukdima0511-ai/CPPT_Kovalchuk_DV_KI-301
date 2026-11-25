# Лабораторна робота №3. Спадкування, інтерфейси й поліморфізм

Кросплатформні засоби програмування · варіант 12 «Розсадник рослин» · Ковальчук Д. В., КІ-301 · версія 1.2.0

Запечатаний абстрактний `Plant` з підтипами `Tree` і `Flower`, інтерфейс `CareNeed`, enum `PlantKind`. Потреба в догляді `careHours()` рахується поліморфно, equals/hashCode перевірено в HashSet і HashMap.

## Як запустити

**VS Code (найпростіше).** File → Open Folder → ця папка (`lab-03`). Потрібне розширення *Extension Pack for Java*. Відкрийте `src/main/java/ua/lpnu/kzp/Main.java` і натисніть **Run** над `main` (або F5). Тести — вкладка *Testing*.

**Без Maven (Windows):** `run.cmd`

**Maven Wrapper** (Maven завантажиться автоматично під час першого запуску):

```
mvnw.cmd test        # тести (Windows)
mvnw.cmd verify      # тести + SpotBugs
mvnw.cmd package     # JAR у target/
java -jar target/plant-nursery-lab03-1.2.0.jar
./mvnw verify        # macOS / Ubuntu
```

Вхідні дані: `data/input.csv`. Звіт: `out/report.txt`.

## Структура

- Plant.java (sealed abstract), Tree.java, Flower.java
- PlantKind.java — enum, що визначає підтип за родом рослини
- CareNeed.java — інтерфейс
- CareSummary.java — показники догляду
- `src/test/java/...` — тести JUnit 5
- `pom.xml` — Java 17, JUnit 5, SpotBugs, Shade (виконуваний JAR)
