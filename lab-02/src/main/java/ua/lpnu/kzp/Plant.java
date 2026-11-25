package ua.lpnu.kzp;

import java.util.Locale;
import java.util.Objects;

/**
 * Рослина розсадника: вид, вік, висота, ціна та періодичність поливу.
 *
 * <p>Об'єкт незмінний: усі поля {@code private final}, сеттерів немає,
 * а всі правила перевіряються в конструкторі, тому некоректна рослина не створюється.
 */
public final class Plant {

    /** Кількість полів у рядку CSV. */
    public static final int FIELD_COUNT = 5;

    private final String species;
    private final int ageYears;
    private final int heightCm;
    private final double price;
    private final int wateringDays;

    /**
     * Створює рослину після перевірки всіх полів.
     *
     * @param species вид рослини, не порожній
     * @param ageYears вік у роках, не від'ємний
     * @param heightCm висота в сантиметрах, більша за нуль
     * @param price ціна в гривнях, не від'ємна та скінченна
     * @param wateringDays періодичність поливу в днях, не менше 1
     * @throws IllegalArgumentException якщо хоча б одне значення некоректне
     */
    public Plant(String species, int ageYears, int heightCm, double price, int wateringDays) {
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
     * Створює рослину з рядка {@code вид;вік;висота;ціна;полив}.
     *
     * <p>Фабрика перевіряє структуру рядка й типи чисел, а конструктор — значення.
     *
     * @param line рядок CSV
     * @return коректна рослина
     * @throws IllegalArgumentException якщо рядок має хибну структуру або значення
     */
    public static Plant fromCsv(String line) {
        Objects.requireNonNull(line, "line must not be null");
        String[] fields = line.split(";", -1);
        if (fields.length != FIELD_COUNT) {
            throw new IllegalArgumentException(
                    "expected " + FIELD_COUNT + " fields, got " + fields.length);
        }
        return new Plant(
                fields[0],
                parseInt(fields[1], "age"),
                parseInt(fields[2], "height"),
                parseDouble(fields[3], "price"),
                parseInt(fields[4], "watering"));
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

    /** @return вид рослини */
    public String getSpecies() {
        return species;
    }

    /** @return вік у роках */
    public int getAgeYears() {
        return ageYears;
    }

    /** @return висота в сантиметрах */
    public int getHeightCm() {
        return heightCm;
    }

    /** @return ціна в гривнях */
    public double getPrice() {
        return price;
    }

    /** @return періодичність поливу в днях */
    public int getWateringDays() {
        return wateringDays;
    }

    /** @return пара «висота — полив» як незмінне значення */
    public HeightWatering heightWatering() {
        return new HeightWatering(heightCm, wateringDays);
    }

    /** Повертає текстове подання рослини для звіту. */
    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s: %d р., %d см, %.2f грн, полив кожні %d дн.",
                species, ageYears, heightCm, price, wateringDays);
    }
}
