# PackPulse

Open-source Fabric mod updater for private packs.

## English

### What PackPulse does

PackPulse reads a remote `manifest.json` at game startup, shows what files will be downloaded, syncs missing/changed files, and closes Minecraft if mods were updated.

`http://IP/...` and `https://domain/...` are both supported.

### Quick client setup

1. Install Fabric Loader for Minecraft `1.20.1`.
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

### Server automation scripts (2 versions)

Put pack files in:

```text
server-pack/
  mods/
  config/
  resourcepacks/
  shaderpacks/
  options.txt (optional)
```

Then on your Linux server (Ubuntu/Debian):

1. Clone your GitHub repo.
2. Go to repo folder.
3. Run one script:

HTTP by IP:

```bash
chmod +x scripts/*.sh
sudo ./scripts/deploy_http_ip.sh --server-ip 45.194.66.26
```

HTTPS by domain + Let's Encrypt:

```bash
chmod +x scripts/*.sh
sudo ./scripts/deploy_https_letsencrypt.sh --domain files.example.com --email you@example.com
```

What scripts do automatically:

1. Install packages (`nginx`, `python3`; and `certbot` for HTTPS script).
2. Copy files from `server-pack` to `/var/www/packpulse-pack`.
3. Generate `manifest.json` with SHA-256 hashes and URLs.
4. Configure Nginx endpoint `/packpulse/`.
5. For HTTPS script: request and configure Let's Encrypt certificate.
6. Print ready `manifestUrl` for client config.

When you update files, run the same script again.

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

Поддерживаются оба варианта ссылки: `http://IP/...` и `https://домен/...`.

### Быстрая настройка клиента

1. Установи Fabric Loader для Minecraft `1.20.1`.
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

### Автоматические серверные скрипты (2 версии)

Положи файлы сборки в:

```text
server-pack/
  mods/
  config/
  resourcepacks/
  shaderpacks/
  options.txt (необязательно)
```

Дальше на Linux-сервере (Ubuntu/Debian):

1. Клонируй репозиторий с GitHub.
2. Перейди в папку репозитория.
3. Запусти один из скриптов:

HTTP по IP:

```bash
chmod +x scripts/*.sh
sudo ./scripts/deploy_http_ip.sh --server-ip 45.194.66.26
```

HTTPS через домен + Let's Encrypt:

```bash
chmod +x scripts/*.sh
sudo ./scripts/deploy_https_letsencrypt.sh --domain files.example.com --email you@example.com
```

Что скрипты делают автоматически:

1. Ставят пакеты (`nginx`, `python3`, а для HTTPS еще `certbot`).
2. Копируют файлы из `server-pack` в `/var/www/packpulse-pack`.
3. Генерируют `manifest.json` с SHA-256 и ссылками.
4. Настраивают Nginx на путь `/packpulse/`.
5. Для HTTPS: выпускают и подключают сертификат Let's Encrypt.
6. Печатают готовую `manifestUrl` для клиента.

После обновления модов/конфигов просто запусти тот же скрипт снова.

## License

MIT
