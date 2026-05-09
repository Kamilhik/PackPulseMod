# Что сделано в PackPulseMod

Этот файл описывает текущее состояние мода PackPulseMod и основные изменения, которые были сделаны в проекте.

## Назначение мода

PackPulseMod - клиентский мод для Minecraft, который помогает держать приватную сборку в актуальном состоянии.

При запуске игры мод:

- читает удаленный `manifest.json`;
- проверяет локальные файлы по SHA-256;
- показывает окно подтверждения перед скачиванием;
- дает выбрать, какие файлы скачивать;
- скачивает отсутствующие или измененные файлы;
- показывает прогресс обновления;
- закрывает Minecraft, если обновились `.jar` моды и нужен перезапуск.

Мод не привязан к конкретному серверу. Пользователь сам указывает ссылку на свой manifest в конфиге:

```text
.minecraft/config/packpulse.json
```

## Поддерживаемые загрузчики и версии

Сейчас проект собирается в 4 jar-файла:

| Jar | Загрузчик | Версии Minecraft |
| --- | --- | --- |
| `PackPulseMod-1.0.0-fabric-mc1.20.x.jar` | Fabric | `1.20.1` - `1.20.6` |
| `PackPulseMod-1.0.0-fabric-mc1.21.x.jar` | Fabric | `1.21` - `1.21.11` |
| `PackPulseMod-1.0.0-neoforge-mc1.20.5-1.20.6.jar` | NeoForge | `1.20.5` - `1.20.6` |
| `PackPulseMod-1.0.0-neoforge-mc1.21.x.jar` | NeoForge | `1.21` - `1.21.11` |

Готовые jar лежат в папке:

```text
releases/
```

## Что добавлено

- Мультиверсионная сборка на 4 jar-файла.
- Поддержка Fabric для веток Minecraft `1.20.x` и `1.21.x`.
- Поддержка NeoForge для `1.20.5-1.20.6`.
- Поддержка NeoForge для `1.21.x`.
- Общий код вынесен в `src/shared`.
- Отдельные entrypoint-файлы сделаны для Fabric и NeoForge.
- Добавлен Mod Menu metadata:
  - название: `PackPulseMod`;
  - автор: `Kamilchik`;
  - иконка мода.
- Добавлена иконка для Mod Menu.
- README написан и улучшен на русском и английском.
- Улучшено описание для Modrinth.
- Добавлены серверные скрипты для генерации и публикации manifest.

## UI установки

Окно подтверждения установки было улучшено:

- показывает список всех файлов, которые нужно скачать;
- отображает только чистое имя файла без расширений вроде `.jar`, `.zip`, `.json`, `.txt`;
- поддерживает скролл;
- позволяет выбирать отдельные файлы;
- есть кнопка `Скачать`;
- есть кнопка `Скачать все`;
- есть кнопка `Отмена`;
- убран эффект, из-за которого при установке экран выглядел заблюренным/сломленным;
- исправлены проблемы отрисовки на Minecraft `1.21.11`.

## Исправления запуска

Была проблема: окно установки не появлялось, потому что мод запускал синхронизацию слишком рано, еще во время загрузки Minecraft.

Исправлено:

- мод теперь ждет готовности клиента;
- синхронизация начинается после появления главного меню или входа в мир;
- добавлен лог:

```text
Minecraft client is ready, starting pack sync.
```

Также добавлен лог для диагностики manifest:

```text
Manifest loaded: ... files, ... files require download.
```

По нему можно понять, нашел ли мод файлы для скачивания.

## Исправления совместимости

Исправлены ошибки из логов:

- Fabric `1.21.11`: падение на отрисовке `renderOutline`;
- NeoForge `1.21.11`: падение на отрисовке текста в UI;
- NeoForge `1.20.6`: старый jar был собран под legacy/Forge `47.x`, теперь отдельный jar собран под NeoForge `20.6.x`.

Из-за этого NeoForge jar для 1.20 теперь называется:

```text
PackPulseMod-1.0.0-neoforge-mc1.20.5-1.20.6.jar
```

## Конфиг

При первом запуске создается файл:

```text
.minecraft/config/packpulse.json
```

Пример:

```json
{
  "manifestUrl": "https://example.com/packpulse/manifest.json",
  "removeFilesMissingFromManifest": false,
  "updateOnStartup": true,
  "showProgressWindow": true,
  "autoRestartAfterModUpdates": false,
  "restartExecutable": ""
}
```

Публичный PackPulseMod не содержит личных ссылок и не привязан к FreeWebInstaller.

## Manifest

Мод ожидает manifest со списком файлов, URL и SHA-256 хэшами.

Поддерживаемые папки:

- `mods`;
- `config`;
- `resourcepacks`;
- `shaderpacks`;
- `options.txt`.

Для генерации manifest в проекте есть скрипты:

```text
scripts/generate_manifest.py
scripts/deploy_http_ip.sh
scripts/deploy_https_letsencrypt.sh
```

## Структура проекта

```text
src/shared      общий код синхронизации, конфига и UI
src/fabric      Fabric entrypoint и fabric.mod.json
src/neoforge    NeoForge entrypoint и neoforge.mods.toml
scripts/        серверные скрипты
releases/       готовые jar-файлы
```

## GitHub

Проект подготовлен и загружен в репозиторий:

```text
https://github.com/Kamilhik/PackPulseMod
```

Последние важные изменения были отправлены в GitHub, включая:

- исправление появления окна установки;
- исправление UI;
- поддержку NeoForge `1.20.5-1.20.6`;
- обновленные release jar.

