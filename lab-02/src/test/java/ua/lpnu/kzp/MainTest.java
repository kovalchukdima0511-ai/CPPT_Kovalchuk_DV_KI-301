package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Тести звіту. Перенесені з роботи №1: текст звіту не змінився. */
class MainTest {

    @Test
    void reportOnControlFile() throws IOException {
        List<String> lines = Files.readAllLines(Main.DEFAULT_INPUT, StandardCharsets.UTF_8);
        String report = Main.analyze(lines);
        assertTrue(report.contains("Valid records: 10"));
        assertTrue(report.contains("Total stock value: 4345.50 UAH"));
        assertTrue(report.contains("Average price: 434.55 UAH"));
        assertTrue(report.contains("Average height: 102.50 cm"));
        assertTrue(report.contains("Tallest plant: Дуб звичайний (210 cm)"));
        assertTrue(report.contains("Frequent watering (<= 2 days): 5"));
        assertTrue(report.contains("Unique species: 9"));
        assertEquals(4, report.lines().filter(line -> line.startsWith("Skipped")).count());
    }

    @Test
    void emptyInputGivesZeroReport() {
        String report = Main.analyze(List.of());
        assertTrue(report.contains("Valid records: 0"));
        assertTrue(report.contains("Average price: 0.00 UAH"));
        assertTrue(report.contains("Tallest plant: - (0 cm)"));
    }

    @Test
    void invalidLinesDoNotReachTheList() {
        List<String> errors = new ArrayList<>();
        List<Plant> plants = Main.load(List.of(
                "Туя західна;3;120;450.00;3",
                "Хризантема садова;вік;30;90.00;2",
                "Орхідея фаленопсис;1;25;300.00"), errors);
        assertEquals(1, plants.size());
        assertEquals(List.of(
                "Skipped line 2: age is not a whole number: 'вік'",
                "Skipped line 3: expected 5 fields, got 4"), errors);
    }
}
