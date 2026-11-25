package ua.lpnu.kzp;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
     * Обчислює показники догляду потоками. Перевірок instanceof немає:
     * careHours() викликається через базовий тип.
     *
     * @param plants рослини різних підтипів
     * @return показники догляду
     */
    public static CareSummary from(List<? extends Plant> plants) {
        Objects.requireNonNull(plants, "plants must not be null");
        double total = plants.stream()
                .mapToDouble(CareNeed::careHours)
                .sum();
        Optional<? extends Plant> highest = plants.stream()
                .max(Comparator.comparingDouble(CareNeed::careHours));
        Map<PlantKind, Long> byKind = plants.stream()
                .collect(Collectors.groupingBy(Plant::getKind,
                        () -> new EnumMap<>(PlantKind.class), Collectors.counting()));
        int unique = new HashSet<>(plants).size();
        return new CareSummary(total,
                highest.map(Plant::getSpecies).orElse("-"),
                highest.map(Plant::careHours).orElse(0.0),
                byKind.getOrDefault(PlantKind.TREE, 0L).intValue(),
                byKind.getOrDefault(PlantKind.FLOWER, 0L).intValue(),
                unique);
    }
}
