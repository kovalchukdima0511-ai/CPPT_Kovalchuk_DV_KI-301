package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Тести потокових запитів на контрольних даних і крайових випадках. */
class PlantReportTest {

    private static List<Plant> control() throws IOException {
        return Main.load(Files.readAllLines(Main.DEFAULT_INPUT, StandardCharsets.UTF_8), new ArrayList<>());
    }

    @Test
    void filterKeepsWateringUpToTwoDays() throws IOException {
        List<Plant> frequent = PlantReport.frequentlyWatered(control());
        assertEquals(5, frequent.size());
        assertTrue(frequent.stream().allMatch(plant -> plant.getWateringDays() <= 2));
    }

    @Test
    void filterBoundaryTwoDaysIncludedThreeExcluded() {
        List<Plant> plants = List.of(new Flower("Троянда", 1, 10, 1.0, 2), new Flower("Лаванда", 1, 10, 1.0, 3));
        assertEquals(List.of("Троянда"), PlantReport.speciesNames(PlantReport.frequentlyWatered(plants)));
    }

    @Test
    void mapReturnsSpeciesInFileOrderWithDuplicates() throws IOException {
        List<String> names = PlantReport.speciesNames(control());
        assertEquals(10, names.size());
        assertEquals("Туя західна", names.get(0));
        assertEquals(2, names.stream().filter("Троянда чайна"::equals).count());
    }

    @Test
    void groupingAveragePriceByKindInEnumOrder() throws IOException {
        Map<PlantKind, Double> byKind = PlantReport.averagePriceByKind(control());
        assertEquals(List.of(PlantKind.TREE, PlantKind.FLOWER), new ArrayList<>(byKind.keySet()));
        assertEquals(825.0, byKind.get(PlantKind.TREE), 1e-9);
        assertEquals(174.25, byKind.get(PlantKind.FLOWER), 1e-9);
    }

    @Test
    void heightStatistics() throws IOException {
        IntSummaryStatistics stats = PlantReport.heightStatistics(control());
        assertEquals(10, stats.getCount());
        assertEquals(1025, stats.getSum());
        assertEquals(35, stats.getMin());
        assertEquals(210, stats.getMax());
        assertEquals(102.5, stats.getAverage(), 1e-9);
    }

    @Test
    void topFiveUsesSecondKeyOnEqualHeight() throws IOException {
        List<String> top = PlantReport.speciesNames(PlantReport.topByHeight(control(), 5));
        assertEquals(List.of("Дуб звичайний", "Клен гостролистий", "Ялина колюча", "Туя західна",
                "Троянда \"Глорія Дей\", плетиста"), top);
    }

    @Test
    void topLimitZeroBigAndNegative() throws IOException {
        assertEquals(0, PlantReport.topByHeight(control(), 0).size());
        assertEquals(10, PlantReport.topByHeight(control(), 100).size());
        assertThrows(IllegalArgumentException.class, () -> PlantReport.topByHeight(List.of(), -1));
    }

    @Test
    void findReturnsFirstMatch() throws IOException {
        List<Plant> plants = control();
        Optional<Plant> found = PlantReport.findBySpecies(plants, "Троянда чайна");
        assertTrue(found.isPresent());
        assertSame(plants.get(1), found.get());
        assertEquals(1, found.get().getAgeYears());
    }

    @Test
    void findReturnsEmptyWhenAbsent() throws IOException {
        assertTrue(PlantReport.findBySpecies(control(), "Кактус").isEmpty());
    }

    @Test
    void queriesOnEmptyList() {
        assertEquals(List.of(), PlantReport.frequentlyWatered(List.of()));
        assertTrue(PlantReport.averagePriceByKind(List.of()).isEmpty());
        assertEquals(0, PlantReport.heightStatistics(List.of()).getCount());
        assertTrue(PlantReport.findBySpecies(List.of(), "Дуб").isEmpty());
    }

    @Test
    void queriesDoNotChangeInput() throws IOException {
        List<Plant> plants = new ArrayList<>(control());
        List<Plant> copy = List.copyOf(plants);
        PlantReport.topByHeight(plants, 5);
        PlantReport.frequentlyWatered(plants);
        assertEquals(copy, plants);
    }

    @Test
    void reportContainsQuerySection() throws IOException {
        String report = Main.analyze(Files.readAllLines(Main.DEFAULT_INPUT, StandardCharsets.UTF_8));
        assertTrue(report.contains("--- STREAM QUERIES ---"));
        assertTrue(report.contains("Average price by kind: дерево=825.00, квітка=174.25"));
        assertTrue(report.contains("Search \"Півонія молочноквіткова\": found, 70 cm, 260.00 UAH, квітка"));
    }

    @Test
    void emptyReportPrintsNoData() {
        assertTrue(Main.analyze(List.of()).contains("Height statistics: no data"));
    }
}
