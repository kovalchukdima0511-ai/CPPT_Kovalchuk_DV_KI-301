package ua.lpnu.kzp;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Універсальний CSV-експортер. Читає поля з {@link CsvColumn} рефлексією
 * і не знає жодного конкретного класу предметної області.
 */
public final class CsvExporter {

    private CsvExporter() {
    }

    /**
     * Повертає анотовані поля класу та його батьківських класів у порядку Java-імен.
     *
     * @param type клас записів
     * @return поля, доступні для читання
     * @throws DataStorageException якщо анотованих полів немає або доступ заборонено
     */
    public static List<Field> annotatedFields(Class<?> type) throws DataStorageException {
        Objects.requireNonNull(type, "type must not be null");
        List<Field> fields = new ArrayList<>();
        for (Class<?> current = type; current != null && current != Object.class;
                current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(CsvColumn.class)) {
                    fields.add(field);
                }
            }
        }
        if (fields.isEmpty()) {
            throw new DataStorageException("Class " + type.getSimpleName() + " has no @CsvColumn fields");
        }
        fields.sort(Comparator.comparing(Field::getName));
        for (Field field : fields) {
            if (!field.trySetAccessible()) {
                throw new DataStorageException("No access to field " + field.getName());
            }
        }
        return fields;
    }

    /**
     * Формує рядок заголовка з назв {@link CsvColumn}.
     *
     * @param type клас записів
     * @return заголовок CSV
     * @throws DataStorageException якщо поля неможливо отримати
     */
    public static String header(Class<?> type) throws DataStorageException {
        return annotatedFields(type).stream()
                .map(field -> escape(field.getAnnotation(CsvColumn.class).value()))
                .collect(Collectors.joining(","));
    }

    /**
     * Перетворює записи на CSV-текст.
     *
     * @param <T> тип записів
     * @param type клас записів (потрібен і для порожнього списку — для заголовка)
     * @param records записи
     * @return CSV-текст із заголовком
     * @throws DataStorageException якщо значення поля неможливо прочитати
     */
    public static <T> String toCsv(Class<T> type, List<? extends T> records) throws DataStorageException {
        Objects.requireNonNull(records, "records must not be null");
        List<Field> fields = annotatedFields(type);
        String nl = System.lineSeparator();
        StringBuilder csv = new StringBuilder(header(type)).append(nl);
        for (T record : records) {
            List<String> values = new ArrayList<>();
            for (Field field : fields) {
                try {
                    values.add(escape(field.get(record)));
                } catch (IllegalAccessException exception) {
                    throw new DataStorageException("Cannot read field " + field.getName(), exception);
                }
            }
            csv.append(String.join(",", values)).append(nl);
        }
        return csv.toString();
    }

    /**
     * Записує записи у файл CSV з кодуванням UTF-8.
     *
     * @param <T> тип записів
     * @param path шлях до файлу
     * @param type клас записів
     * @param records записи
     * @throws DataStorageException якщо поля неможливо прочитати або файл записати
     */
    public static <T> void write(Path path, Class<T> type, List<? extends T> records)
            throws DataStorageException {
        Objects.requireNonNull(path, "path must not be null");
        String csv = toCsv(type, records);
        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(path, csv, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException exception) {
            throw new DataStorageException("Cannot write CSV: " + path, exception);
        }
    }

    /**
     * Екранує значення за правилами CSV: кома, лапка або перенесення рядка —
     * значення береться в лапки, а лапка всередині подвоюється.
     *
     * @param value значення
     * @return текст для CSV
     */
    public static String escape(Object value) {
        String text = Objects.toString(value, "");
        boolean needsQuotes = text.contains(",") || text.contains("\"")
                || text.contains("\r") || text.contains("\n");
        return needsQuotes ? "\"" + text.replace("\"", "\"\"") + "\"" : text;
    }
}
