"""Командний рядок: python -m kzp_lab06.cli input.csv report.txt."""

import argparse
import sys
from pathlib import Path

from .analysis import make_summary
from .errors import DataError
from .loader import load_records
from .reporting import format_report, write_report

EXIT_OK = 0
EXIT_DATA_ERROR = 2


def _use_utf8() -> None:
    """Задає UTF-8 для консолі, щоб українські повідомлення не псувалися у Windows."""
    for stream in (sys.stdout, sys.stderr):
        try:
            stream.reconfigure(encoding="utf-8")
        except (AttributeError, ValueError):
            pass


def main(argv: list[str] | None = None) -> int:
    """Читає CSV, будує звіт, друкує його та записує у файл. Повертає код завершення."""
    _use_utf8()
    parser = argparse.ArgumentParser(prog="kzp-lab06", description="Звіт розсадника рослин")
    parser.add_argument("input_csv", type=Path, help="вхідний CSV (UTF-8)")
    parser.add_argument("output_report", type=Path, help="файл звіту")
    args = parser.parse_args(argv)
    try:
        records = load_records(args.input_csv)
        summary = make_summary(
            records, "price", "height", price="Вартість, грн", height="Висота, см"
        )
        text = format_report(summary)
        write_report(args.output_report, text)
    except DataError as error:
        print(f"Помилка даних: {error}", file=sys.stderr)
        return EXIT_DATA_ERROR
    print(text, end="")
    print(f"Звіт збережено: {args.output_report.as_posix()}")
    return EXIT_OK


if __name__ == "__main__":
    raise SystemExit(main())
