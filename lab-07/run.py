"""Швидкий запуск без встановлення пакета: python run.py [input.csv] [report.txt]."""

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent / "src"))

from kzp_lab07.cli import main  # noqa: E402

if __name__ == "__main__":
    args = sys.argv[1:] or ["data/input.csv", "reports/summary.txt"]
    raise SystemExit(main(args))
