package ua.lpnu.kzp;

import java.util.ArrayList;
import java.util.List;

/**
 * Документний CSV-парсер (машина станів).
 *
 * <p>Усередині лапок кома та перенесення рядка є даними, а не роздільниками,
 * тому парсер повертає список записів, а не список фізичних рядків файлу.
 */
public final class CsvParser {

    private CsvParser() {
    }

    /**
     * Розбирає весь CSV-текст.
     *
     * @param text вміст файлу
     * @return записи, кожен — список полів
     * @throws DataStorageException якщо лапки не закрито або після них є зайві символи
     */
    public static List<List<String>> parseDocument(String text) throws DataStorageException {
        if (text == null) {
            throw new DataStorageException("CSV document must not be null");
        }
        List<List<String>> records = new ArrayList<>();
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        boolean quoteClosed = false;
        int line = 1;
        for (int index = 0; index < text.length(); index++) {
            char symbol = text.charAt(index);
            if (symbol == '"' && !quoted && !quoteClosed && current.length() == 0) {
                quoted = true;
            } else if (symbol == '"' && quoted && index + 1 < text.length() && text.charAt(index + 1) == '"') {
                current.append('"');
                index++;
            } else if (symbol == '"' && quoted) {
                quoted = false;
                quoteClosed = true;
            } else if (symbol == ',' && !quoted) {
                fields.add(current.toString());
                current.setLength(0);
                quoteClosed = false;
            } else if ((symbol == '\r' || symbol == '\n') && !quoted) {
                fields.add(current.toString());
                records.add(List.copyOf(fields));
                fields = new ArrayList<>();
                current.setLength(0);
                quoteClosed = false;
                if (symbol == '\r' && index + 1 < text.length() && text.charAt(index + 1) == '\n') {
                    index++;
                }
                line++;
            } else {
                if (quoteClosed) {
                    throw new DataStorageException("CSV line " + line + ": character after closing quote");
                }
                if (symbol == '\n') {
                    line++;
                }
                current.append(symbol);
            }
        }
        if (quoted) {
            throw new DataStorageException("CSV line " + line + ": unclosed quote");
        }
        if (!fields.isEmpty() || current.length() > 0) {
            fields.add(current.toString());
            records.add(List.copyOf(fields));
        }
        return List.copyOf(records);
    }
}
