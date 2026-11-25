"""Тести читання та перетворення CSV."""

import pytest
from conftest import HEADER

from kzp_lab06.errors import DataError
from kzp_lab06.loader import load_records, parse_row


def test_control_file_is_loaded_with_types(control_csv) -> None:
    """Перевіряє кількість записів і типи полів."""
    records = load_records(control_csv)
    assert len(records) == 10
    first = records[0]
    assert first == {
        "species": "Туя західна",
        "age": 3,
        "height": 120,
        "price": 450.0,
        "watering_days": 3,
    }


def test_text_with_comma_and_quotes_is_read_correctly(control_csv) -> None:
    """Перевіряє поле з комою та лапками."""
    species = [record["species"] for record in load_records(control_csv)]
    assert 'Троянда "Глорія Дей", плетиста' in species


def test_header_only_gives_empty_list(write_csv) -> None:
    """Файл лише із заголовком — коректний порожній набір."""
    assert load_records(write_csv(HEADER)) == []


def test_completely_empty_file_is_error(write_csv) -> None:
    """Порожній файл без заголовка відхиляється."""
    with pytest.raises(DataError, match="немає заголовка"):
        load_records(write_csv(""))


def test_missing_column_is_error(write_csv) -> None:
    """Відсутній стовпець у заголовку."""
    with pytest.raises(DataError, match="watering_days"):
        load_records(write_csv("species,age,height,price\nДуб,1,10,1.0\n"))


def test_bad_number_has_row_number_and_cause(write_csv) -> None:
    """Хибне число: номер рядка та першопричина ValueError."""
    with pytest.raises(DataError, match="Рядок 3: поле age") as info:
        load_records(write_csv(HEADER + "Дуб,1,10,1.0,7\nТуя,три,10,1.0,3\n"))
    assert isinstance(info.value.__cause__, ValueError)


def test_missing_value_in_row_is_error(write_csv) -> None:
    """Відсутнє значення в рядку (менше полів, ніж у заголовку)."""
    with pytest.raises(DataError, match="Рядок 2: поле watering_days") as info:
        load_records(write_csv(HEADER + "Дуб,1,10,1.0\n"))
    assert info.value.__cause__ is not None


def test_negative_height_is_error(control_csv, write_csv) -> None:
    """Від'ємна висота заборонена."""
    with pytest.raises(DataError, match="height"):
        load_records(write_csv(HEADER + "Береза,3,-150,500.00,4\n"))


def test_blank_species_is_error() -> None:
    """Порожній вид."""
    with pytest.raises(DataError, match="species"):
        parse_row(
            {"species": "  ", "age": "1", "height": "1", "price": "1", "watering_days": "1"}, 2
        )


def test_boundary_values_are_accepted() -> None:
    """Вік 0, ціна 0, висота 1, полив 1 — граничні коректні значення."""
    record = parse_row(
        {
            "species": " Тюльпан  Дарвіна ",
            "age": "0",
            "height": "1",
            "price": "0",
            "watering_days": "1",
        },
        2,
    )
    assert record == {
        "species": "Тюльпан Дарвіна",
        "age": 0,
        "height": 1,
        "price": 0.0,
        "watering_days": 1,
    }


def test_nan_price_is_error() -> None:
    """NaN не допускається як ціна."""
    with pytest.raises(DataError, match="price"):
        parse_row(
            {"species": "Дуб", "age": "1", "height": "1", "price": "nan", "watering_days": "1"}, 2
        )


def test_missing_file_is_data_error(tmp_path) -> None:
    """Відсутній файл перетворюється на DataError із причиною OSError."""
    with pytest.raises(DataError) as info:
        load_records(tmp_path / "missing.csv")
    assert isinstance(info.value.__cause__, OSError)
