package ru.fwx.freewebupdater.sync;

public record PackFileEntry(
    String path,
    String url,
    String sha256
) {
}
