# Лабораторна робота №7. Об'єктно-орієнтована обробка даних у Python

Кросплатформні засоби програмування · варіант 12 «Розсадник рослин» · Ковальчук Д. В., КІ-301 · версія 1.6.0

Словникові записи ЛР №6 замінено об'єктами: базовий `Plant` з `@property`-валідацією, підтипи `Tree` і `Flower` з різною `care_score()`, протокол `CareScored`, `@dataclass(frozen=True)` для підсумків, контекстний менеджер безпечного запису звіту. Основний звіт збігається з ЛР №6.

## Як запустити

**VS Code (найпростіше).** File → Open Folder → ця папка (`lab-07`). Відкрийте `run.py` і натисніть ▶ (Run Python File) або F5. Пакет встановлювати не потрібно.

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
kzp-lab07 data/input.csv reports/summary.txt
```

## Структура

- `src/kzp_lab07/models.py` — Plant, Tree, Flower, CareScored, dataclass-підсумки
- `src/kzp_lab07/loader.py` — CSV → об'єкти
- `src/kzp_lab07/analysis.py` — обчислення через протокол
- `src/kzp_lab07/reporting.py` — звіт і контекстний менеджер запису
- `src/kzp_lab07/cli.py` — командний рядок, коди завершення 0 / 2
- `src/kzp_lab07/errors.py` — `DataError`
- `tests/` — pytest
