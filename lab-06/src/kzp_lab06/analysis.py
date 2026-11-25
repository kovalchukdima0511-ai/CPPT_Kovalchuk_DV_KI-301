"""Обчислення над записами: відбір, множина, словник агрегатів і підсумок."""

from collections.abc import Iterable

Record = dict[str, object]

FREQUENT_WATERING_DAYS = 2
"""Межа частого поливу в днях."""


def frequent_watering(
    records: Iterable[Record], max_days: int = FREQUENT_WATERING_DAYS
) -> list[Record]:
    """Відбір: рослини, які поливають не рідше ніж раз на max_days днів."""
    return [record for record in records if int(record["watering_days"]) <= max_days]


def unique_species(records: Iterable[Record]) -> set[str]:
    """Множина: унікальні види."""
    return {str(record["species"]) for record in records}


def average_price_by_species(records: list[Record]) -> dict[str, float]:
    """Словник: середня ціна за видом у стабільному (алфавітному) порядку."""
    return {
        species: sum(float(r["price"]) for r in records if r["species"] == species)
        / sum(1 for r in records if r["species"] == species)
        for species in sorted(unique_species(records))
    }


def tallest(records: list[Record]) -> Record | None:
    """Найвища рослина; за рівної висоти — перша у файлі. None для порожнього входу."""
    return max(records, key=lambda record: int(record["height"]), default=None)


def average_price(records: list[Record]) -> float:
    """Середня ціна; 0.0 для порожнього входу."""
    if not records:
        return 0.0
    return sum(float(record["price"]) for record in records) / len(records)


def make_summary(
    records: list[Record],
    *metrics: str,
    max_watering_days: int = FREQUENT_WATERING_DAYS,
    **labels: str,
) -> dict[str, object]:
    """Формує підсумок звіту.

    *metrics — числові поля, суми яких рахуються для відібраних записів;
    max_watering_days — поріг відбору (типове значення 2);
    **labels — підписи метрик у звіті, наприклад price="Вартість".
    """
    for metric in metrics:
        if metric not in ("age", "height", "price", "watering_days"):
            raise ValueError(f"Невідома метрика: {metric}")
    selected = frequent_watering(records, max_watering_days)
    top = tallest(records)
    return {
        "count": len(records),
        "max_watering_days": max_watering_days,
        "selected": sorted(selected, key=lambda r: (str(r["species"]), int(r["age"]))),
        "species": unique_species(records),
        "average_price_by_species": average_price_by_species(records),
        "tallest": top,
        "average_price": average_price(records),
        "metrics": {
            labels.get(metric, metric): float(sum(float(r[metric]) for r in selected))
            for metric in metrics
        },
    }
