package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Тести класу-сутності Plant: конструктор, fromCsv і toString. */
class PlantTest {

    @Test
    void constructorKeepsAllFields() {
        Plant plant = new Plant("Туя західна", 3, 120, 450.0, 3);
        assertEquals("Туя західна", plant.getSpecies());
        assertEquals(3, plant.getAgeYears());
        assertEquals(120, plant.getHeightCm());
        assertEquals(450.0, plant.getPrice(), 1e-9);
        assertEquals(3, plant.getWateringDays());
    }

    @Test
    void boundaryValuesAreAccepted() {
        Plant plant = new Plant("Тюльпан", 0, 1, 0.0, 1);
        assertEquals(0, plant.getAgeYears());
        assertEquals(0.0, plant.getPrice(), 1e-9);
    }

    @Test
    void nullSpeciesIsRejected() {
        assertThrows(NullPointerException.class, () -> new Plant(null, 1, 10, 1.0, 1));
    }

    @Test
    void blankSpeciesIsRejected() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new Plant("  ", 1, 10, 1.0, 1));
        assertEquals("species must not be blank", error.getMessage());
    }

    @Test
    void negativeAgeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Plant("Дуб", -1, 10, 1.0, 1));
    }

    @Test
    void zeroOrNegativeHeightIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Plant("Дуб", 1, 0, 1.0, 1));
        assertThrows(IllegalArgumentException.class, () -> new Plant("Дуб", 1, -150, 1.0, 1));
    }

    @Test
    void negativeNanOrInfinitePriceIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Plant("Дуб", 1, 10, -0.01, 1));
        assertThrows(IllegalArgumentException.class, () -> new Plant("Дуб", 1, 10, Double.NaN, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new Plant("Дуб", 1, 10, Double.POSITIVE_INFINITY, 1));
    }

    @Test
    void zeroWateringIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Plant("Дуб", 1, 10, 1.0, 0));
    }

    @Test
    void fromCsvParsesValidLine() {
        Plant plant = Plant.fromCsv("Троянда чайна;1;45;180.00;2");
        assertEquals("Троянда чайна", plant.getSpecies());
        assertEquals(45, plant.getHeightCm());
        assertEquals(180.0, plant.getPrice(), 1e-9);
    }

    @Test
    void fromCsvRejectsWrongFieldCount() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> Plant.fromCsv("Орхідея;1;25;300.00"));
        assertEquals("expected 5 fields, got 4", error.getMessage());
    }

    @Test
    void fromCsvRejectsWrongNumber() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> Plant.fromCsv("Хризантема;вік;30;90.00;2"));
        assertEquals("age is not a whole number: 'вік'", error.getMessage());
    }

    @Test
    void fromCsvRejectsBrokenInvariant() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> Plant.fromCsv("Береза;3;-150;500.00;4"));
        assertEquals("height must be positive, got -150", error.getMessage());
    }

    @Test
    void toStringUsesDotAsDecimalSeparator() {
        assertEquals("Дуб: 5 р., 210 см, 950.00 грн, полив кожні 7 дн.",
                new Plant("Дуб", 5, 210, 950.0, 7).toString());
    }
}
