package ua.lpnu.kzp;

/**
 * Незмінна пара «висота — періодичність поливу» однієї рослини.
 *
 * @param heightCm висота в сантиметрах
 * @param wateringDays періодичність поливу в днях
 */
public record HeightWatering(int heightCm, int wateringDays) {

    /** Межа частого поливу в днях. */
    public static final int FREQUENT_WATERING_DAYS = 2;

    /** Перевіряє, що обидва значення додатні. */
    public HeightWatering {
        if (heightCm <= 0 || wateringDays <= 0) {
            throw new IllegalArgumentException("height and watering must be positive");
        }
    }

    /** @return {@code true}, якщо рослину поливають не рідше ніж раз на 2 дні */
    public boolean needsFrequentWatering() {
        return wateringDays <= FREQUENT_WATERING_DAYS;
    }
}
