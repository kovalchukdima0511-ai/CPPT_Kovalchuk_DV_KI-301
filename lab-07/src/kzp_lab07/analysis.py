"""Обчислення над об'єктами. Жодна функція не перевіряє конкретний підтип."""

from collections.abc import Iterable, Sequence

from .models import FREQUENT_WATERING_DAYS, CareScored, CareSummary, Plant, PlantSummary

METRICS = ("age", "height", "price", "watering_days")


def frequent_watering(
    plants: Iterable[Plant], max_days: int = FREQUENT_WATERING_DAYS
) -> list[Plant]:
    """Відбір: рослини з поливом не рідше ніж раз на max_days днів."""
    return [plant for plant in plants if plant.needs_frequent_watering(max_days)]


def unique_species(plants: Iterable[Plant]) -> set[str]:
    """Множина унікальних видів."""
    return {plant.species for plant in plants}


def average_price_by_species(plants: Sequence[Plant]) -> dict[str, float]:
    """Середня ціна за видом в алфавітному порядку видів."""
    result = {}
    for species in sorted(unique_species(plants)):
        prices = [plant.price for plant in plants if plant.species == species]
        result[species] = sum(prices) / len(prices)
    return result


def tallest(plants: Sequence[Plant]) -> Plant | None:
    """Найвища рослина (перша за рівної висоти) або None."""
    return max(plants, key=lambda plant: plant.height, default=None)


def average_price(plants: Sequence[Plant]) -> float:
    """Середня ціна або 0.0 для порожнього входу."""
    return sum(plant.price for plant in plants) / len(plants) if plants else 0.0


def make_summary(
    plants: Sequence[Plant],
    *metrics: str,
    max_watering_days: int = FREQUENT_WATERING_DAYS,
    **labels: str,
) -> PlantSummary:
    """Формує незмінний підсумок основного звіту."""
    unknown = [metric for metric in metrics if metric not in METRICS]
    if unknown:
        raise ValueError(f"Невідома метрика: {unknown[0]}")
    selected = frequent_watering(plants, max_watering_days)
    return PlantSummary(
        count=len(plants),
        max_watering_days=max_watering_days,
        selected=tuple(sorted(selected, key=lambda plant: (plant.species, plant.age))),
        species=frozenset(unique_species(plants)),
        average_price_by_species=tuple(average_price_by_species(plants).items()),
        tallest=tallest(plants),
        average_price=average_price(plants),
        metrics=tuple(
            (labels.get(metric, metric), float(sum(getattr(p, metric) for p in selected)))
            for metric in metrics
        ),
    )


def most_demanding(items: Iterable[CareScored]) -> CareScored | None:
    """Об'єкт із найбільшою потребою в догляді — працює з будь-яким CareScored."""
    return max(items, key=lambda item: item.care_score(), default=None)


def care_by_kind(items: Iterable[CareScored]) -> dict[str, float]:
    """Сумарна потреба в догляді за категорією (kind) у порядку назв."""
    totals: dict[str, float] = {}
    for item in items:
        totals[item.kind] = totals.get(item.kind, 0.0) + item.care_score()
    return dict(sorted(totals.items()))


def make_care_summary(items: Sequence[CareScored]) -> CareSummary:
    """Підсумок догляду через спільний протокол (качина типізація)."""
    return CareSummary(
        total=sum(item.care_score() for item in items),
        by_kind=tuple(care_by_kind(items).items()),
        most_demanding=most_demanding(items),
    )
