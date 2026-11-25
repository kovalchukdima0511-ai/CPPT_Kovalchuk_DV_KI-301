"""Форматування та безпечний запис звіту."""

import os
from collections.abc import Iterator
from contextlib import contextmanager
from pathlib import Path
from typing import TextIO

from .models import CareSummary, PlantSummary


def format_report(summary: PlantSummary) -> str:
    """Основний звіт — той самий текст, що в роботі №6."""
    lines = [
        "=== Розсадник рослин: звіт (варіант 12) ===",
        f"Записів: {summary.count}",
        f"Полив не рідше ніж раз на {summary.max_watering_days} дн.: {len(summary.selected)}",
    ]
    for plant in summary.selected:
        lines.append(
            f"  - {plant.species} (вік {plant.age}): полив кожні {plant.watering_days} дн."
        )
    for label, value in summary.metrics:
        lines.append(f"{label} (відібрані): {value:.2f}")
    lines.append(f"Унікальних видів: {len(summary.species)}")
    lines.append("Середня ціна за видом, грн:")
    for name, price in summary.average_price_by_species:
        lines.append(f"  {name}: {price:.2f}")
    top = summary.tallest
    lines.append(
        "Найвища рослина: немає даних"
        if top is None
        else f"Найвища рослина: {top.species}, {top.height} см"
    )
    lines.append(f"Середня ціна: {summary.average_price:.2f} грн")
    return "\n".join(lines) + "\n"


def format_care(care: CareSummary) -> str:
    """Розділ потреби в догляді, обчисленої поліморфно."""
    by_kind = ", ".join(f"{kind}={hours:.2f}" for kind, hours in care.by_kind) or "немає даних"
    top = care.most_demanding
    lines = [
        "--- Потреба в догляді (поліморфно) ---",
        f"Сумарна потреба: {care.total:.2f} год/міс",
        f"За типом: {by_kind}",
        "Найбільша потреба: немає даних"
        if top is None
        else f"Найбільша потреба: {top.species} ({top.kind}), {top.care_score():.2f} год/міс",
    ]
    return "\n".join(lines) + "\n"


@contextmanager
def report_writer(path: Path) -> Iterator[TextIO]:
    """Контекстний менеджер безпечного запису.

    Текст пишеться в тимчасовий файл і замінює звіт лише після успішного завершення
    блоку with. Якщо в блоці виник виняток, тимчасовий файл видаляється, а старий звіт
    залишається без змін.
    """
    path.parent.mkdir(parents=True, exist_ok=True)
    temporary = path.with_name(path.name + ".tmp")
    target = temporary.open("w", encoding="utf-8", newline="\n")
    try:
        yield target
    except BaseException:
        target.close()
        temporary.unlink(missing_ok=True)
        raise
    target.close()
    os.replace(temporary, path)


def write_report(path: Path, text: str) -> None:
    """Записує звіт у UTF-8 через безпечний контекстний менеджер."""
    with report_writer(path) as target:
        target.write(text)
