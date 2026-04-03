package ru.fwx.freewebupdater.sync;

public record PackSyncResult(
    int updatedFiles,
    int removedFiles,
    boolean restartRequired
) {
}
