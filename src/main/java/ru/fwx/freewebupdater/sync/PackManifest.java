package ru.fwx.freewebupdater.sync;

import java.util.List;

public record PackManifest(
    String name,
    String version,
    String minecraftVersion,
    String loader,
    String fabricLoaderVersion,
    String versionId,
    String profileName,
    List<PackFileEntry> files
) {
}
