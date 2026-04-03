package ru.fwx.freewebupdater.sync;

public record SyncProgressInfo(
    String filePath,
    SyncStage stage,
    int currentFile,
    int totalFiles
) {
}
