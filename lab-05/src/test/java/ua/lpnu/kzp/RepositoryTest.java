package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

/** Тести параметризованого сховища. */
class RepositoryTest {

    @Test
    void addAndSnapshot() {
        Repository<Plant> repository = new Repository<>();
        repository.add(new Tree("Дуб", 5, 210, 950.0, 7));
        repository.add(new Flower("Тюльпан", 1, 40, 35.5, 1));
        assertEquals(2, repository.all().size());
        assertEquals(2, repository.size());
    }

    @Test
    void snapshotIsUnmodifiable() {
        Repository<String> repository = new Repository<>();
        repository.add("a");
        List<String> snapshot = repository.all();
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add("b"));
        assertEquals(1, repository.size());
    }

    @Test
    void snapshotDoesNotSeeLaterChanges() {
        Repository<String> repository = new Repository<>();
        repository.add("a");
        List<String> snapshot = repository.all();
        repository.add("b");
        assertEquals(1, snapshot.size());
    }

    @Test
    void findAcceptsPredicateOfSupertype() {
        Repository<Tree> trees = new Repository<>();
        trees.add(new Tree("Дуб", 5, 210, 950.0, 7));
        trees.add(new Tree("Туя", 3, 120, 450.0, 3));
        Predicate<Plant> tall = plant -> plant.getHeightCm() > 150;
        assertEquals(1, trees.find(tall).size());
    }

    @Test
    void nullItemIsRejected() {
        Repository<Plant> repository = new Repository<>();
        assertThrows(NullPointerException.class, () -> repository.add(null));
    }

    @Test
    void emptyRepositoryFindsNothing() {
        assertEquals(List.of(), new Repository<Plant>().find(plant -> true));
    }
}
