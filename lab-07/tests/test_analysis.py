"""Тести аналізу, еквівалентності з роботою №6, звіту та CLI."""

from pathlib import Path

import pytest
from conftest import HEADER

from kzp_lab07.analysis import care_by_kind, make_care_summary, make_summary
from kzp_lab07.cli import EXIT_DATA_ERROR, EXIT_OK, build_report, main
from kzp_lab07.loader import load_plants
from kzp_lab07.reporting import report_writer, write_report

LAB06_REPORT = Path(__file__).resolve().parent / "expected_lab06_report.txt"


def test_main_report_is_equivalent_to_lab06(control_csv) -> None:
    """Об'єктна модель дає той самий основний звіт, що й словникова (робота №6)."""
    report = build_report(load_plants(control_csv))
    assert report.startswith(LAB06_REPORT.read_text(encoding="utf-8"))


def test_care_section_on_control_data(control_csv) -> None:
    """Розділ догляду: сума, розподіл за типом і лідер."""
    care = make_care_summary(load_plants(control_csv))
    assert care.total == pytest.approx(35.9)
    assert care.by_kind == (("дерево", pytest.approx(10.9)), ("квітка", pytest.approx(25.0)))
    assert care.most_demanding.species == "Тюльпан Дарвіна"


def test_summary_metrics_and_labels(control_csv) -> None:
    """*metrics, типовий поріг і **labels."""
    summary = make_summary(load_plants(control_csv), "price", price="Вартість")
    assert summary.metrics == (("Вартість", pytest.approx(925.5)),)
    assert len(summary.selected) == 5
    assert len(summary.species) == 9


def test_unknown_metric() -> None:
    """Невідома метрика."""
    with pytest.raises(ValueError):
        make_summary([], "colour")


def test_empty_input() -> None:
    """Порожній вхід."""
    assert care_by_kind([]) == {}
    text = build_report([])
    assert "Записів: 0" in text
    assert "Найбільша потреба: немає даних" in text


def test_report_writer_replaces_file_on_success(tmp_path) -> None:
    """Контекстний менеджер записує файл UTF-8."""
    path = tmp_path / "out" / "report.txt"
    write_report(path, "Готово\n")
    assert path.read_text(encoding="utf-8") == "Готово\n"


def test_report_writer_keeps_old_file_on_error(tmp_path) -> None:
    """Під час винятку старий звіт не псується, тимчасовий файл видаляється."""
    path = tmp_path / "report.txt"
    write_report(path, "старий\n")
    with pytest.raises(RuntimeError):
        with report_writer(path) as target:
            target.write("новий, але недописаний")
            raise RuntimeError("збій")
    assert path.read_text(encoding="utf-8") == "старий\n"
    assert list(tmp_path.iterdir()) == [path]


def test_cli_success(control_csv, tmp_path, capsys) -> None:
    """Успішний запуск CLI."""
    output = tmp_path / "summary.txt"
    assert main([str(control_csv), str(output)]) == EXIT_OK
    assert "Сумарна потреба: 35.90 год/міс" in output.read_text(encoding="utf-8")
    assert "Звіт збережено" in capsys.readouterr().out


def test_cli_error(write_csv, tmp_path, capsys) -> None:
    """Помилковий запуск CLI: код 2 і повідомлення в stderr."""
    output = tmp_path / "summary.txt"
    assert main([str(write_csv(HEADER + "Кактус,2,20,80.00,10\n")), str(output)]) == EXIT_DATA_ERROR
    assert "Помилка даних" in capsys.readouterr().err
    assert not output.exists()
