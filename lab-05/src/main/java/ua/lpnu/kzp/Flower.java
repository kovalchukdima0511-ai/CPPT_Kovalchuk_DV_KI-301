package ua.lpnu.kzp;

/**
 * Квітка. Догляд залежить від частоти поливу: кожен полив забирає 15 хвилин.
 */
public final class Flower extends Plant {

    /** Тривалість одного поливу, годин. */
    static final double HOURS_PER_WATERING = 0.25;

    /** Кількість днів у розрахунковому місяці. */
    static final int DAYS_IN_MONTH = 30;

    /**
     * Створює квітку; усі перевірки виконує конструктор базового класу.
     *
     * @param species вид
     * @param ageYears вік
     * @param heightCm висота
     * @param price ціна
     * @param wateringDays полив
     */
    public Flower(String species, int ageYears, int heightCm, double price, int wateringDays) {
        super(species, ageYears, heightCm, price, wateringDays);
    }

    /** Догляд квітки: (30 / полив) поливів на місяць по 0.25 год. */
    @Override
    public double careHours() {
        return (double) DAYS_IN_MONTH / getWateringDays() * HOURS_PER_WATERING;
    }

    @Override
    public PlantKind getKind() {
        return PlantKind.FLOWER;
    }
}
