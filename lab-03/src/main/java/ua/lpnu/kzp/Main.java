package ua.lpnu.kzp;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Лабораторна робота №3. Розсадник рослин (варіант 12).
 *
 * <p>Рядки CSV перетворюються на дерева й квіти (підтипи {@link Plant}). Основний звіт
 * не змінився; додано розділ догляду, який обчислюється поліморфно.
 */
public final class Main {

    /** Шлях до вхідного файлу за замовчуванням. */
    static final Path DEFAULT_INPUT = Path.of("data", "input.csv");

    /** Шлях до файлу звіту. */
    static final Path REPORT_FILE = Path.of("out", "report.txt");

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
            String output = analyze(lines);
            System.out.print(output);
            Files.createDirectories(REPORT_FILE.getParent());
            Files.writeString(REPORT_FILE, output, StandardCharsets.UTF_8);
            System.out.printf("Report saved to %s%n", REPORT_FILE.toString().replace('\\', '/'));
        } catch (IOException exception) {
            System.out.printf("Cannot read or write file: %s%n", exception.getMessage());
            System.exit(1);
        }
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

    /**
     * Повний цикл: розбір рядків, підсумок і текст звіту.
     *
     * @param lines рядки файлу
     * @return повідомлення про пропущені рядки та звіт
     */
    static String analyze(List<String> lines) {
        List<String> errors = new ArrayList<>();
        List<Plant> plants = load(lines, errors);
        StringBuilder out = new StringBuilder();
        for (String error : errors) {
            out.append(error).append(System.lineSeparator());
        }
        out.append(formatReport(PlantSummary.from(plants)));
        return out.append(formatCare(CareSummary.from(plants))).toString();
    }
}
