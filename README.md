# PackPulseMod

Open-source Minecraft client updater for private modpacks.

PackPulseMod is not tied to one server or one pack. You host your own `manifest.json`, put your pack files on a web server, set `manifestUrl` in the config, and the mod syncs the client on startup.

Fabric and NeoForge builds are included.

## English

### What PackPulseMod Does

PackPulseMod reads a remote `manifest.json` when Minecraft starts, compares it with local files, shows a confirmation screen, and downloads missing or changed files.

Features:

- configurable remote manifest URL;
- HTTP and HTTPS support;
- SHA-256 file integrity checks;
- selectable download list with scrolling;
- `Download selected` and `Download all` actions;
- clean file names without `.jar`, `.zip`, `.txt`, etc.;
- safe explicit deletion through the manifest `delete` list;
- optional stale-file cleanup;
- bundled empty fallback manifest;
- restart-required screen after `.jar` mods were updated, with `Close game` and `Restart later` choices.

Supported URL types:

- `http://IP/...`
- `https://domain/...`

### Downloads

Choose one jar for your loader and Minecraft line:

| File | Loader | Minecraft versions |
| --- | --- | --- |
| `PackPulseMod-1.0.1-fabric-mc1.20.x.jar` | Fabric | `1.20.1` - `1.20.6` |
| `PackPulseMod-1.0.1-fabric-mc1.21.x.jar` | Fabric | `1.21` - `1.21.11` |
| `PackPulseMod-1.0.1-neoforge-mc1.20.5-1.20.6.jar` | NeoForge | `1.20.5` - `1.20.6` |
| `PackPulseMod-1.0.1-neoforge-mc1.21.x.jar` | NeoForge | `1.21` - `1.21.11` |

The 4-jar multi-version layout builds successfully. Before publishing a public release, test the edge versions of each range in a real Minecraft client.

### Quick Client Setup

1. Install Fabric Loader or NeoForge for your Minecraft version.
2. Put the matching `PackPulseMod` jar into `.minecraft/mods`.
3. Start Minecraft once to create the config, then close the game.
4. Edit `.minecraft/config/packpulse.json`.
5. Set `manifestUrl` to your hosted manifest.
6. Start Minecraft again and confirm the files you want to download.

Config path:

```text
.minecraft/config/packpulse.json
```

Config example:

```json
{
  "manifestUrl": "https://your-domain.example/packpulse/manifest.json",
  "removeFilesMissingFromManifest": false,
  "updateOnStartup": true,
  "showProgressWindow": true,
  "autoRestartAfterModUpdates": false,
  "restartExecutable": ""
}
```

Default `manifestUrl` is only a placeholder:

```text
https://example.com/packpulse/manifest.json
```

You must replace it with your own URL.

### Server Pack Layout

Put your pack files into:

```text
/opt/packpulse/server-pack/
  mods/
  config/
  resourcepacks/
  shaderpacks/
  options.txt (optional)
```

Only these roots are accepted by the manifest generator and client sync logic.

### Server Scripts

Create folders on your Linux server:

```bash
sudo mkdir -p /opt/packpulse/scripts /opt/packpulse/server-pack
cd /opt/packpulse
```

Download scripts from GitHub:

```bash
curl -fsSL -o scripts/generate_manifest.py https://raw.githubusercontent.com/Kamilhik/PackPulseMod/main/scripts/generate_manifest.py
curl -fsSL -o scripts/deploy_http_ip.sh https://raw.githubusercontent.com/Kamilhik/PackPulseMod/main/scripts/deploy_http_ip.sh
curl -fsSL -o scripts/deploy_https_letsencrypt.sh https://raw.githubusercontent.com/Kamilhik/PackPulseMod/main/scripts/deploy_https_letsencrypt.sh
chmod +x scripts/deploy_http_ip.sh scripts/deploy_https_letsencrypt.sh
```

HTTP by IP:

```bash
cd /opt/packpulse
sudo ./scripts/deploy_http_ip.sh --server-ip YOUR_SERVER_IP
```

HTTPS by domain with Let's Encrypt:

```bash
cd /opt/packpulse
sudo ./scripts/deploy_https_letsencrypt.sh --domain files.example.com --email you@example.com
```

After changing mods, configs, resource packs, or shaders, run the same deploy script again. The generator also writes a `delete` list when files that were previously published disappear from the server pack, so clients can remove old renamed mods without enabling broad stale-file cleanup.

### Build From Source

Requirements:

- JDK 21;
- internet access during the first Gradle dependency download.

Build all 4 release jars:

```bash
./gradlew buildReleaseJars
```

Windows:

```powershell
.\gradlew.bat buildReleaseJars
```

Output folders:

```text
fabric-1.20/build/libs/
fabric-1.21/build/libs/
neoforge-1.20/build/libs/
neoforge-1.21/build/libs/
```

### Project Structure

```text
src/shared          shared updater, sync, config, and UI code
src/fabric          Fabric client entrypoint and fabric.mod.json
src/neoforge        NeoForge client entrypoint
```

## Русский

### Что Делает PackPulseMod

PackPulseMod при запуске Minecraft читает удаленный `manifest.json`, сравнивает его с локальными файлами, показывает экран подтверждения и скачивает отсутствующие или измененные файлы.

PackPulseMod не привязан к одному серверу или одной сборке. Ты размещаешь свой `manifest.json`, кладешь файлы сборки на веб-сервер, указываешь `manifestUrl` в конфиге, и мод синхронизирует клиент при запуске.

Возможности:

- настраиваемая ссылка на manifest;
- поддержка HTTP и HTTPS;
- проверка файлов по SHA-256;
- выбор файлов для установки;
- список файлов со скроллом;
- кнопки `Скачать выбранные` и `Скачать всё`;
- названия файлов без `.jar`, `.zip`, `.txt` и других расширений;
- безопасное явное удаление через список `delete` в manifest;
- опциональное удаление файлов, которых нет в manifest;
- встроенный пустой резервный manifest;
- экран перезапуска после обновления `.jar`-модов с кнопками `Закрыть игру` и `Перезапущу позже`.

Поддерживаются ссылки:

- `http://IP/...`
- `https://домен/...`

### Готовые Сборки

Выбери jar под свой лоадер и линейку Minecraft:

| Файл | Лоадер | Версии Minecraft |
| --- | --- | --- |
| `PackPulseMod-1.0.1-fabric-mc1.20.x.jar` | Fabric | `1.20.1` - `1.20.6` |
| `PackPulseMod-1.0.1-fabric-mc1.21.x.jar` | Fabric | `1.21` - `1.21.11` |
| `PackPulseMod-1.0.1-neoforge-mc1.20.5-1.20.6.jar` | NeoForge | `1.20.5` - `1.20.6` |
| `PackPulseMod-1.0.1-neoforge-mc1.21.x.jar` | NeoForge | `1.21` - `1.21.11` |

Схема на 4 jar собирается успешно. Перед публичным релизом лучше проверить запуск на крайних версиях каждого диапазона.

### Быстрая Настройка Клиента

1. Установи Fabric Loader или NeoForge под свою версию Minecraft.
2. Положи подходящий jar `PackPulseMod` в `.minecraft/mods`.
3. Запусти Minecraft один раз, чтобы создался конфиг, затем закрой игру.
4. Открой `.minecraft/config/packpulse.json`.
5. Укажи свой `manifestUrl`.
6. Запусти Minecraft снова и подтверди файлы для скачивания.

Путь к конфигу:

```text
.minecraft/config/packpulse.json
```

Пример конфига:

```json
{
  "manifestUrl": "https://your-domain.example/packpulse/manifest.json",
  "removeFilesMissingFromManifest": false,
  "updateOnStartup": true,
  "showProgressWindow": true,
  "autoRestartAfterModUpdates": false,
  "restartExecutable": ""
}
```

Стандартный `manifestUrl` является только примером:

```text
https://example.com/packpulse/manifest.json
```

Его нужно заменить на свою ссылку.

### Структура Серверной Сборки

Файлы сборки нужно класть сюда:

```text
/opt/packpulse/server-pack/
  mods/
  config/
  resourcepacks/
  shaderpacks/
  options.txt (необязательно)
```

Manifest и клиентская синхронизация принимают только эти папки.

### Серверные Скрипты

Создай папки на Linux-сервере:

```bash
sudo mkdir -p /opt/packpulse/scripts /opt/packpulse/server-pack
cd /opt/packpulse
```

Скачай скрипты из GitHub:

```bash
curl -fsSL -o scripts/generate_manifest.py https://raw.githubusercontent.com/Kamilhik/PackPulseMod/main/scripts/generate_manifest.py
curl -fsSL -o scripts/deploy_http_ip.sh https://raw.githubusercontent.com/Kamilhik/PackPulseMod/main/scripts/deploy_http_ip.sh
curl -fsSL -o scripts/deploy_https_letsencrypt.sh https://raw.githubusercontent.com/Kamilhik/PackPulseMod/main/scripts/deploy_https_letsencrypt.sh
chmod +x scripts/deploy_http_ip.sh scripts/deploy_https_letsencrypt.sh
```

HTTP по IP:

```bash
cd /opt/packpulse
sudo ./scripts/deploy_http_ip.sh --server-ip YOUR_SERVER_IP
```

HTTPS через домен и Let's Encrypt:

```bash
cd /opt/packpulse
sudo ./scripts/deploy_https_letsencrypt.sh --domain files.example.com --email you@example.com
```

После изменения модов, конфигов, ресурспаков или шейдеров просто запусти тот же deploy-скрипт снова. Генератор также добавляет список `delete`, если ранее опубликованный файл исчез из серверной сборки, поэтому клиенты могут удалить старые переименованные моды без включения грубой очистки всех лишних файлов.

### Сборка Из Исходников

Требования:

- JDK 21;
- интернет при первой загрузке зависимостей Gradle.

Собрать все 4 jar:

```bash
./gradlew buildReleaseJars
```

Windows:

```powershell
.\gradlew.bat buildReleaseJars
```

Папки с результатом:

```text
fabric-1.20/build/libs/
fabric-1.21/build/libs/
neoforge-1.20/build/libs/
neoforge-1.21/build/libs/
```

### Структура Проекта

```text
src/shared          общий код обновления, синхронизации, конфига и UI
src/fabric          Fabric entrypoint и fabric.mod.json
src/neoforge        entrypoint для NeoForge
```

## License

MIT
