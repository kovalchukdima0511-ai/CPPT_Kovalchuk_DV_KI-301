"""Тести перетворення CSV на об'єкти."""

import pytest
from conftest import HEADER

from kzp_lab07.errors import DataError
from kzp_lab07.loader import load_plants, plant_class
from kzp_lab07.models import Flower, Tree


def test_control_file_gives_trees_and_flowers(control_csv) -> None:
    """10 об'єктів: 4 дерева та 6 квітів."""
    plants = load_plants(control_csv)
    assert len(plants) == 10
    assert sum(1 for plant in plants if plant.kind == "дерево") == 4
    assert plants[0] == Tree("Туя західна", 3, 120, 450.0, 3)


def test_subtype_is_chosen_by_genus() -> None:
    """Фабрика вибирає підтип за родом."""
    assert plant_class("Туя західна") is Tree
    assert plant_class('Троянда "Глорія Дей", плетиста') is Flower
    with pytest.raises(ValueError):
        plant_class("Кактус")


def test_unknown_genus_is_data_error(write_csv) -> None:
    """Невідомий рід — DataError з номером рядка."""
    with pytest.raises(DataError, match="Рядок 2: невідомий рід"):
        load_plants(write_csv(HEADER + "Кактус,2,20,80.00,10\n"))


def test_invalid_value_has_row_and_cause(write_csv) -> None:
    """Порушення інваріанта: номер рядка та першопричина ValueError."""
    with pytest.raises(DataError, match="Рядок 3: height") as info:
        load_plants(write_csv(HEADER + "Туя,3,120,450.00,3\nБереза,3,-150,500.00,4\n"))
    assert isinstance(info.value.__cause__, ValueError)


def test_bad_number(write_csv) -> None:
    """Нечислове значення."""
    with pytest.raises(DataError, match="поле age"):
        load_plants(write_csv(HEADER + "Дуб,п'ять,210,950.00,7\n"))


def test_missing_value(write_csv) -> None:
    """Відсутнє значення в рядку."""
    with pytest.raises(DataError, match="watering_days"):
        load_plants(write_csv(HEADER + "Дуб,5,210,950.00\n"))


def test_empty_inputs(write_csv) -> None:
    """Лише заголовок — порожній список; порожній файл — помилка."""
    assert load_plants(write_csv(HEADER)) == []
    with pytest.raises(DataError):
        load_plants(write_csv("", "void.csv"))


def test_missing_column(write_csv) -> None:
    """Відсутній стовпець."""
    with pytest.raises(DataError, match="price"):
        load_plants(write_csv("species,age,height,watering_days\nДуб,5,210,7\n"))
