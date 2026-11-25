package ua.lpnu.kzp;

/**
 * Дерево. Догляд залежить від висоти: чим вище дерево, тим довше обрізка й полив.
 */
public final class Tree extends Plant {

    /** Базовий догляд дерева, годин на місяць. */
    static final double BASE_HOURS = 1.0;

    /** Додаткові години на кожні 100 см висоти. */
    static final double HOURS_PER_METER = 1.0;

    /**
     * Створює дерево; усі перевірки виконує конструктор базового класу.
     *
     * @param species вид
     * @param ageYears вік
     * @param heightCm висота
     * @param price ціна
     * @param wateringDays полив
     */
    public Tree(String species, int ageYears, int heightCm, double price, int wateringDays) {
        super(species, ageYears, heightCm, price, wateringDays);
    }

    /** Догляд дерева: 1 год + 1 год на кожен метр висоти. */
    @Override
    public double careHours() {
        return BASE_HOURS + HOURS_PER_METER * getHeightCm() / 100.0;
    }

    @Override
    public PlantKind getKind() {
        return PlantKind.TREE;
    }
}
