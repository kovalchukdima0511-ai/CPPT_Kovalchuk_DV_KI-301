package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Тести розбору рядків і звіту лабораторної роботи №1. */
class MainTest {

    @Test
    void validLineHasNoError() {
        assertNull(Main.validate("Туя західна;3;120;450.00;3".split(";", -1)));
    }

    @Test
    void zeroAgeAndZeroPriceAreAllowed() {
        assertNull(Main.validate("Тюльпан;0;10;0;1".split(";", -1)));
    }

    @Test
    void wrongFieldCountIsRejected() {
        assertEquals("expected 5 fields, got 4", Main.validate("Орхідея;1;25;300.00".split(";", -1)));
    }

    @Test
    void blankSpeciesIsRejected() {
        assertEquals("species must not be blank", Main.validate(" ;2;40;100.00;2".split(";", -1)));
    }

    @Test
    void nonNumericAgeIsRejected() {
        assertEquals("age is not a whole number: 'вік'",
                Main.validate("Хризантема;вік;30;90.00;2".split(";", -1)));
    }

    @Test
    void negativeHeightIsRejected() {
        assertEquals("height must be positive, got -150",
                Main.validate("Береза;3;-150;500.00;4".split(";", -1)));
    }

    @Test
    void zeroWateringIsRejected() {
        assertEquals("watering must be at least 1 day, got 0",
                Main.validate("Береза;3;150;500.00;0".split(";", -1)));
    }

    @Test
    void negativePriceIsRejected() {
        assertEquals("price must not be negative, got -1.00",
                Main.validate("Береза;3;150;-1;4".split(";", -1)));
    }

    @Test
    void reportOnControlFile() throws IOException {
        List<String> lines = Files.readAllLines(Main.DEFAULT_INPUT, StandardCharsets.UTF_8);
        String report = Main.analyze(lines);
        assertTrue(report.contains("Valid records: 10"));
        assertTrue(report.contains("Total stock value: 4345.50 UAH"));
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
}
