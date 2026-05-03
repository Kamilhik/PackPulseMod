package ru.fwx.packpulse.sync;

public record PackSyncResult(
    int updatedFiles,
    int removedFiles,
    boolean restartRequired
) {
}

