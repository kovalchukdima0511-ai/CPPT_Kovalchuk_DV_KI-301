package ua.lpnu.kzp;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Показники догляду, обчислені поліморфно через {@link CareNeed#careHours()}.
 *
 * @param totalCareHours сумарна потреба в догляді, год/міс
 * @param highestCareSpecies вид із найбільшою потребою або «-»
 * @param highestCareHours найбільша потреба, год/міс
 * @param trees кількість дерев
 * @param flowers кількість квітів
 * @param uniquePlants кількість логічно різних рослин (HashSet)
 */
public record CareSummary(double totalCareHours, String highestCareSpecies, double highestCareHours,
        int trees, int flowers, int uniquePlants) {

    /** Перевіряє, що показники не від'ємні. */
    public CareSummary {
        Objects.requireNonNull(highestCareSpecies, "highestCareSpecies must not be null");
        if (totalCareHours < 0 || highestCareHours < 0 || trees < 0 || flowers < 0 || uniquePlants < 0) {
            throw new IllegalArgumentException("care values must not be negative");
        }
    }

    /**
     * Обчислює показники догляду. У циклі немає перевірок instanceof:
     * careHours() викликається через базовий тип.
     *
     * @param plants рослини різних підтипів
     * @return показники догляду
     */
    public static CareSummary from(List<? extends Plant> plants) {
        Objects.requireNonNull(plants, "plants must not be null");
        double total = 0.0;
        double highest = 0.0;
        String highestSpecies = "-";
        Map<PlantKind, Integer> byKind = new EnumMap<>(PlantKind.class);
        for (Plant plant : plants) {
            double hours = plant.careHours();
            total += hours;
            if (hours > highest) {
                highest = hours;
                highestSpecies = plant.getSpecies();
            }
            byKind.merge(plant.getKind(), 1, Integer::sum);
        }
        int unique = new HashSet<>(plants).size();
        return new CareSummary(total, highestSpecies, highest,
                byKind.getOrDefault(PlantKind.TREE, 0), byKind.getOrDefault(PlantKind.FLOWER, 0), unique);
    }
}
