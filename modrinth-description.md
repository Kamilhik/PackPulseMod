# PackPulseMod

**PackPulseMod** is an open-source client-side updater for private Minecraft modpacks.

It is designed for server owners and small communities who want players to keep their `mods`, `config`, `resourcepacks`, and `shaderpacks` folders in sync without shipping a full launcher.

PackPulseMod is **not tied to any specific server**. You host your own files and `manifest.json`, then set the manifest URL in the client config.

## Features

- Syncs missing or changed files on Minecraft startup
- Uses SHA-256 hashes for file checks
- Supports `mods`, `config`, `resourcepacks`, `shaderpacks`, and optional `options.txt`
- Shows a confirmation screen before downloading
- Lets players choose which files to download
- Includes a scrollable file list
- Shows clean mod/file names without `.jar`, `.zip`, `.txt`, etc.
- Has a `Download all` button
- Supports HTTP and HTTPS manifest URLs
- Can optionally remove stale files that are no longer in the manifest
- Closes Minecraft when updated `.jar` mods require a restart

## Supported Builds

| Loader | Minecraft versions |
| --- | --- |
| Fabric | `1.20.1` - `1.20.6` |
| Fabric | `1.21` - `1.21.11` |
| NeoForge | `1.20.5` - `1.20.6` |
| NeoForge | `1.21` - `1.21.11` |

## Client Setup

1. Install the correct Fabric Loader or NeoForge version.
2. Put the matching PackPulseMod jar into `.minecraft/mods`.
3. Start Minecraft once to create the config.
4. Close Minecraft.
5. Open:

```text
.minecraft/config/packpulse.json
```

6. Replace the example `manifestUrl` with your own URL:

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

The default URL is only a placeholder. You must host your own manifest.

## Server Pack Layout

Put pack files on your server like this:

```text
server-pack/
  mods/
  config/
  resourcepacks/
  shaderpacks/
  options.txt
```

`options.txt` is optional.

## Manifest

PackPulseMod expects a JSON manifest with file paths, download URLs, and SHA-256 hashes. The repository includes scripts that can generate and deploy this manifest automatically.

Example script workflow:

```bash
sudo mkdir -p /opt/packpulse/scripts /opt/packpulse/server-pack
cd /opt/packpulse

curl -fsSL -o scripts/generate_manifest.py https://raw.githubusercontent.com/Kamilhik/PackPulseMod/main/scripts/generate_manifest.py
curl -fsSL -o scripts/deploy_http_ip.sh https://raw.githubusercontent.com/Kamilhik/PackPulseMod/main/scripts/deploy_http_ip.sh
curl -fsSL -o scripts/deploy_https_letsencrypt.sh https://raw.githubusercontent.com/Kamilhik/PackPulseMod/main/scripts/deploy_https_letsencrypt.sh
chmod +x scripts/deploy_http_ip.sh scripts/deploy_https_letsencrypt.sh
```

HTTP by IP:

```bash
sudo ./scripts/deploy_http_ip.sh --server-ip YOUR_SERVER_IP
```

HTTPS with domain and Let's Encrypt:

```bash
sudo ./scripts/deploy_https_letsencrypt.sh --domain files.example.com --email you@example.com
```

After changing your pack files, run the same deploy script again.

## Notes

- This mod is client-side.
- It is meant for private packs, server packs, and community packs.
- For public releases, test the edge versions of each supported Minecraft range.

## Русский

**PackPulseMod** - это open-source клиентский мод для автоматического обновления приватных Minecraft-сборок.

Он подходит для владельцев серверов и небольших сообществ, которым нужно синхронизировать у игроков папки `mods`, `config`, `resourcepacks` и `shaderpacks` без отдельного лаунчера.

PackPulseMod **не привязан к конкретному серверу**. Ты размещаешь свои файлы и `manifest.json`, а затем указываешь ссылку на manifest в конфиге клиента.

## Возможности

- Синхронизация отсутствующих или измененных файлов при запуске Minecraft
- Проверка файлов по SHA-256
- Поддержка `mods`, `config`, `resourcepacks`, `shaderpacks` и опционального `options.txt`
- Экран подтверждения перед скачиванием
- Выбор, какие файлы скачивать
- Список файлов со скроллом
- Красивые названия файлов без `.jar`, `.zip`, `.txt` и других расширений
- Кнопка `Скачать всё`
- Поддержка HTTP и HTTPS ссылок на manifest
- Опциональное удаление устаревших файлов, которых больше нет в manifest
- Закрытие Minecraft после обновления `.jar`-модов, если нужен перезапуск

## Поддерживаемые Сборки

| Лоадер | Версии Minecraft |
| --- | --- |
| Fabric | `1.20.1` - `1.20.6` |
| Fabric | `1.21` - `1.21.11` |
| NeoForge | `1.20.5` - `1.20.6` |
| NeoForge | `1.21` - `1.21.11` |

## Настройка Клиента

1. Установи подходящий Fabric Loader или NeoForge.
2. Положи нужный jar PackPulseMod в `.minecraft/mods`.
3. Запусти Minecraft один раз, чтобы создался конфиг.
4. Закрой Minecraft.
5. Открой:

```text
.minecraft/config/packpulse.json
```

6. Замени примерный `manifestUrl` на свою ссылку:

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

Стандартная ссылка является только примером. Нужно разместить свой manifest.

## Структура Серверной Сборки

Файлы сборки на сервере должны лежать так:

```text
server-pack/
  mods/
  config/
  resourcepacks/
  shaderpacks/
  options.txt
```

`options.txt` необязателен.

## Manifest

PackPulseMod ожидает JSON manifest с путями файлов, ссылками для скачивания и SHA-256 хэшами. В репозитории есть скрипты, которые могут автоматически создать manifest и развернуть файлы на сервере.

Пример:

```bash
sudo mkdir -p /opt/packpulse/scripts /opt/packpulse/server-pack
cd /opt/packpulse

curl -fsSL -o scripts/generate_manifest.py https://raw.githubusercontent.com/Kamilhik/PackPulseMod/main/scripts/generate_manifest.py
curl -fsSL -o scripts/deploy_http_ip.sh https://raw.githubusercontent.com/Kamilhik/PackPulseMod/main/scripts/deploy_http_ip.sh
curl -fsSL -o scripts/deploy_https_letsencrypt.sh https://raw.githubusercontent.com/Kamilhik/PackPulseMod/main/scripts/deploy_https_letsencrypt.sh
chmod +x scripts/deploy_http_ip.sh scripts/deploy_https_letsencrypt.sh
```

HTTP по IP:

```bash
sudo ./scripts/deploy_http_ip.sh --server-ip YOUR_SERVER_IP
```

HTTPS через домен и Let's Encrypt:

```bash
sudo ./scripts/deploy_https_letsencrypt.sh --domain files.example.com --email you@example.com
```

После изменения файлов сборки просто запусти тот же deploy-скрипт снова.

## Примечания

- Мод работает на стороне клиента.
- Подходит для приватных сборок, серверных сборок и сборок небольших сообществ.
- Перед публичным релизом лучше проверить крайние версии каждого поддерживаемого диапазона Minecraft.

## License

MIT
