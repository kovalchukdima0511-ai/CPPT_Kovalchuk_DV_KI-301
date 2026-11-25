package ua.lpnu.kzp;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
    public static PlantSummary from(List<Plant> plants) {
        Objects.requireNonNull(plants, "plants must not be null");
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
            if (plant.heightWatering().needsFrequentWatering()) {
                frequent++;
            }
            species.add(plant.getSpecies());
        }
        int count = plants.size();
        double averagePrice = count == 0 ? 0.0 : totalValue / count;
        double averageHeight = count == 0 ? 0.0 : (double) totalHeight / count;
        return new PlantSummary(count, totalValue, averagePrice, averageHeight,
                tallest, maxHeight, frequent, species.size());
    }
}
