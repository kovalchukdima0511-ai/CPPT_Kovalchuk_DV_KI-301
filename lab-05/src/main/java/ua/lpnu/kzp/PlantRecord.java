package ua.lpnu.kzp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Плоский запис рослини для CSV-експорту (варіант 12).
 *
 * <p>Поля експортуються в порядку Java-імен: ageYears, heightCm, price, species, wateringDays,
 * тому заголовок — «вік,висота,ціна,вид,полив». Ключ round-trip — species + ageYears.
 */
public final class PlantRecord {

    @CsvColumn("вид")
    private final String species;

    @CsvColumn("вік")
    private final int ageYears;

    @CsvColumn("висота")
    private final int heightCm;

    @CsvColumn("ціна")
    private final double price;

    @CsvColumn("полив")
    private final int wateringDays;

    /**
     * Створює запис. Правила валідації не дублюються: перевірку виконує модель Plant.
     *
     * @param species вид
     * @param ageYears вік
     * @param heightCm висота
     * @param price ціна
     * @param wateringDays полив
     * @throws IllegalArgumentException якщо значення порушують інваріанти Plant
     */
    public PlantRecord(String species, int ageYears, int heightCm, double price, int wateringDays) {
        Plant checked = PlantKind.fromSpecies(Objects.requireNonNull(species, "species must not be null"))
                .create(species, ageYears, heightCm, price, wateringDays);
        this.species = checked.getSpecies();
        this.ageYears = ageYears;
        this.heightCm = heightCm;
        this.price = price;
        this.wateringDays = wateringDays;
    }

    /**
     * Створює запис з об'єкта моделі.
     *
     * @param plant рослина
     * @return плоский запис
     */
    public static PlantRecord from(Plant plant) {
        return new PlantRecord(plant.getSpecies(), plant.getAgeYears(), plant.getHeightCm(),
                plant.getPrice(), plant.getWateringDays());
    }

    /** @return рослина відповідного підтипу */
    public Plant toPlant() {
        return PlantKind.fromSpecies(species).create(species, ageYears, heightCm, price, wateringDays);
    }

    /**
     * Створює запис із розібраних CSV-полів у порядку заголовка «вік,висота,ціна,вид,полив».
     *
     * @param fields поля
     * @return запис
     * @throws DataStorageException якщо кількість полів, число або значення некоректні
     */
    public static PlantRecord fromFields(List<String> fields) throws DataStorageException {
        Objects.requireNonNull(fields, "fields must not be null");
        if (fields.size() != 5) {
            throw new DataStorageException("expected 5 CSV fields, got " + fields.size());
        }
        try {
            return new PlantRecord(
                    fields.get(3),
                    Integer.parseInt(fields.get(0).trim()),
                    Integer.parseInt(fields.get(1).trim()),
                    Double.parseDouble(fields.get(2).trim()),
                    Integer.parseInt(fields.get(4).trim()));
        } catch (IllegalArgumentException exception) {
            // NumberFormatException теж є IllegalArgumentException — причина зберігається.
            throw new DataStorageException("invalid plant values: " + exception.getMessage(), exception);
        }
    }

    /**
     * Читає CSV, створений {@link CsvExporter}, і відновлює записи.
     *
     * @param path шлях до файлу
     * @return записи в порядку файлу
     * @throws DataStorageException якщо файл не читається або дані некоректні
     */
    public static List<PlantRecord> read(Path path) throws DataStorageException {
        String text;
        try {
            text = Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new DataStorageException("Cannot read CSV: " + path, exception);
        }
        List<List<String>> rows = CsvParser.parseDocument(text);
        if (rows.isEmpty()) {
            throw new DataStorageException("CSV has no header: " + path);
        }
        String expected = CsvExporter.header(PlantRecord.class);
        String actual = String.join(",", rows.get(0));
        if (!expected.equals(actual)) {
            throw new DataStorageException("unexpected CSV header: '" + actual + "', expected '" + expected + "'");
        }
        List<PlantRecord> records = new ArrayList<>();
        for (int index = 1; index < rows.size(); index++) {
            try {
                records.add(fromFields(rows.get(index)));
            } catch (DataStorageException exception) {
                throw new DataStorageException("CSV record " + (index + 1) + ": " + exception.getMessage(),
                        exception);
            }
        }
        return List.copyOf(records);
    }

    /** @return вид */
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

    /** @return ціна */
    public double getPrice() {
        return price;
    }

    /** @return полив у днях */
    public int getWateringDays() {
        return wateringDays;
    }

    /** Записи рівні, якщо збігаються всі експортовані поля. */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PlantRecord record)) {
            return false;
        }
        return ageYears == record.ageYears
                && heightCm == record.heightCm
                && Double.compare(price, record.price) == 0
                && wateringDays == record.wateringDays
                && species.equals(record.species);
    }

    /** Повертає хеш-код, узгоджений з equals. */
    @Override
    public int hashCode() {
        return Objects.hash(species, ageYears, heightCm, price, wateringDays);
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "PlantRecord[%s, %d, %d, %.2f, %d]",
                species, ageYears, heightCm, price, wateringDays);
    }
}
