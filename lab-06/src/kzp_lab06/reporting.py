"""Форматування та запис текстового звіту."""

from pathlib import Path


def format_report(summary: dict[str, object]) -> str:
    """Форматує підсумок у детермінований текстовий звіт."""
    lines = [
        "=== Розсадник рослин: звіт (варіант 12) ===",
        f"Записів: {summary['count']}",
        f"Полив не рідше ніж раз на {summary['max_watering_days']} дн.: {len(summary['selected'])}",
    ]
    for record in summary["selected"]:
        days = record["watering_days"]
        lines.append(f"  - {record['species']} (вік {record['age']}): полив кожні {days} дн.")
    for label, value in summary["metrics"].items():
        lines.append(f"{label} (відібрані): {value:.2f}")
    species = sorted(summary["species"])
    lines.append(f"Унікальних видів: {len(species)}")
    lines.append("Середня ціна за видом, грн:")
    for name, price in summary["average_price_by_species"].items():
        lines.append(f"  {name}: {price:.2f}")
    top = summary["tallest"]
    lines.append(
        "Найвища рослина: немає даних"
        if top is None
        else f"Найвища рослина: {top['species']}, {top['height']} см"
    )
    lines.append(f"Середня ціна: {summary['average_price']:.2f} грн")
    return "\n".join(lines) + "\n"


def write_report(path: Path, text: str) -> None:
    """Створює каталог і записує звіт у файл UTF-8."""
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8", newline="\n") as target:
        target.write(text)
