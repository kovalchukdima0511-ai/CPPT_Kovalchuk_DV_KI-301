package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Тести record-ів HeightWatering і PlantSummary. */
class RecordTest {

    @Test
    void heightWateringHasValueEquality() {
        assertEquals(new HeightWatering(120, 3), new HeightWatering(120, 3));
        assertEquals(new HeightWatering(120, 3).hashCode(), new HeightWatering(120, 3).hashCode());
        assertEquals(120, new HeightWatering(120, 3).heightCm());
    }

    @Test
    void frequentWateringBoundary() {
        assertTrue(new HeightWatering(10, 2).needsFrequentWatering());
        assertFalse(new HeightWatering(10, 3).needsFrequentWatering());
    }

    @Test
    void heightWateringRejectsZero() {
        assertThrows(IllegalArgumentException.class, () -> new HeightWatering(0, 2));
    }

    @Test
    void summaryOfTwoPlants() {
        PlantSummary summary = PlantSummary.from(List.of(
                new Plant("Дуб", 5, 210, 950.0, 7),
                new Plant("Тюльпан", 1, 40, 35.5, 1)));
        assertEquals(new PlantSummary(2, 985.5, 492.75, 125.0, "Дуб", 210, 1, 2), summary);
    }

    @Test
    void summaryRejectsNegativeValues() {
        assertThrows(IllegalArgumentException.class,
                () -> new PlantSummary(-1, 0, 0, 0, "-", 0, 0, 0));
    }
}
