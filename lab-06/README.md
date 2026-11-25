# Лабораторна робота №6. Обробка даних засобами Python

Кросплатформні засоби програмування · варіант 12 «Розсадник рослин» · Ковальчук Д. В., КІ-301 · версія 1.5.0

Пакет читає CSV через `csv.DictReader`, перетворює поля на типи, будує звіт за списками, словниками й множинами (полив ≤ 2 дні, середня ціна за видом, унікальні види, найвища рослина, середня ціна) і записує його у файл.

## Як запустити

**VS Code (найпростіше).** File → Open Folder → ця папка (`lab-06`). Відкрийте `run.py` і натисніть ▶ (Run Python File) або F5. Пакет встановлювати не потрібно.

```
python run.py                                   # data/input.csv -> reports/summary.txt
python run.py data/broken.csv reports/x.txt     # помилковий сценарій, код 2
```

**Повний цикл (віртуальне середовище, тести, ruff, wheel):**

```
python -m venv .venv
.venv\Scripts\activate          # Windows  (macOS/Ubuntu: source .venv/bin/activate)
python -m pip install -e ".[dev]"
python -m pytest
ruff check .
ruff format --check .
python -m build --wheel
kzp-lab06 data/input.csv reports/summary.txt
```

## Структура

- `src/kzp_lab06/loader.py` — читання й перетворення CSV
- `src/kzp_lab06/analysis.py` — обчислення
- `src/kzp_lab06/reporting.py` — текст і запис звіту
- `src/kzp_lab06/cli.py` — командний рядок, коди завершення 0 / 2
- `src/kzp_lab06/errors.py` — `DataError`
- `tests/` — pytest
