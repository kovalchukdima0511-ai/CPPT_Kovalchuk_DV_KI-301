"""Тести функцій аналізу."""

import pytest

from kzp_lab06.analysis import (
    average_price,
    average_price_by_species,
    frequent_watering,
    make_summary,
    tallest,
    unique_species,
)
from kzp_lab06.loader import load_records


def _plant(species: str, height: int = 10, price: float = 1.0, watering: int = 1, age: int = 1):
    return {
        "species": species,
        "age": age,
        "height": height,
        "price": price,
        "watering_days": watering,
    }


def test_filter_boundary_two_days_included() -> None:
    """Полив 2 дні входить у відбір, 3 дні — ні."""
    records = [_plant("A", watering=2), _plant("B", watering=3)]
    assert [r["species"] for r in frequent_watering(records)] == ["A"]


def test_filter_can_return_empty_result() -> None:
    """Порожній результат відбору — коректний випадок."""
    assert frequent_watering([_plant("Дуб", watering=7)]) == []


def test_unique_species_removes_duplicates(control_csv) -> None:
    """Дубль «Троянда чайна» дає один елемент множини."""
    species = unique_species(load_records(control_csv))
    assert len(species) == 9
    assert "Троянда чайна" in species


def test_average_price_by_species_is_sorted_and_correct(control_csv) -> None:
    """Середня ціна за видом і стабільний порядок ключів."""
    result = average_price_by_species(load_records(control_csv))
    assert list(result) == sorted(result)
    assert result["Троянда чайна"] == pytest.approx(195.0)


def test_tallest_and_average(control_csv) -> None:
    """Найвища рослина й середня ціна на контрольних даних."""
    records = load_records(control_csv)
    assert tallest(records)["species"] == "Дуб звичайний"
    assert average_price(records) == pytest.approx(434.55)


def test_tallest_tie_keeps_first() -> None:
    """За рівної висоти обирається перший запис."""
    assert tallest([_plant("Клен", height=180), _plant("Ялина", height=180)])["species"] == "Клен"


def test_empty_input() -> None:
    """Порожній вхід: None, 0.0 і порожній словник."""
    assert tallest([]) is None
    assert average_price([]) == 0.0
    assert average_price_by_species([]) == {}
    summary = make_summary([], "price")
    assert summary["count"] == 0
    assert summary["metrics"] == {"price": 0.0}


def test_make_summary_args_defaults_and_kwargs(control_csv) -> None:
    """*metrics, типовий поріг і **labels."""
    records = load_records(control_csv)
    summary = make_summary(records, "price", "height", price="Вартість")
    assert summary["max_watering_days"] == 2
    assert summary["metrics"] == {"Вартість": pytest.approx(925.5), "height": pytest.approx(300.0)}
    wider = make_summary(records, max_watering_days=3)
    assert len(wider["selected"]) == 7
    assert wider["metrics"] == {}


def test_make_summary_rejects_unknown_metric() -> None:
    """Невідома метрика не маскується порожнім звітом."""
    with pytest.raises(ValueError, match="Невідома метрика"):
        make_summary([_plant("A")], "colour")
