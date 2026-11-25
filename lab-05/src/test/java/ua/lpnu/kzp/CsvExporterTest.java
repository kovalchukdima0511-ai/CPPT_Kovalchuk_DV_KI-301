package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Тести рефлексивного експортера й анотації. */
class CsvExporterTest {

    @TempDir
    Path tempDir;

    /** Клас іншої предметної області: експортер має працювати і з ним. */
    static final class Book {
        @CsvColumn("назва")
        private final String title;
        @CsvColumn("рік")
        private final int year;
        private final String notExported = "секрет";

        Book(String title, int year) {
            this.title = title;
            this.year = year;
        }
    }

    /** Клас без анотованих полів. */
    static final class Empty {
        private final int value = 1;
    }

    @Test
    void annotationIsVisibleAtRuntime() throws NoSuchFieldException {
        Field field = PlantRecord.class.getDeclaredField("species");
        assertNotNull(field.getAnnotation(CsvColumn.class));
        assertEquals("вид", field.getAnnotation(CsvColumn.class).value());
    }

    @Test
    void headerIsSortedByJavaFieldName() throws DataStorageException {
        assertEquals("вік,висота,ціна,вид,полив", CsvExporter.header(PlantRecord.class));
    }

    @Test
    void exporterWorksWithAnyAnnotatedClass() throws DataStorageException {
        String csv = CsvExporter.toCsv(Book.class, List.of(new Book("Кобзар", 1840)));
        assertEquals(List.of("назва,рік", "Кобзар,1840"), csv.lines().toList());
    }

    @Test
    void notAnnotatedFieldIsNotExported() throws DataStorageException {
        assertTrue(!CsvExporter.toCsv(Book.class, List.of(new Book("A", 1))).contains("секрет"));
    }

    @Test
    void emptyListGivesHeaderOnly() throws DataStorageException {
        assertEquals(List.of("вік,висота,ціна,вид,полив"),
                CsvExporter.toCsv(PlantRecord.class, List.of()).lines().toList());
    }

    @Test
    void classWithoutColumnsIsRejected() {
        DataStorageException error = assertThrows(DataStorageException.class,
                () -> CsvExporter.toCsv(Empty.class, List.of(new Empty())));
        assertEquals("Class Empty has no @CsvColumn fields", error.getMessage());
    }

    @Test
    void escapeCommaQuoteAndNewLine() {
        assertEquals("просто", CsvExporter.escape("просто"));
        assertEquals("\"Туя, західна\"", CsvExporter.escape("Туя, західна"));
        assertEquals("\"Троянда \"\"Глорія\"\"\"", CsvExporter.escape("Троянда \"Глорія\""));
        assertEquals("\"рядок 1\nрядок 2\"", CsvExporter.escape("рядок 1\nрядок 2"));
        assertEquals("", CsvExporter.escape(null));
    }

    @Test
    void writeUsesUtf8AndTruncatesOldContent() throws Exception {
        Path file = tempDir.resolve("sub").resolve("plants.csv");
        CsvExporter.write(file, PlantRecord.class, List.of(
                new PlantRecord("Дуб звичайний", 5, 210, 950.0, 7),
                new PlantRecord("Туя західна", 3, 120, 450.0, 3)));
        CsvExporter.write(file, PlantRecord.class, List.of(new PlantRecord("Дуб звичайний", 5, 210, 950.0, 7)));
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        assertEquals(List.of("вік,висота,ціна,вид,полив", "5,210,950.0,Дуб звичайний,7"), lines);
    }

    @Test
    void fileErrorKeepsIoExceptionAsCause() throws IOException {
        Path blocker = Files.writeString(tempDir.resolve("file.txt"), "x");
        DataStorageException error = assertThrows(DataStorageException.class,
                () -> CsvExporter.write(blocker.resolve("plants.csv"), PlantRecord.class, List.of()));
        assertTrue(error.getMessage().startsWith("Cannot write CSV"));
        assertInstanceOf(IOException.class, error.getCause());
    }
}
