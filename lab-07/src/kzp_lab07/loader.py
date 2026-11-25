"""Читання CSV і перетворення рядків на об'єкти Tree або Flower."""

import csv
from pathlib import Path

from .errors import DataError
from .models import Flower, Plant, Tree

FIELDS = ("species", "age", "height", "price", "watering_days")
"""Обов'язкові стовпці CSV — ті самі, що в роботі №6."""

TREE_GENERA = frozenset(
    "туя дуб клен ялина береза сосна липа каштан ялівець модрина горіх яблуня вишня груша".split()
)
FLOWER_GENERA = frozenset(
    "троянда хризантема лаванда тюльпан півонія орхідея лілія гортензія нарцис жоржина "
    "айстра фіалка".split()
)


def plant_class(species: str) -> type[Plant]:
    """Визначає підтип за першим словом виду (родом рослини)."""
    genus = species.split()[0].replace('"', "").lower() if species.split() else ""
    if genus in TREE_GENERA:
        return Tree
    if genus in FLOWER_GENERA:
        return Flower
    raise ValueError(f"невідомий рід рослини: {species!r}")


def _number(row: dict[str, str], field: str, kind: type, row_number: int) -> int | float:
    try:
        return kind(row[field].strip())
    except (AttributeError, KeyError, TypeError, ValueError) as error:
        raise DataError(f"Рядок {row_number}: поле {field} має бути числом") from error


def build_plant(row: dict[str, str], row_number: int) -> Plant:
    """Перетворює рядок DictReader на об'єкт потрібного підтипу."""
    species = (row.get("species") or "").strip()
    if not species:
        raise DataError(f"Рядок {row_number}: поле species порожнє")
    values = {
        "age": _number(row, "age", int, row_number),
        "height": _number(row, "height", int, row_number),
        "price": _number(row, "price", float, row_number),
        "watering_days": _number(row, "watering_days", int, row_number),
    }
    try:
        # Фабрика вибирає підтип лише тут, на межі завантаження.
        return plant_class(species)(species, **values)
    except (TypeError, ValueError) as error:
        raise DataError(f"Рядок {row_number}: {error}") from error


def load_plants(path: Path) -> list[Plant]:
    """Читає CSV UTF-8 і повертає рослини в порядку файлу."""
    try:
        with path.open("r", encoding="utf-8", newline="") as source:
            reader = csv.DictReader(source)
            if reader.fieldnames is None:
                raise DataError(f"Файл {path} порожній: немає заголовка CSV")
            missing = [field for field in FIELDS if field not in reader.fieldnames]
            if missing:
                raise DataError(f"У заголовку CSV немає стовпців: {', '.join(missing)}")
            return [build_plant(row, number) for number, row in enumerate(reader, start=2)]
    except OSError as error:
        raise DataError(f"Не вдалося прочитати файл: {path}") from error
