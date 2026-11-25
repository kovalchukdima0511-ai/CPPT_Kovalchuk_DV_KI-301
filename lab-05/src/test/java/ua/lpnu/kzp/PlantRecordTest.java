package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Тести предметного запису, імпорту та round-trip. */
class PlantRecordTest {

    @TempDir
    Path tempDir;

    private static List<PlantRecord> controlRecords() throws IOException {
        return Main.load(Files.readAllLines(Main.DEFAULT_INPUT, StandardCharsets.UTF_8), new ArrayList<>())
                .stream().map(PlantRecord::from).toList();
    }

    @Test
    void roundTripReturnsEqualRecords() throws Exception {
        List<PlantRecord> original = controlRecords();
        Path file = tempDir.resolve("plants.csv");
        CsvExporter.write(file, PlantRecord.class, original);
        List<PlantRecord> restored = PlantRecord.read(file);
        assertEquals(10, restored.size());
        assertEquals(original, restored);
    }

    @Test
    void roundTripKeepsSubtypes() throws Exception {
        List<Plant> plants = controlRecords().stream().map(PlantRecord::toPlant).toList();
        Path file = tempDir.resolve("plants.csv");
        CsvExporter.write(file, PlantRecord.class, plants.stream().map(PlantRecord::from).toList());
        assertEquals(plants, PlantRecord.read(file).stream().map(PlantRecord::toPlant).toList());
    }

    @Test
    void roundTripWithCommaQuotesAndNewLine() throws Exception {
        List<PlantRecord> original = List.of(
                new PlantRecord("Троянда \"Глорія Дей\", плетиста", 2, 90, 240.0, 2),
                new PlantRecord("Туя західна\nсорт Смарагд", 3, 120, 450.0, 3));
        Path file = tempDir.resolve("special.csv");
        CsvExporter.write(file, PlantRecord.class, original);
        assertEquals(3, CsvParser.parseDocument(Files.readString(file, StandardCharsets.UTF_8)).size());
        assertEquals(original, PlantRecord.read(file));
    }

    @Test
    void headerOnlyFileGivesEmptyList() throws Exception {
        Path file = tempDir.resolve("empty.csv");
        CsvExporter.write(file, PlantRecord.class, List.of());
        assertEquals(List.of(), PlantRecord.read(file));
    }

    @Test
    void completelyEmptyFileIsRejected() throws IOException {
        Path file = Files.writeString(tempDir.resolve("void.csv"), "");
        assertThrows(DataStorageException.class, () -> PlantRecord.read(file));
    }

    @Test
    void wrongHeaderIsRejected() throws IOException {
        Path file = Files.writeString(tempDir.resolve("h.csv"), "a,b,c,d,e\n", StandardCharsets.UTF_8);
        DataStorageException error = assertThrows(DataStorageException.class, () -> PlantRecord.read(file));
        assertTrue(error.getMessage().startsWith("unexpected CSV header"));
    }

    @Test
    void wrongFieldCountIsRejected() {
        DataStorageException error = assertThrows(DataStorageException.class,
                () -> PlantRecord.fromFields(List.of("2", "90", "240.0", "Дуб")));
        assertEquals("expected 5 CSV fields, got 4", error.getMessage());
    }

    @Test
    void wrongNumberKeepsCause() {
        DataStorageException error = assertThrows(DataStorageException.class,
                () -> PlantRecord.fromFields(List.of("два", "90", "240.0", "Дуб", "2")));
        assertInstanceOf(NumberFormatException.class, error.getCause());
    }

    @Test
    void unknownKindIsRejectedWithCause() {
        DataStorageException error = assertThrows(DataStorageException.class,
                () -> PlantRecord.fromFields(List.of("2", "20", "80.0", "Кактус", "10")));
        assertInstanceOf(IllegalArgumentException.class, error.getCause());
    }

    @Test
    void brokenRecordInFileReportsRecordNumber() throws IOException {
        Path file = Files.writeString(tempDir.resolve("bad.csv"),
                "вік,висота,ціна,вид,полив\n5,210,950.0,Дуб звичайний,7\n3,-1,1.0,Туя,3\n", StandardCharsets.UTF_8);
        DataStorageException error = assertThrows(DataStorageException.class, () -> PlantRecord.read(file));
        assertTrue(error.getMessage().startsWith("CSV record 3:"));
        assertInstanceOf(DataStorageException.class, error.getCause());
    }

    @Test
    void missingFileKeepsIoCause() {
        DataStorageException error = assertThrows(DataStorageException.class,
                () -> PlantRecord.read(tempDir.resolve("missing.csv")));
        assertInstanceOf(NoSuchFileException.class, error.getCause());
    }

    @Test
    void equalsAndHashCode() {
        PlantRecord first = new PlantRecord("Дуб", 5, 210, 950.0, 7);
        assertEquals(first, new PlantRecord("Дуб", 5, 210, 950.0, 7));
        assertEquals(first.hashCode(), new PlantRecord("Дуб", 5, 210, 950.0, 7).hashCode());
        assertNotEquals(first, new PlantRecord("Дуб", 6, 210, 950.0, 7));
    }

    @Test
    void recordUsesModelValidation() {
        assertThrows(IllegalArgumentException.class, () -> new PlantRecord("Дуб", 5, 0, 950.0, 7));
    }
}
