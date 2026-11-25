package ua.lpnu.kzp;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Незмінний набір показників звіту.
 *
 * @param count кількість коректних записів
 * @param totalValue загальна вартість рослин
 * @param averagePrice середня ціна
 * @param averageHeight середня висота
 * @param tallestSpecies вид найвищої рослини або «-»
 * @param maxHeight найбільша висота
 * @param frequentWatering кількість рослин із частим поливом
 * @param uniqueSpecies кількість різних видів
 */
public record PlantSummary(int count, double totalValue, double averagePrice, double averageHeight,
        String tallestSpecies, int maxHeight, int frequentWatering, int uniqueSpecies) {

    /** Перевіряє, що показники не від'ємні. */
    public PlantSummary {
        Objects.requireNonNull(tallestSpecies, "tallestSpecies must not be null");
        if (count < 0 || totalValue < 0 || averagePrice < 0 || averageHeight < 0
                || maxHeight < 0 || frequentWatering < 0 || uniqueSpecies < 0) {
            throw new IllegalArgumentException("summary values must not be negative");
        }
    }

    /**
     * Обчислює показники для списку рослин.
     *
     * @param plants коректні рослини
     * @return підсумок
     */
    public static PlantSummary from(List<? extends Plant> plants) {
        Objects.requireNonNull(plants, "plants must not be null");
        double totalValue = plants.stream()
                .mapToDouble(Plant::getPrice)
                .sum();
        double averagePrice = plants.stream()
                .mapToDouble(Plant::getPrice)
                .average()
                .orElse(0.0);
        double averageHeight = plants.stream()
                .mapToInt(Plant::getHeightCm)
                .average()
                .orElse(0.0);
        Optional<? extends Plant> tallest = plants.stream()
                .max(Comparator.comparingInt(Plant::getHeightCm));
        long frequent = plants.stream()
                .filter(plant -> plant.heightWatering().needsFrequentWatering())
                .count();
        long unique = plants.stream()
                .map(Plant::getSpecies)
                .distinct()
                .count();
        return new PlantSummary(plants.size(), totalValue, averagePrice, averageHeight,
                tallest.map(Plant::getSpecies).orElse("-"),
                tallest.map(Plant::getHeightCm).orElse(0),
                (int) frequent, (int) unique);
    }
}
