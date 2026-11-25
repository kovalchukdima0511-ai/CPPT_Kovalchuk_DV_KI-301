"""Тести моделі: властивості, спеціальні методи, підтипи, протокол і dataclass."""

import dataclasses

import pytest

from kzp_lab07.analysis import make_care_summary, most_demanding
from kzp_lab07.models import CareSummary, Flower, Plant, PlantSummary, Tree


def test_constructor_keeps_values() -> None:
    """Конструктор нормалізує вид і зберігає поля."""
    tree = Tree("  Дуб   звичайний ", 5, 210, 950, 7)
    assert tree.species == "Дуб звичайний"
    assert (tree.age, tree.height, tree.price, tree.watering_days) == (5, 210, 950.0, 7)
    assert isinstance(tree.price, float)


def test_setter_rejects_invalid_values() -> None:
    """Setter-и відхиляють неправильні значення."""
    tree = Tree("Дуб", 5, 210, 950.0, 7)
    with pytest.raises(ValueError):
        tree.height = 0
    with pytest.raises(ValueError):
        tree.age = -1
    with pytest.raises(ValueError):
        tree.price = float("nan")
    with pytest.raises(ValueError):
        tree.watering_days = 0
    with pytest.raises(TypeError):
        tree.age = "5"
    with pytest.raises(TypeError):
        tree.height = True
    assert tree.height == 210


def test_setter_accepts_valid_change() -> None:
    """Дозволена зміна стану проходить через перевірку."""
    flower = Flower("Троянда", 1, 45, 180.0, 2)
    flower.height = 60
    assert flower.height == 60


def test_species_is_read_only_and_not_blank() -> None:
    """Вид не можна змінити й не можна залишити порожнім."""
    with pytest.raises(AttributeError):
        Tree("Дуб", 1, 10, 1.0, 1).species = "Клен"
    with pytest.raises(ValueError):
        Flower("   ", 1, 10, 1.0, 1)


def test_str_and_repr() -> None:
    """__str__ для читача, __repr__ для розробника."""
    tree = Tree("Дуб звичайний", 5, 210, 950.0, 7)
    assert str(tree) == "Дуб звичайний [дерево]: 5 р., 210 см, 950.00 грн, полив кожні 7 дн."
    assert repr(tree) == (
        "Tree(species='Дуб звичайний', age=5, height=210, price=950.0, watering_days=7)"
    )


def test_equality_by_type_and_fields() -> None:
    """__eq__: той самий підтип і ті самі поля."""
    assert Tree("Дуб", 5, 210, 950.0, 7) == Tree("Дуб", 5, 210, 950.0, 7)
    assert Tree("Дуб", 5, 210, 950.0, 7) != Tree("Дуб", 6, 210, 950.0, 7)
    assert Tree("Рослина", 1, 10, 1.0, 1) != Flower("Рослина", 1, 10, 1.0, 1)


def test_eq_with_other_type_returns_not_implemented() -> None:
    """Для несумісного типу __eq__ повертає NotImplemented, тому == дає False."""
    tree = Tree("Дуб", 5, 210, 950.0, 7)
    assert tree.__eq__("Дуб") is NotImplemented
    assert tree != "Дуб"


def test_mutable_plant_is_not_hashable() -> None:
    """Змінний об'єкт не можна покласти в множину."""
    with pytest.raises(TypeError):
        {Tree("Дуб", 5, 210, 950.0, 7)}


def test_subtypes_have_different_care_scores() -> None:
    """Різні формули care_score у підтипах."""
    tree = Tree("Дуб", 5, 200, 950.0, 2)
    flower = Flower("Троянда", 1, 200, 180.0, 2)
    assert tree.care_score() == pytest.approx(3.0)
    assert flower.care_score() == pytest.approx(3.75)


def test_base_class_has_no_formula() -> None:
    """Базовий клас лише оголошує операцію."""
    with pytest.raises(NotImplementedError):
        Plant("Щось", 1, 10, 1.0, 1).care_score()


def test_protocol_works_with_any_object_without_isinstance() -> None:
    """Качина типізація: стороння класова ієрархія теж підходить."""

    class Lawn:
        species = "Газон"
        kind = "газон"

        def care_score(self) -> float:
            return 20.0

    items = [Tree("Дуб", 5, 210, 950.0, 7), Lawn()]
    assert most_demanding(items).species == "Газон"
    assert make_care_summary(items).by_kind == (("газон", 20.0), ("дерево", pytest.approx(3.1)))


def test_summary_dataclasses_are_frozen() -> None:
    """frozen=True забороняє зміну полів."""
    care = CareSummary(1.0, (), None)
    with pytest.raises(dataclasses.FrozenInstanceError):
        care.total = 2.0
    summary = PlantSummary(0, 2, (), frozenset(), (), None, 0.0, ())
    with pytest.raises(dataclasses.FrozenInstanceError):
        summary.count = 1


def test_dataclass_equality() -> None:
    """dataclass порівнюється за значеннями полів."""
    assert CareSummary(1.0, (("дерево", 1.0),), None) == CareSummary(1.0, (("дерево", 1.0),), None)


def test_sorting_by_key() -> None:
    """Впорядкування через key: висота за спаданням, далі вид."""
    plants = [
        Tree("Ялина", 1, 180, 1.0, 6),
        Tree("Клен", 1, 180, 1.0, 5),
        Tree("Дуб", 1, 210, 1.0, 7),
    ]
    ordered = sorted(plants, key=lambda plant: (-plant.height, plant.species))
    assert [plant.species for plant in ordered] == ["Дуб", "Клен", "Ялина"]
