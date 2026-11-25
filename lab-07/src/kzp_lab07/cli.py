"""Командний рядок: python -m kzp_lab07.cli input.csv report.txt."""

import argparse
import sys
from pathlib import Path

from .analysis import make_care_summary, make_summary
from .errors import DataError
from .loader import load_plants
from .reporting import format_care, format_report, write_report

EXIT_OK = 0
EXIT_DATA_ERROR = 2


def _use_utf8() -> None:
    """Задає UTF-8 для консолі, щоб українські повідомлення не псувалися у Windows."""
    for stream in (sys.stdout, sys.stderr):
        try:
            stream.reconfigure(encoding="utf-8")
        except (AttributeError, ValueError):
            pass


def build_report(plants: list) -> str:
    """Повний текст звіту: основна частина роботи №6 і розділ догляду."""
    summary = make_summary(plants, "price", "height", price="Вартість, грн", height="Висота, см")
    return format_report(summary) + format_care(make_care_summary(plants))


def main(argv: list[str] | None = None) -> int:
    """Читає CSV, будує звіт, друкує його та записує у файл. Повертає код завершення."""
    _use_utf8()
    parser = argparse.ArgumentParser(prog="kzp-lab07", description="ООП-звіт розсадника рослин")
    parser.add_argument("input_csv", type=Path, help="вхідний CSV (UTF-8)")
    parser.add_argument("output_report", type=Path, help="файл звіту")
    args = parser.parse_args(argv)
    try:
        text = build_report(load_plants(args.input_csv))
        write_report(args.output_report, text)
    except DataError as error:
        print(f"Помилка даних: {error}", file=sys.stderr)
        return EXIT_DATA_ERROR
    print(text, end="")
    print(f"Звіт збережено: {args.output_report.as_posix()}")
    return EXIT_OK


if __name__ == "__main__":
    raise SystemExit(main())
