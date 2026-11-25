"""Модель предметної області: базовий клас, підтипи, протокол і dataclass-підсумки."""

import math
from dataclasses import dataclass
from typing import Protocol

FREQUENT_WATERING_DAYS = 2
"""Межа частого поливу в днях."""


class CareScored(Protocol):
    """Протокол: об'єкт має вид і вміє оцінити потребу в догляді."""

    @property
    def species(self) -> str:
        """Вид рослини."""
        ...

    @property
    def kind(self) -> str:
        """Категорія для звіту."""
        ...

    def care_score(self) -> float:
        """Години догляду на місяць."""
        ...


def _check_int(name: str, value: object, minimum: int) -> int:
    """Перевіряє, що value — ціле число не менше minimum."""
    if isinstance(value, bool) or not isinstance(value, int):
        raise TypeError(f"{name} має бути цілим числом")
    if value < minimum:
        raise ValueError(f"{name} має бути не менше {minimum}, а не {value}")
    return value


class Plant:
    """Базова рослина: спільний стан, перевірки у властивостях і контракт care_score."""

    kind = "рослина"

    def __init__(
        self, species: str, age: int, height: int, price: float, watering_days: int
    ) -> None:
        """Створює рослину; кожне значення перевіряє відповідний setter."""
        normalized = " ".join(str(species).split())
        if not normalized:
            raise ValueError("species не може бути порожнім")
        self._species = normalized
        self.age = age
        self.height = height
        self.price = price
        self.watering_days = watering_days

    @property
    def species(self) -> str:
        """Вид рослини (лише читання)."""
        return self._species

    @property
    def age(self) -> int:
        """Вік у роках."""
        return self._age

    @age.setter
    def age(self, value: int) -> None:
        """Встановлює невід'ємний вік."""
        self._age = _check_int("age", value, 0)

    @property
    def height(self) -> int:
        """Висота в сантиметрах."""
        return self._height

    @height.setter
    def height(self, value: int) -> None:
        """Встановлює додатну висоту."""
        self._height = _check_int("height", value, 1)

    @property
    def price(self) -> float:
        """Ціна в гривнях."""
        return self._price

    @price.setter
    def price(self, value: float) -> None:
        """Встановлює невід'ємну скінченну ціну."""
        if isinstance(value, bool) or not isinstance(value, int | float):
            raise TypeError("price має бути числом")
        if not math.isfinite(value) or value < 0:
            raise ValueError("price має бути невід'ємним скінченним числом")
        self._price = float(value)

    @property
    def watering_days(self) -> int:
        """Періодичність поливу в днях."""
        return self._watering_days

    @watering_days.setter
    def watering_days(self, value: int) -> None:
        """Встановлює полив не рідше ніж раз на 1 день."""
        self._watering_days = _check_int("watering_days", value, 1)

    def needs_frequent_watering(self, max_days: int = FREQUENT_WATERING_DAYS) -> bool:
        """Чи поливають рослину не рідше ніж раз на max_days днів."""
        return self.watering_days <= max_days

    def care_score(self) -> float:
        """Потреба в догляді, годин на місяць. Реалізується в підтипах."""
        raise NotImplementedError

    def _fields(self) -> tuple[str, int, int, float, int]:
        return (self.species, self.age, self.height, self.price, self.watering_days)

    def __str__(self) -> str:
        """Коротке подання для читача."""
        return (
            f"{self.species} [{self.kind}]: {self.age} р., {self.height} см, "
            f"{self.price:.2f} грн, полив кожні {self.watering_days} дн."
        )

    def __repr__(self) -> str:
        """Діагностичне подання."""
        return (
            f"{type(self).__name__}(species={self.species!r}, age={self.age!r}, "
            f"height={self.height!r}, price={self.price!r}, watering_days={self.watering_days!r})"
        )

    def __eq__(self, other: object) -> bool:
        """Рівні, якщо це той самий підтип і всі поля збігаються."""
        if not isinstance(other, Plant):
            return NotImplemented
        return type(self) is type(other) and self._fields() == other._fields()

    # Стан змінюється через setter-и, тому об'єкт не можна робити ключем словника.
    __hash__ = None  # type: ignore[assignment]


class Tree(Plant):
    """Дерево: догляд залежить від висоти (обрізка, полив великої крони)."""

    kind = "дерево"

    def care_score(self) -> float:
        """1 год + 1 год на кожен метр висоти."""
        return 1.0 + self.height / 100


class Flower(Plant):
    """Квітка: догляд залежить від частоти поливу (15 хв на кожен полив)."""

    kind = "квітка"

    def care_score(self) -> float:
        """(30 / полив) поливів на місяць по 0.25 год."""
        return 30 / self.watering_days * 0.25


@dataclass(frozen=True)
class PlantSummary:
    """Незмінний підсумок основного звіту (той самий зміст, що в роботі №6)."""

    count: int
    max_watering_days: int
    selected: tuple[Plant, ...]
    species: frozenset[str]
    average_price_by_species: tuple[tuple[str, float], ...]
    tallest: Plant | None
    average_price: float
    metrics: tuple[tuple[str, float], ...]


@dataclass(frozen=True)
class CareSummary:
    """Незмінний підсумок потреби в догляді."""

    total: float
    by_kind: tuple[tuple[str, float], ...]
    most_demanding: CareScored | None
