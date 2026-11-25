package ua.lpnu.kzp;

import java.util.Locale;
import java.util.Set;

/**
 * Категорія рослини. Кожна константа знає свої види та вміє створити свій підтип.
 */
public enum PlantKind {

    /** Дерева та хвойні. */
    TREE("дерево", Set.of("туя", "дуб", "клен", "ялина", "береза", "сосна", "липа",
            "каштан", "ялівець", "модрина", "горіх", "яблуня", "вишня", "груша")) {
        @Override
        Plant create(String species, int age, int height, double price, int watering) {
            return new Tree(species, age, height, price, watering);
        }
    },

    /** Квіти. */
    FLOWER("квітка", Set.of("троянда", "хризантема", "лаванда", "тюльпан", "півонія",
            "орхідея", "лілія", "гортензія", "нарцис", "жоржина", "айстра", "фіалка")) {
        @Override
        Plant create(String species, int age, int height, double price, int watering) {
            return new Flower(species, age, height, price, watering);
        }
    };

    private final String label;
    private final Set<String> genera;

    PlantKind(String label, Set<String> genera) {
        this.label = label;
        this.genera = genera;
    }

    /** @return українська назва категорії для звіту */
    public String label() {
        return label;
    }

    /**
     * Створює об'єкт підтипу цієї категорії.
     *
     * @param species вид
     * @param age вік
     * @param height висота
     * @param price ціна
     * @param watering полив
     * @return рослина цієї категорії
     */
    abstract Plant create(String species, int age, int height, double price, int watering);

    /**
     * Визначає категорію за першим словом виду (рід рослини).
     *
     * @param species вид, наприклад «Туя західна»
     * @return категорія
     * @throws IllegalArgumentException якщо рід невідомий
     */
    public static PlantKind fromSpecies(String species) {
        String genus = species.trim().split("\\s+", 2)[0].replace("\"", "").toLowerCase(Locale.ROOT);
        for (PlantKind kind : values()) {
            if (kind.genera.contains(genus)) {
                return kind;
            }
        }
        throw new IllegalArgumentException("unknown plant kind for species: '" + species.trim() + "'");
    }
}
