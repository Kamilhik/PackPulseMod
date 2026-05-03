#!/usr/bin/env python3
import argparse
import hashlib
import json
from datetime import datetime, timezone
from pathlib import Path
from urllib.parse import quote

ALLOWED_ROOTS = ("mods", "config", "resourcepacks", "shaderpacks")


def sha256_file(file_path: Path) -> str:
    digest = hashlib.sha256()
    with file_path.open("rb") as stream:
        while True:
            chunk = stream.read(1024 * 1024)
            if not chunk:
                break
            digest.update(chunk)
    return digest.hexdigest()


def make_url(base_url: str, relative_path: str) -> str:
    return f"{base_url.rstrip('/')}/{quote(relative_path, safe='/-_.~')}"


def collect_files(pack_root: Path, base_url: str) -> list[dict[str, str]]:
    files: list[dict[str, str]] = []

    for root in ALLOWED_ROOTS:
        source_root = pack_root / root
        if not source_root.is_dir():
            continue

        for file_path in sorted(source_root.rglob("*")):
            if not file_path.is_file():
                continue

            relative = file_path.relative_to(pack_root).as_posix()
            files.append(
                {
                    "path": relative,
                    "url": make_url(base_url, relative),
                    "sha256": sha256_file(file_path),
                }
            )

    options_path = pack_root / "options.txt"
    if options_path.is_file():
        relative = "options.txt"
        files.append(
            {
                "path": relative,
                "url": make_url(base_url, relative),
                "sha256": sha256_file(options_path),
            }
        )

    files.sort(key=lambda item: item["path"])
    return files


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Generate PackPulseMod manifest.json")
    parser.add_argument("--pack-root", required=True, help="Folder with mods/config/resourcepacks/shaderpacks")
    parser.add_argument("--base-url", required=True, help="Base URL for files, e.g. https://domain.com/packpulse")
    parser.add_argument("--output", required=True, help="Output path for manifest.json")
    parser.add_argument("--name", default="PackPulse Pack", help="Manifest pack name")
    parser.add_argument("--version", default=datetime.now(timezone.utc).strftime("%Y.%m.%d-%H%M%S"), help="Pack version")
    parser.add_argument("--minecraft-version", default="1.21.1", help="Minecraft version")
    parser.add_argument("--loader", default="neoforge", help="Loader name")
    parser.add_argument("--neoforge-version", default="21.1.228", help="NeoForge version")
    parser.add_argument("--version-id", default="packpulse-pack", help="Version id")
    parser.add_argument("--profile-name", default="PackPulse Pack", help="Profile name")
    return parser.parse_args()


def main() -> None:
    args = parse_args()

    pack_root = Path(args.pack_root).resolve()
    output_path = Path(args.output).resolve()
    output_path.parent.mkdir(parents=True, exist_ok=True)

    files = collect_files(pack_root, args.base_url)
    manifest = {
        "name": args.name,
        "version": args.version,
        "minecraftVersion": args.minecraft_version,
        "loader": args.loader,
        "neoForgeVersion": args.neoforge_version,
        "versionId": args.version_id,
        "profileName": args.profile_name,
        "files": files,
    }

    with output_path.open("w", encoding="utf-8") as stream:
        json.dump(manifest, stream, indent=2, ensure_ascii=False)
        stream.write("\n")

    print(f"Manifest generated: {output_path}")
    print(f"Files in manifest: {len(files)}")


if __name__ == "__main__":
    main()
