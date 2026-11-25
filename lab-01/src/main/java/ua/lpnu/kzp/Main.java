package ua.lpnu.kzp;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Лабораторна робота №1. Розсадник рослин (варіант 12).
 *
 * <p>Програма читає CSV-файл із рядками {@code вид;вік;висота;ціна;полив},
 * пропускає хибні рядки з поясненням і формує текстовий звіт.
 * На цьому етапі запис зберігається як масив рядків, а поля беруться за індексами.
 */
public final class Main {

    /** Шлях до вхідного файлу за замовчуванням. */
    static final Path DEFAULT_INPUT = Path.of("data", "input.csv");

    /** Шлях до файлу звіту. */
    static final Path REPORT_FILE = Path.of("out", "report.txt");

    /** Кількість полів в одному рядку CSV. */
    static final int FIELD_COUNT = 5;

    /** Межа «частого поливу» в днях. */
    static final int FREQUENT_WATERING_DAYS = 2;

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
     * Перевіряє рядки, обчислює показники та повертає текст звіту.
     *
     * @param lines рядки вхідного файлу
     * @return повідомлення про пропущені рядки та звіт
     */
    static String analyze(List<String> lines) {
        StringBuilder out = new StringBuilder();
        List<String[]> records = new ArrayList<>();

        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);
            if (line.isBlank()) {
                continue;
            }
            String error = validate(line.split(";", -1));
            if (error == null) {
                records.add(line.split(";", -1));
            } else {
                out.append(String.format(Locale.ROOT, "Skipped line %d: %s%n", index + 1, error));
            }
        }

        double totalValue = 0.0;
        long totalHeight = 0;
        int maxHeight = 0;
        String tallest = "-";
        int frequent = 0;
        Set<String> species = new HashSet<>();

        for (String[] fields : records) {
            String name = fields[0].trim();
            int height = Integer.parseInt(fields[2].trim());
            double price = Double.parseDouble(fields[3].trim());
            int watering = Integer.parseInt(fields[4].trim());

            totalValue += price;
            totalHeight += height;
            if (height > maxHeight) {
                maxHeight = height;
                tallest = name;
            }
            if (watering <= FREQUENT_WATERING_DAYS) {
                frequent++;
            }
            species.add(name);
        }

        int count = records.size();
        double averagePrice = count == 0 ? 0.0 : totalValue / count;
        double averageHeight = count == 0 ? 0.0 : (double) totalHeight / count;

        out.append("--- REPORT ---").append(System.lineSeparator());
        out.append(String.format(Locale.ROOT, "Valid records: %d%n", count));
        out.append(String.format(Locale.ROOT, "Total stock value: %.2f UAH%n", totalValue));
        out.append(String.format(Locale.ROOT, "Average price: %.2f UAH%n", averagePrice));
        out.append(String.format(Locale.ROOT, "Average height: %.2f cm%n", averageHeight));
        out.append(String.format(Locale.ROOT, "Tallest plant: %s (%d cm)%n", tallest, maxHeight));
        out.append(String.format(Locale.ROOT, "Frequent watering (<= %d days): %d%n",
                FREQUENT_WATERING_DAYS, frequent));
        out.append(String.format(Locale.ROOT, "Unique species: %d%n", species.size()));
        return out.toString();
    }

    /**
     * Перевіряє поля одного рядка.
     *
     * @param fields поля рядка
     * @return текст помилки або {@code null}, якщо рядок коректний
     */
    static String validate(String[] fields) {
        if (fields.length != FIELD_COUNT) {
            return String.format(Locale.ROOT, "expected %d fields, got %d", FIELD_COUNT, fields.length);
        }
        if (fields[0].isBlank()) {
            return "species must not be blank";
        }
        Integer age = parseInt(fields[1]);
        if (age == null) {
            return String.format("age is not a whole number: '%s'", fields[1].trim());
        }
        if (age < 0) {
            return String.format(Locale.ROOT, "age must not be negative, got %d", age);
        }
        Integer height = parseInt(fields[2]);
        if (height == null) {
            return String.format("height is not a whole number: '%s'", fields[2].trim());
        }
        if (height <= 0) {
            return String.format(Locale.ROOT, "height must be positive, got %d", height);
        }
        Double price = parseDouble(fields[3]);
        if (price == null || !Double.isFinite(price)) {
            return String.format("price is not a number: '%s'", fields[3].trim());
        }
        if (price < 0) {
            return String.format(Locale.ROOT, "price must not be negative, got %.2f", price);
        }
        Integer watering = parseInt(fields[4]);
        if (watering == null) {
            return String.format("watering is not a whole number: '%s'", fields[4].trim());
        }
        if (watering <= 0) {
            return String.format(Locale.ROOT, "watering must be at least 1 day, got %d", watering);
        }
        return null;
    }

    private static Integer parseInt(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static Double parseDouble(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
