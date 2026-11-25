"""Спільні фікстури тестів."""

from pathlib import Path

import pytest

DATA_DIR = Path(__file__).resolve().parent.parent / "data"

HEADER = "species,age,height,price,watering_days\n"


@pytest.fixture
def control_csv() -> Path:
    """Контрольний CSV варіанта 12."""
    return DATA_DIR / "input.csv"


@pytest.fixture
def write_csv(tmp_path: Path):
    """Створює тимчасовий CSV із заданим вмістом."""

    def _write(text: str, name: str = "input.csv") -> Path:
        path = tmp_path / name
        path.write_text(text, encoding="utf-8")
        return path

    return _write
