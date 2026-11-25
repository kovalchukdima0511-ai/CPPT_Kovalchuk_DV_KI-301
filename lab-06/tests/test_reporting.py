"""Тести звіту та CLI."""

from pathlib import Path

from conftest import HEADER

from kzp_lab06.analysis import make_summary
from kzp_lab06.cli import EXIT_DATA_ERROR, EXIT_OK, main
from kzp_lab06.loader import load_records
from kzp_lab06.reporting import format_report, write_report

EXPECTED = Path(__file__).resolve().parent / "expected_report.txt"


def test_report_matches_expected_text(control_csv) -> None:
    """Повний текст звіту детермінований і збігається з еталоном."""
    summary = make_summary(
        load_records(control_csv), "price", "height", price="Вартість, грн", height="Висота, см"
    )
    assert format_report(summary) == EXPECTED.read_text(encoding="utf-8")


def test_report_for_empty_input() -> None:
    """Звіт для порожнього набору."""
    text = format_report(make_summary([]))
    assert "Записів: 0" in text
    assert "Найвища рослина: немає даних" in text


def test_write_report_utf8(tmp_path) -> None:
    """Запис і читання UTF-8 звіту, каталог створюється автоматично."""
    output = tmp_path / "nested" / "report.txt"
    write_report(output, "Готово ✓\n")
    assert output.read_text(encoding="utf-8") == "Готово ✓\n"


def test_cli_success(control_csv, tmp_path, capsys) -> None:
    """Успішний сценарій: код 0, файл звіту створено."""
    output = tmp_path / "summary.txt"
    assert main([str(control_csv), str(output)]) == EXIT_OK
    assert output.read_text(encoding="utf-8") == EXPECTED.read_text(encoding="utf-8")
    assert "Звіт збережено" in capsys.readouterr().out


def test_cli_broken_csv(write_csv, tmp_path, capsys) -> None:
    """Помилковий сценарій: код 2, повідомлення в stderr, файл не створено."""
    broken = write_csv(HEADER + "Береза,3,-150,500.00,4\n")
    output = tmp_path / "summary.txt"
    assert main([str(broken), str(output)]) == EXIT_DATA_ERROR
    assert "Помилка даних: Рядок 2" in capsys.readouterr().err
    assert not output.exists()


def test_import_does_not_run_cli(capsys) -> None:
    """Імпорт модуля cli нічого не друкує."""
    import kzp_lab06.cli  # noqa: F401

    assert capsys.readouterr().out == ""
