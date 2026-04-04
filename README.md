# PackPulse

Open-source Fabric mod updater for private packs.

## English

### What PackPulse does

PackPulse reads a remote `manifest.json` at game startup, shows what files will be downloaded, syncs missing/changed files, and closes Minecraft if mods were updated.

Both URL types are supported:
- `http://IP/...`
- `https://domain/...`

### Quick client setup

1. Install Fabric Loader for Minecraft `1.20.1` or `1.21.1`.
2. Put `packpulse-1.0.0.jar` into `.minecraft/mods`.
3. Run Minecraft once to create config, then close the game.
4. Edit `.minecraft/config/packpulse.json` and set `manifestUrl`.

Config example:

```json
{
  "manifestUrl": "http://YOUR_SERVER_IP/packpulse/manifest.json",
  "removeFilesMissingFromManifest": false,
  "updateOnStartup": true,
  "showProgressWindow": true,
  "autoRestartAfterModUpdates": false,
  "restartExecutable": ""
}
```

### Server scripts (download directly from GitHub)

Create folders on your Linux server (Ubuntu/Debian):

```bash
sudo mkdir -p /opt/packpulse/scripts /opt/packpulse/server-pack
cd /opt/packpulse
```

Download scripts directly from GitHub:

```bash
curl -fsSL -o scripts/generate_manifest.py https://raw.githubusercontent.com/Kamilhik/PackPulseMod/main/scripts/generate_manifest.py
curl -fsSL -o scripts/deploy_http_ip.sh https://raw.githubusercontent.com/Kamilhik/PackPulseMod/main/scripts/deploy_http_ip.sh
curl -fsSL -o scripts/deploy_https_letsencrypt.sh https://raw.githubusercontent.com/Kamilhik/PackPulseMod/main/scripts/deploy_https_letsencrypt.sh
chmod +x scripts/deploy_http_ip.sh scripts/deploy_https_letsencrypt.sh
```

Put your files into:

```text
/opt/packpulse/server-pack/
  mods/
  config/
  resourcepacks/
  shaderpacks/
  options.txt (optional)
```

Run one script:

HTTP by IP:

```bash
cd /opt/packpulse
sudo ./scripts/deploy_http_ip.sh --server-ip 45.194.66.26
```

HTTPS by domain + Let's Encrypt:

```bash
cd /opt/packpulse
sudo ./scripts/deploy_https_letsencrypt.sh --domain files.example.com --email you@example.com
```

After pack updates, run the same deploy script again.

### Build from source

Requirements: Java 17, Gradle 8+

```bash
gradle build
```

Output:

```text
build/libs/packpulse-<version>.jar
```

## Русский

### Что делает PackPulse

PackPulse при запуске игры читает удаленный `manifest.json`, показывает список файлов для скачивания, синхронизирует недостающие/измененные файлы и закрывает Minecraft, если обновились моды.

Поддерживаются оба варианта ссылки:
- `http://IP/...`
- `https://домен/...`

### Быстрая настройка клиента

1. Установи Fabric Loader для Minecraft `1.20.1` или `1.21.1`.
2. Положи `packpulse-1.0.0.jar` в `.minecraft/mods`.
3. Запусти Minecraft один раз (создастся конфиг), затем закрой игру.
4. Открой `.minecraft/config/packpulse.json` и укажи `manifestUrl`.

Пример конфига:

```json
{
  "manifestUrl": "http://YOUR_SERVER_IP/packpulse/manifest.json",
  "removeFilesMissingFromManifest": false,
  "updateOnStartup": true,
  "showProgressWindow": true,
  "autoRestartAfterModUpdates": false,
  "restartExecutable": ""
}
```

### Серверные скрипты (скачивание напрямую с GitHub)

Создай папки на Linux-сервере (Ubuntu/Debian):

```bash
sudo mkdir -p /opt/packpulse/scripts /opt/packpulse/server-pack
cd /opt/packpulse
```

Скачай скрипты напрямую из GitHub:

```bash
curl -fsSL -o scripts/generate_manifest.py https://raw.githubusercontent.com/Kamilhik/PackPulseMod/main/scripts/generate_manifest.py
curl -fsSL -o scripts/deploy_http_ip.sh https://raw.githubusercontent.com/Kamilhik/PackPulseMod/main/scripts/deploy_http_ip.sh
curl -fsSL -o scripts/deploy_https_letsencrypt.sh https://raw.githubusercontent.com/Kamilhik/PackPulseMod/main/scripts/deploy_https_letsencrypt.sh
chmod +x scripts/deploy_http_ip.sh scripts/deploy_https_letsencrypt.sh
```

Положи файлы сборки в:

```text
/opt/packpulse/server-pack/
  mods/
  config/
  resourcepacks/
  shaderpacks/
  options.txt (необязательно)
```

Запусти один скрипт:

HTTP по IP:

```bash
cd /opt/packpulse
sudo ./scripts/deploy_http_ip.sh --server-ip 45.194.66.26
```

HTTPS через домен + Let's Encrypt:

```bash
cd /opt/packpulse
sudo ./scripts/deploy_https_letsencrypt.sh --domain files.example.com --email you@example.com
```

После обновления модов/конфигов просто запусти тот же deploy-скрипт снова.

## License

MIT
