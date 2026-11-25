package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Цикли версії 1.2.0 збережено тут як еталон.
 * Потокова реалізація PlantSummary і CareSummary має давати ті самі показники.
 */
class LoopStreamEquivalenceTest {

    /** Підсумок, обчислений циклом так само, як у лабораторній роботі №3. */
    private static PlantSummary loopSummary(List<Plant> plants) {
        double totalValue = 0.0;
        long totalHeight = 0;
        int maxHeight = 0;
        String tallest = "-";
        int frequent = 0;
        Set<String> species = new HashSet<>();
        for (Plant plant : plants) {
            totalValue += plant.getPrice();
            totalHeight += plant.getHeightCm();
            if (plant.getHeightCm() > maxHeight) {
                maxHeight = plant.getHeightCm();
                tallest = plant.getSpecies();
            }
            if (plant.getWateringDays() <= 2) {
                frequent++;
            }
            species.add(plant.getSpecies());
        }
        int count = plants.size();
        return new PlantSummary(count, totalValue, count == 0 ? 0.0 : totalValue / count,
                count == 0 ? 0.0 : (double) totalHeight / count, tallest, maxHeight, frequent, species.size());
    }

    private static double loopCare(List<Plant> plants) {
        double total = 0.0;
        for (Plant plant : plants) {
            total += plant.careHours();
        }
        return total;
    }

    private static void assertSame(PlantSummary loop, PlantSummary stream) {
        assertEquals(loop.count(), stream.count());
        assertEquals(loop.totalValue(), stream.totalValue(), 1e-9);
        assertEquals(loop.averagePrice(), stream.averagePrice(), 1e-9);
        assertEquals(loop.averageHeight(), stream.averageHeight(), 1e-9);
        assertEquals(loop.tallestSpecies(), stream.tallestSpecies());
        assertEquals(loop.maxHeight(), stream.maxHeight());
        assertEquals(loop.frequentWatering(), stream.frequentWatering());
        assertEquals(loop.uniqueSpecies(), stream.uniqueSpecies());
    }

    @Test
    void sameResultsOnControlData() throws IOException {
        List<Plant> plants = Main.load(
                Files.readAllLines(Main.DEFAULT_INPUT, StandardCharsets.UTF_8), new ArrayList<>());
        assertSame(loopSummary(plants), PlantSummary.from(plants));
        assertEquals(loopCare(plants), CareSummary.from(plants).totalCareHours(), 1e-9);
    }

    @Test
    void sameResultsOnEmptyList() {
        assertSame(loopSummary(List.of()), PlantSummary.from(List.of()));
        assertEquals(0.0, CareSummary.from(List.of()).totalCareHours(), 1e-9);
    }

    @Test
    void sameTallestOnTie() {
        List<Plant> plants = List.of(new Tree("Клен", 1, 180, 1.0, 5), new Tree("Ялина", 1, 180, 1.0, 5));
        assertEquals(loopSummary(plants).tallestSpecies(), PlantSummary.from(plants).tallestSpecies());
    }
}
