package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Тести ієрархії: підтипи, enum, поліморфізм, equals/hashCode. */
class HierarchyTest {

    @Test
    void subtypesCalculateCareDifferently() {
        CareNeed tree = new Tree("Дуб", 5, 200, 950.0, 2);
        CareNeed flower = new Flower("Троянда", 1, 200, 180.0, 2);
        assertEquals(3.0, tree.careHours(), 1e-9);
        assertEquals(3.75, flower.careHours(), 1e-9);
        assertNotEquals(tree.careHours(), flower.careHours());
    }

    @Test
    void treeCareGrowsWithHeight() {
        assertTrue(new Tree("Дуб", 1, 300, 1.0, 7).careHours()
                > new Tree("Дуб", 1, 100, 1.0, 7).careHours());
    }

    @Test
    void flowerCareGrowsWithWateringFrequency() {
        assertEquals(7.5, new Flower("Тюльпан", 1, 40, 1.0, 1).careHours(), 1e-9);
        assertEquals(2.5, new Flower("Лаванда", 1, 40, 1.0, 3).careHours(), 1e-9);
    }

    @Test
    void subtypeConstructorUsesBaseValidation() {
        assertThrows(IllegalArgumentException.class, () -> new Tree("Дуб", 1, 0, 1.0, 1));
        assertThrows(IllegalArgumentException.class, () -> new Flower(" ", 1, 10, 1.0, 1));
    }

    @Test
    void kindIsDetectedBySpeciesGenus() {
        assertSame(PlantKind.TREE, PlantKind.fromSpecies("Туя західна"));
        assertSame(PlantKind.FLOWER, PlantKind.fromSpecies("Троянда \"Глорія Дей\", плетиста"));
        assertSame(PlantKind.TREE, PlantKind.fromSpecies("ДУБ звичайний"));
    }

    @Test
    void unknownGenusIsRejected() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> Plant.fromCsv("Кактус;2;20;80.00;10"));
        assertEquals("unknown plant kind for species: 'Кактус'", error.getMessage());
    }

    @Test
    void fromCsvCreatesProperSubtype() {
        assertEquals(Tree.class, Plant.fromCsv("Туя західна;3;120;450.00;3").getClass());
        assertEquals(Flower.class, Plant.fromCsv("Тюльпан Дарвіна;1;40;35.50;1").getClass());
    }

    @Test
    void enumLabels() {
        assertEquals("дерево", PlantKind.TREE.label());
        assertEquals("квітка", PlantKind.FLOWER.label());
        assertEquals(2, PlantKind.values().length);
    }

    @Test
    void equalPlantsHaveSameHashCode() {
        Plant first = new Tree("Дуб", 5, 210, 950.0, 7);
        Plant second = new Tree("Дуб", 5, 210, 950.0, 7);
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void hashSetDoesNotDuplicateEqualPlants() {
        Set<Plant> set = new HashSet<>();
        set.add(new Flower("Троянда чайна", 1, 45, 180.0, 2));
        set.add(new Flower("Троянда чайна", 1, 45, 180.0, 2));
        set.add(new Flower("Троянда чайна", 2, 55, 210.0, 2));
        assertEquals(2, set.size());
    }

    @Test
    void differentSubtypesAreNotEqual() {
        assertNotEquals(new Tree("Рослина", 1, 10, 1.0, 1), new Flower("Рослина", 1, 10, 1.0, 1));
    }

    @Test
    void plantCanBeHashMapKey() {
        Map<Plant, Integer> stock = new HashMap<>();
        stock.put(new Tree("Дуб", 5, 210, 950.0, 7), 3);
        assertEquals(Integer.valueOf(3), stock.get(new Tree("Дуб", 5, 210, 950.0, 7)));
    }

    @Test
    void careSummaryWorksThroughBaseType() {
        List<Plant> plants = List.of(
                new Tree("Дуб", 5, 200, 950.0, 7),
                new Flower("Тюльпан", 1, 40, 35.5, 1));
        CareSummary care = CareSummary.from(plants);
        assertEquals(10.5, care.totalCareHours(), 1e-9);
        assertEquals("Тюльпан", care.highestCareSpecies());
        assertEquals(1, care.trees());
        assertEquals(1, care.flowers());
    }

    @Test
    void careSummaryOfEmptyList() {
        assertEquals(new CareSummary(0.0, "-", 0.0, 0, 0, 0), CareSummary.from(List.of()));
    }
}
