package ua.lpnu.kzp;

import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.EnumMap;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Потокові запити варіанта 12 над колекцією рослин.
 *
 * <p>Кожен метод відкриває новий потік, не змінює вхідний список і нічого не друкує.
 */
public final class PlantReport {

    /** Межа частого поливу в днях. */
    public static final int FREQUENT_WATERING_DAYS = HeightWatering.FREQUENT_WATERING_DAYS;

    /** Висота за спаданням, за рівної висоти — вид за зростанням. */
    public static final Comparator<Plant> BY_HEIGHT_DESC_THEN_SPECIES = Comparator
            .comparingInt(Plant::getHeightCm)
            .reversed()
            .thenComparing(Plant::getSpecies);

    private PlantReport() {
    }

    private static Stream<Plant> streamOf(List<? extends Plant> plants) {
        return Objects.requireNonNull(plants, "plants must not be null").stream().map(Plant.class::cast);
    }

    /**
     * filter: рослини, які поливають не рідше ніж раз на 2 дні.
     *
     * @param plants рослини
     * @return відібрані рослини в порядку файлу
     */
    public static List<Plant> frequentlyWatered(List<? extends Plant> plants) {
        return streamOf(plants)
                .filter(plant -> plant.getWateringDays() <= FREQUENT_WATERING_DAYS)
                .toList();
    }

    /**
     * map: види рослин.
     *
     * @param plants рослини
     * @return види в порядку файлу
     */
    public static List<String> speciesNames(List<? extends Plant> plants) {
        return streamOf(plants)
                .map(Plant::getSpecies)
                .toList();
    }

    /**
     * groupingBy: середня ціна за типом рослини.
     *
     * @param plants рослини
     * @return тип → середня ціна (у порядку констант enum)
     */
    public static Map<PlantKind, Double> averagePriceByKind(List<? extends Plant> plants) {
        return streamOf(plants)
                .collect(Collectors.groupingBy(
                        Plant::getKind,
                        () -> new EnumMap<>(PlantKind.class),
                        Collectors.averagingDouble(Plant::getPrice)));
    }

    /**
     * summarizingInt: статистика висоти за один прохід.
     *
     * @param plants рослини
     * @return кількість, сума, мінімум, максимум і середнє висоти
     */
    public static IntSummaryStatistics heightStatistics(List<? extends Plant> plants) {
        return streamOf(plants)
                .collect(Collectors.summarizingInt(Plant::getHeightCm));
    }

    /**
     * sorted + limit: перші N рослин за висотою (спаданням), далі за видом.
     *
     * @param plants рослини
     * @param limit скільки рослин залишити, не від'ємне
     * @return найвищі рослини
     */
    public static List<Plant> topByHeight(List<? extends Plant> plants, int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("limit must not be negative, got " + limit);
        }
        // sorted() стоїть перед limit(): спершу впорядковуємо всі, потім беремо найкращі.
        return streamOf(plants)
                .sorted(BY_HEIGHT_DESC_THEN_SPECIES)
                .limit(limit)
                .toList();
    }

    /**
     * Optional: перша рослина заданого виду в порядку файлу.
     *
     * @param plants рослини
     * @param species вид для пошуку
     * @return знайдена рослина або Optional.empty()
     */
    public static Optional<Plant> findBySpecies(List<? extends Plant> plants, String species) {
        Objects.requireNonNull(species, "species must not be null");
        return streamOf(plants)
                .filter(plant -> plant.getSpecies().equals(species))
                .findFirst();
    }
}
