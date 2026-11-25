package ua.lpnu.kzp;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Лабораторна робота №5. Розсадник рослин (варіант 12).
 *
 * <p>Рослини зберігаються в параметризованому сховищі {@link Repository}, експортуються
 * універсальним рефлексивним {@link CsvExporter} у UTF-8 CSV, читаються назад і порівнюються
 * (round-trip). Звіти робіт №1–4 не змінилися.
 */
public final class Main {

    /** Шлях до вхідного файлу за замовчуванням. */
    static final Path DEFAULT_INPUT = Path.of("data", "input.csv");

    /** Шлях до файлу звіту. */
    static final Path REPORT_FILE = Path.of("out", "report.txt");

    /** Шлях до експортованого CSV. */
    static final Path CSV_FILE = Path.of("out", "plants.csv");

    private Main() {
    }

    /**
     * Точка входу.
     *
     * @param args необов'язковий шлях до CSV-файлу
     */
    public static void main(String[] args) {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true,
                StandardCharsets.UTF_8));
        Path input = args.length > 0 ? Path.of(args[0]) : DEFAULT_INPUT;
        try {
            List<String> lines = Files.readAllLines(input, StandardCharsets.UTF_8);
            Repository<Plant> repository = new Repository<>();
            List<String> errors = new ArrayList<>();
            repository.addAll(load(lines, errors));
            String output = report(errors, repository.all()) + exportSection(repository, CSV_FILE);
            System.out.print(output);
            Files.createDirectories(REPORT_FILE.getParent());
            Files.writeString(REPORT_FILE, output, StandardCharsets.UTF_8);
            System.out.printf("Report saved to %s%n", slash(REPORT_FILE));
        } catch (IOException exception) {
            System.out.printf("Cannot read or write file: %s%n", exception.getMessage());
            System.exit(1);
        } catch (DataStorageException exception) {
            System.out.printf("Storage error: %s (cause: %s)%n", exception.getMessage(), exception.getCause());
            System.exit(2);
        }
    }

    private static String slash(Path path) {
        return path.toString().replace('\\', '/');
    }

    /** Ключ round-trip для демонстрації: вид + вік. */
    static final String KEY_SPECIES = "Троянда чайна";

    /** Вік для ключа round-trip. */
    static final int KEY_AGE = 2;

    /**
     * Експортує сховище в CSV, читає файл назад і порівнює записи.
     *
     * @param repository сховище рослин
     * @param csvFile шлях до CSV
     * @return текст розділу звіту
     * @throws DataStorageException якщо експорт або імпорт не вдався
     */
    static String exportSection(Repository<Plant> repository, Path csvFile) throws DataStorageException {
        List<PlantRecord> original = repository.all().stream().map(PlantRecord::from).toList();
        CsvExporter.write(csvFile, PlantRecord.class, original);
        List<PlantRecord> restored = PlantRecord.read(csvFile);
        List<Plant> restoredPlants = restored.stream().map(PlantRecord::toPlant).toList();
        String escaped = CsvExporter.toCsv(PlantRecord.class, original).lines()
                .filter(line -> line.contains("\""))
                .findFirst()
                .orElse("-");
        String key = restored.stream()
                .filter(r -> r.getSpecies().equals(KEY_SPECIES) && r.getAgeYears() == KEY_AGE)
                .findFirst()
                .map(r -> String.format(Locale.ROOT, "found, %d cm, %.2f UAH", r.getHeightCm(), r.getPrice()))
                .orElse("not found");

        StringBuilder out = new StringBuilder();
        String nl = System.lineSeparator();
        out.append("--- CSV EXPORT ---").append(nl);
        out.append(String.format(Locale.ROOT, "Repository<Plant> size: %d%n", repository.size()));
        out.append(String.format(Locale.ROOT, "Repository find (watering <= 2): %d%n",
                repository.find(plant -> plant.getWateringDays() <= PlantReport.FREQUENT_WATERING_DAYS).size()));
        out.append("CSV file: ").append(slash(csvFile)).append(" (UTF-8)").append(nl);
        out.append("CSV header: ").append(CsvExporter.header(PlantRecord.class)).append(nl);
        out.append("Escaped row: ").append(escaped).append(nl);
        out.append(String.format(Locale.ROOT, "Restored records: %d%n", restored.size()));
        out.append(String.format(Locale.ROOT, "Round-trip records equal: %s%n", original.equals(restored)));
        out.append(String.format(Locale.ROOT, "Round-trip plants equal: %s%n",
                repository.all().equals(restoredPlants)));
        out.append(String.format(Locale.ROOT, "Key \"%s\" + %d: %s%n", KEY_SPECIES, KEY_AGE, key));
        return out.toString();
    }

    /**
     * Перетворює рядки на рослини; хибні рядки потрапляють до списку помилок.
     *
     * @param lines рядки файлу
     * @param errors список, куди додаються повідомлення з номером рядка
     * @return коректні рослини в порядку файлу
     */
    static List<Plant> load(List<String> lines, List<String> errors) {
        List<Plant> plants = new ArrayList<>();
        for (int lineNumber = 1; lineNumber <= lines.size(); lineNumber++) {
            String line = lines.get(lineNumber - 1);
            if (line.isBlank()) {
                continue;
            }
            try {
                plants.add(Plant.fromCsv(line));
            } catch (IllegalArgumentException exception) {
                errors.add(String.format(Locale.ROOT, "Skipped line %d: %s",
                        lineNumber, exception.getMessage()));
            }
        }
        return plants;
    }

    /**
     * Формує текст звіту за підсумком.
     *
     * @param summary показники
     * @return текст звіту
     */
    static String formatReport(PlantSummary summary) {
        StringBuilder out = new StringBuilder();
        out.append("--- REPORT ---").append(System.lineSeparator());
        out.append(String.format(Locale.ROOT, "Valid records: %d%n", summary.count()));
        out.append(String.format(Locale.ROOT, "Total stock value: %.2f UAH%n", summary.totalValue()));
        out.append(String.format(Locale.ROOT, "Average price: %.2f UAH%n", summary.averagePrice()));
        out.append(String.format(Locale.ROOT, "Average height: %.2f cm%n", summary.averageHeight()));
        out.append(String.format(Locale.ROOT, "Tallest plant: %s (%d cm)%n",
                summary.tallestSpecies(), summary.maxHeight()));
        out.append(String.format(Locale.ROOT, "Frequent watering (<= %d days): %d%n",
                HeightWatering.FREQUENT_WATERING_DAYS, summary.frequentWatering()));
        out.append(String.format(Locale.ROOT, "Unique species: %d%n", summary.uniqueSpecies()));
        return out.toString();
    }

    /**
     * Формує розділ догляду.
     *
     * @param care показники догляду
     * @return текст розділу
     */
    static String formatCare(CareSummary care) {
        StringBuilder out = new StringBuilder();
        out.append("--- CARE (polymorphic) ---").append(System.lineSeparator());
        out.append(String.format(Locale.ROOT, "Plants by kind: %s=%d, %s=%d%n",
                PlantKind.TREE.label(), care.trees(), PlantKind.FLOWER.label(), care.flowers()));
        out.append(String.format(Locale.ROOT, "Total care need: %.2f h/month%n", care.totalCareHours()));
        out.append(String.format(Locale.ROOT, "Highest care need: %s (%.2f h/month)%n",
                care.highestCareSpecies(), care.highestCareHours()));
        out.append(String.format(Locale.ROOT, "Unique plants (HashSet): %d%n", care.uniquePlants()));
        return out.toString();
    }

    /** Вид, який шукається в демонстраційному запиті Optional. */
    static final String SEARCH_SPECIES = "Півонія молочноквіткова";

    /**
     * Формує розділ потокових запитів.
     *
     * @param plants рослини
     * @return текст розділу
     */
    static String formatQueries(List<Plant> plants) {
        StringBuilder out = new StringBuilder();
        String nl = System.lineSeparator();
        out.append("--- STREAM QUERIES ---").append(nl);
        List<Plant> frequent = PlantReport.frequentlyWatered(plants);
        out.append(String.format(Locale.ROOT, "Watering <= %d days: %d (%s)%n",
                PlantReport.FREQUENT_WATERING_DAYS, frequent.size(),
                String.join(", ", PlantReport.speciesNames(frequent))));
        out.append("Species: ").append(String.join(", ", PlantReport.speciesNames(plants))).append(nl);
        out.append("Average price by kind: ").append(PlantReport.averagePriceByKind(plants).entrySet().stream()
                .map(entry -> String.format(Locale.ROOT, "%s=%.2f", entry.getKey().label(), entry.getValue()))
                .collect(Collectors.joining(", "))).append(nl);
        IntSummaryStatistics stats = PlantReport.heightStatistics(plants);
        out.append(stats.getCount() == 0
                ? "Height statistics: no data" + nl
                : String.format(Locale.ROOT,
                        "Height statistics: count=%d, sum=%d, min=%d, max=%d, average=%.2f%n",
                        stats.getCount(), stats.getSum(), stats.getMin(), stats.getMax(), stats.getAverage()));
        out.append("Top 5 by height: ").append(PlantReport.topByHeight(plants, 5).stream()
                .map(plant -> String.format(Locale.ROOT, "%s (%d)", plant.getSpecies(), plant.getHeightCm()))
                .collect(Collectors.joining(", "))).append(nl);
        out.append(String.format(Locale.ROOT, "Search \"%s\": %s%n", SEARCH_SPECIES,
                PlantReport.findBySpecies(plants, SEARCH_SPECIES)
                        .map(plant -> String.format(Locale.ROOT, "found, %d cm, %.2f UAH, %s",
                                plant.getHeightCm(), plant.getPrice(), plant.getKind().label()))
                        .orElse("not found")));
        return out.toString();
    }

    /**
     * Повний цикл: розбір рядків, підсумок і текст звіту.
     *
     * @param lines рядки файлу
     * @return повідомлення про пропущені рядки та звіт
     */
    static String analyze(List<String> lines) {
        List<String> errors = new ArrayList<>();
        List<Plant> plants = load(lines, errors);
        return report(errors, plants);
    }

    /**
     * Формує звіт робіт №1–4 за вже завантаженими даними.
     *
     * @param errors повідомлення про пропущені рядки
     * @param plants коректні рослини
     * @return текст звіту
     */
    static String report(List<String> errors, List<Plant> plants) {
        StringBuilder out = new StringBuilder();
        for (String error : errors) {
            out.append(error).append(System.lineSeparator());
        }
        out.append(formatReport(PlantSummary.from(plants)));
        out.append(formatCare(CareSummary.from(plants)));
        return out.append(formatQueries(plants)).toString();
    }
}
