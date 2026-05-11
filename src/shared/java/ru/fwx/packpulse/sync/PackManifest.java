package ru.fwx.packpulse.sync;

import java.util.List;

public record PackManifest(
    String name,
    String version,
    String minecraftVersion,
    String loader,
    String neoForgeVersion,
    String versionId,
    String profileName,
    List<PackFileEntry> files,
    List<String> delete
) {
}

