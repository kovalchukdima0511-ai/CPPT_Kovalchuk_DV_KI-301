package ua.lpnu.kzp;

import java.util.Locale;
import java.util.Objects;

/**
 * Спільний базовий тип рослини розсадника.
 *
 * <p>Тут зберігаються спільні поля та всі спільні інваріанти. Відмінна поведінка —
 * потреба в догляді {@link #careHours()} — реалізується в підтипах {@link Tree} і {@link Flower}.
 * Ієрархія запечатана: інших підтипів, крім дозволених, бути не може.
 */
public abstract sealed class Plant implements CareNeed permits Tree, Flower {

    /** Кількість полів у рядку CSV. */
    public static final int FIELD_COUNT = 5;

    private final String species;
    private final int ageYears;
    private final int heightCm;
    private final double price;
    private final int wateringDays;

    /**
     * Перевіряє та зберігає спільні поля. Викликається з конструкторів підтипів через super.
     *
     * @param species вид рослини, не порожній
     * @param ageYears вік у роках, не від'ємний
     * @param heightCm висота в сантиметрах, більша за нуль
     * @param price ціна в гривнях, не від'ємна та скінченна
     * @param wateringDays періодичність поливу в днях, не менше 1
     * @throws IllegalArgumentException якщо хоча б одне значення некоректне
     */
    protected Plant(String species, int ageYears, int heightCm, double price, int wateringDays) {
        this.species = Objects.requireNonNull(species, "species must not be null").trim();
        if (this.species.isEmpty()) {
            throw new IllegalArgumentException("species must not be blank");
        }
        if (ageYears < 0) {
            throw new IllegalArgumentException("age must not be negative, got " + ageYears);
        }
        if (heightCm <= 0) {
            throw new IllegalArgumentException("height must be positive, got " + heightCm);
        }
        if (!Double.isFinite(price)) {
            throw new IllegalArgumentException("price must be a finite number");
        }
        if (price < 0) {
            throw new IllegalArgumentException(
                    String.format(Locale.ROOT, "price must not be negative, got %.2f", price));
        }
        if (wateringDays <= 0) {
            throw new IllegalArgumentException("watering must be at least 1 day, got " + wateringDays);
        }
        this.ageYears = ageYears;
        this.heightCm = heightCm;
        this.price = price;
        this.wateringDays = wateringDays;
    }

    /**
     * Створює рослину потрібного підтипу з рядка {@code вид;вік;висота;ціна;полив}.
     *
     * <p>Підтип визначає {@link PlantKind#fromSpecies(String)} за першим словом виду,
     * а створює сама константа enum — без if/switch за підтипом.
     *
     * @param line рядок CSV
     * @return дерево або квітка
     * @throws IllegalArgumentException якщо рядок має хибну структуру або значення
     */
    public static Plant fromCsv(String line) {
        Objects.requireNonNull(line, "line must not be null");
        String[] fields = line.split(";", -1);
        if (fields.length != FIELD_COUNT) {
            throw new IllegalArgumentException(
                    "expected " + FIELD_COUNT + " fields, got " + fields.length);
        }
        String species = fields[0].trim();
        if (species.isEmpty()) {
            throw new IllegalArgumentException("species must not be blank");
        }
        int age = parseInt(fields[1], "age");
        int height = parseInt(fields[2], "height");
        double price = parseDouble(fields[3], "price");
        int watering = parseInt(fields[4], "watering");
        return PlantKind.fromSpecies(species).create(species, age, height, price, watering);
    }

    private static int parseInt(String text, String field) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    field + " is not a whole number: '" + text.trim() + "'", exception);
        }
    }

    private static double parseDouble(String text, String field) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    field + " is not a number: '" + text.trim() + "'", exception);
        }
    }

    /**
     * Обчислює потребу в догляді — кількість годин роботи садівника на місяць.
     *
     * @return години догляду на місяць
     */
    @Override
    public abstract double careHours();

    /** @return категорія рослини */
    public abstract PlantKind getKind();

    /** @return вид рослини */
    public final String getSpecies() {
        return species;
    }

    /** @return вік у роках */
    public final int getAgeYears() {
        return ageYears;
    }

    /** @return висота в сантиметрах */
    public final int getHeightCm() {
        return heightCm;
    }

    /** @return ціна в гривнях */
    public final double getPrice() {
        return price;
    }

    /** @return періодичність поливу в днях */
    public final int getWateringDays() {
        return wateringDays;
    }

    /** @return пара «висота — полив» як незмінне значення */
    public final HeightWatering heightWatering() {
        return new HeightWatering(heightCm, wateringDays);
    }

    /**
     * Дві рослини рівні, якщо це той самий підтип і всі поля збігаються.
     * Перевірка getClass не дає дереву й квітці з однаковими полями стати рівними.
     */
    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Plant plant = (Plant) other;
        return ageYears == plant.ageYears
                && heightCm == plant.heightCm
                && Double.compare(price, plant.price) == 0
                && wateringDays == plant.wateringDays
                && species.equals(plant.species);
    }

    /** Повертає хеш-код, узгоджений з {@link #equals(Object)}. */
    @Override
    public final int hashCode() {
        return Objects.hash(getClass(), species, ageYears, heightCm, price, wateringDays);
    }

    /** Повертає текстове подання рослини для звіту. */
    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s [%s]: %d р., %d см, %.2f грн, полив кожні %d дн.",
                species, getKind().label(), ageYears, heightCm, price, wateringDays);
    }
}
