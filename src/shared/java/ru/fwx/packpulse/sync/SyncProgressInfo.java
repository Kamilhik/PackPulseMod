package ru.fwx.packpulse.sync;

public record SyncProgressInfo(
    String filePath,
    SyncStage stage,
    int currentFile,
    int totalFiles
) {
}

