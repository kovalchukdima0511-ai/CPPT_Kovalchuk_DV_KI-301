package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Тести документного CSV-парсера. */
class CsvParserTest {

    @Test
    void simpleRows() throws DataStorageException {
        assertEquals(List.of(List.of("a", "b"), List.of("1", "2")), CsvParser.parseDocument("a,b\n1,2\n"));
    }

    @Test
    void commaInsideQuotesIsData() throws DataStorageException {
        assertEquals(List.of(List.of("2", "Туя, західна", "3")),
                CsvParser.parseDocument("2,\"Туя, західна\",3"));
    }

    @Test
    void doubledQuoteBecomesOneQuote() throws DataStorageException {
        assertEquals(List.of(List.of("Троянда \"Глорія\"")), CsvParser.parseDocument("\"Троянда \"\"Глорія\"\"\""));
    }

    @Test
    void multilineFieldIsOneRecord() throws DataStorageException {
        List<List<String>> rows = CsvParser.parseDocument("a,\"рядок 1\nрядок 2\",b\r\nc,d,e\r\n");
        assertEquals(2, rows.size());
        assertEquals("рядок 1\nрядок 2", rows.get(0).get(1));
    }

    @Test
    void windowsLineEndings() throws DataStorageException {
        assertEquals(List.of(List.of("a"), List.of("b")), CsvParser.parseDocument("a\r\nb\r\n"));
    }

    @Test
    void emptyFieldsAreKept() throws DataStorageException {
        assertEquals(List.of(List.of("", "x", "")), CsvParser.parseDocument(",x,"));
    }

    @Test
    void emptyDocumentHasNoRecords() throws DataStorageException {
        assertEquals(List.of(), CsvParser.parseDocument(""));
    }

    @Test
    void unclosedQuoteIsRejected() {
        DataStorageException error = assertThrows(DataStorageException.class,
                () -> CsvParser.parseDocument("a,\"bad\n"));
        assertEquals("CSV line 2: unclosed quote", error.getMessage());
    }

    @Test
    void characterAfterClosingQuoteIsRejected() {
        assertThrows(DataStorageException.class, () -> CsvParser.parseDocument("\"a\"b,c"));
    }

    @Test
    void nullDocumentIsRejected() {
        assertThrows(DataStorageException.class, () -> CsvParser.parseDocument(null));
    }
}
