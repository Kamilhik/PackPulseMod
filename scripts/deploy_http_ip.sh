#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

SERVER_IP=""
PACK_SOURCE="${PROJECT_DIR}/server-pack"
WEB_ROOT="/var/www/packpulse-pack"
SITE_NAME="packpulse-ip"
PACK_NAME="PackPulse Pack"
PACK_VERSION="$(date +%Y.%m.%d-%H%M%S)"
MINECRAFT_VERSION="1.20.1"
FABRIC_LOADER_VERSION=""
PROFILE_NAME="PackPulse"
VERSION_ID="packpulse"

usage() {
  cat <<EOF
Usage:
  sudo ./scripts/deploy_http_ip.sh --server-ip <IP> [options]

Required:
  --server-ip <IP>               Public server IP (example: 45.194.66.26)

Optional:
  --pack-source <path>           Source folder with mods/config/resourcepacks/shaderpacks
                                 Default: ${PROJECT_DIR}/server-pack
  --web-root <path>              Nginx web root for pack files
                                 Default: /var/www/packpulse-pack
  --site-name <name>             Nginx site file name
                                 Default: packpulse-ip
  --pack-name <name>             Manifest "name" field
  --pack-version <version>       Manifest "version" field
  --minecraft-version <version>  Manifest minecraftVersion (default: 1.20.1)
  --fabric-loader-version <ver>  Manifest fabricLoaderVersion
  --profile-name <name>          Manifest profileName
  --version-id <id>              Manifest versionId
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --server-ip) SERVER_IP="${2:-}"; shift 2 ;;
    --pack-source) PACK_SOURCE="${2:-}"; shift 2 ;;
    --web-root) WEB_ROOT="${2:-}"; shift 2 ;;
    --site-name) SITE_NAME="${2:-}"; shift 2 ;;
    --pack-name) PACK_NAME="${2:-}"; shift 2 ;;
    --pack-version) PACK_VERSION="${2:-}"; shift 2 ;;
    --minecraft-version) MINECRAFT_VERSION="${2:-}"; shift 2 ;;
    --fabric-loader-version) FABRIC_LOADER_VERSION="${2:-}"; shift 2 ;;
    --profile-name) PROFILE_NAME="${2:-}"; shift 2 ;;
    --version-id) VERSION_ID="${2:-}"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown option: $1"; usage; exit 1 ;;
  esac
done

if [[ -z "${SERVER_IP}" ]]; then
  echo "Error: --server-ip is required."
  usage
  exit 1
fi

if [[ ! -d "${PACK_SOURCE}" ]]; then
  echo "Error: pack source folder not found: ${PACK_SOURCE}"
  echo "Create it and put your files there (mods/config/resourcepacks/shaderpacks)."
  exit 1
fi

if [[ "${EUID}" -ne 0 ]]; then
  echo "Error: run as root (use sudo)."
  exit 1
fi

if ! command -v apt-get >/dev/null 2>&1; then
  echo "Error: apt-get not found. This script supports Debian/Ubuntu."
  exit 1
fi

export DEBIAN_FRONTEND=noninteractive
apt-get update
apt-get install -y nginx python3

mkdir -p "${WEB_ROOT}"

for dir in mods config resourcepacks shaderpacks; do
  rm -rf "${WEB_ROOT}/${dir}"
  if [[ -d "${PACK_SOURCE}/${dir}" ]]; then
    cp -a "${PACK_SOURCE}/${dir}" "${WEB_ROOT}/${dir}"
  fi
done

if [[ -f "${PACK_SOURCE}/options.txt" ]]; then
  cp -a "${PACK_SOURCE}/options.txt" "${WEB_ROOT}/options.txt"
else
  rm -f "${WEB_ROOT}/options.txt"
fi

BASE_URL="http://${SERVER_IP}/packpulse"
python3 "${SCRIPT_DIR}/generate_manifest.py" \
  --pack-root "${WEB_ROOT}" \
  --base-url "${BASE_URL}" \
  --output "${WEB_ROOT}/manifest.json" \
  --name "${PACK_NAME}" \
  --version "${PACK_VERSION}" \
  --minecraft-version "${MINECRAFT_VERSION}" \
  --fabric-loader-version "${FABRIC_LOADER_VERSION}" \
  --profile-name "${PROFILE_NAME}" \
  --version-id "${VERSION_ID}"

NGINX_CONF="/etc/nginx/sites-available/${SITE_NAME}.conf"
cat > "${NGINX_CONF}" <<EOF
server {
    listen 80;
    listen [::]:80;
    server_name ${SERVER_IP} _;
    client_max_body_size 512M;

    location /packpulse/ {
        alias ${WEB_ROOT}/;
        autoindex off;
        add_header Cache-Control "no-cache";
    }

    location / {
        add_header Content-Type text/plain;
        return 200 "PackPulse HTTP server is running\n";
    }
}
EOF

ln -sfn "${NGINX_CONF}" "/etc/nginx/sites-enabled/${SITE_NAME}.conf"
rm -f /etc/nginx/sites-enabled/default

nginx -t
systemctl enable --now nginx
systemctl reload nginx

cat <<EOF

Done.
Manifest URL:
  ${BASE_URL}/manifest.json

Set this in client config (.minecraft/config/packpulse.json):
{
  "manifestUrl": "${BASE_URL}/manifest.json",
  "removeFilesMissingFromManifest": false,
  "updateOnStartup": true,
  "showProgressWindow": true,
  "autoRestartAfterModUpdates": false,
  "restartExecutable": ""
}

When you update files in ${PACK_SOURCE}, run this script again.
EOF
