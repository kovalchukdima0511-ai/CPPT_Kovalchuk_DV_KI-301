"""Читання CSV і перетворення рядків на типізовані записи."""

import csv
from pathlib import Path

from .errors import DataError

FIELDS = ("species", "age", "height", "price", "watering_days")
"""Обов'язкові стовпці CSV у порядку заголовка."""

Record = dict[str, object]


def _to_int(row: dict[str, str], field: str, row_number: int, minimum: int) -> int:
    """Перетворює поле на ціле число, не менше за minimum."""
    try:
        value = int(row[field].strip())
    except (AttributeError, KeyError, TypeError, ValueError) as error:
        raise DataError(f"Рядок {row_number}: поле {field} має бути цілим числом") from error
    if value < minimum:
        raise DataError(
            f"Рядок {row_number}: поле {field} має бути не менше {minimum}, а не {value}"
        )
    return value


def _to_price(row: dict[str, str], row_number: int) -> float:
    """Перетворює ціну на невід'ємне скінченне число."""
    try:
        value = float(row["price"].strip())
    except (AttributeError, KeyError, TypeError, ValueError) as error:
        raise DataError(f"Рядок {row_number}: поле price має бути числом") from error
    if value < 0 or value != value or value in (float("inf"), float("-inf")):
        raise DataError(f"Рядок {row_number}: поле price має бути невід'ємним числом")
    return value


def parse_row(row: dict[str, str], row_number: int) -> Record:
    """Перевіряє один рядок DictReader і повертає запис із типізованими значеннями."""
    species = row.get("species")
    if species is None or not species.strip():
        raise DataError(f"Рядок {row_number}: поле species порожнє")
    return {
        "species": " ".join(species.split()),
        "age": _to_int(row, "age", row_number, minimum=0),
        "height": _to_int(row, "height", row_number, minimum=1),
        "price": _to_price(row, row_number),
        "watering_days": _to_int(row, "watering_days", row_number, minimum=1),
    }


def load_records(path: Path) -> list[Record]:
    """Читає CSV-файл UTF-8 і повертає записи.

    Порожній файл без заголовка є помилкою; файл лише із заголовком дає порожній список.
    """
    try:
        with path.open("r", encoding="utf-8", newline="") as source:
            reader = csv.DictReader(source)
            if reader.fieldnames is None:
                raise DataError(f"Файл {path} порожній: немає заголовка CSV")
            missing = [field for field in FIELDS if field not in reader.fieldnames]
            if missing:
                raise DataError(f"У заголовку CSV немає стовпців: {', '.join(missing)}")
            return [parse_row(row, row_number) for row_number, row in enumerate(reader, start=2)]
    except OSError as error:
        raise DataError(f"Не вдалося прочитати файл: {path}") from error
