package ru.fwx.packpulse.sync;

import java.util.List;

public record UpdatePlan(List<String> filesToDownload) {
    public boolean isEmpty() {
        return filesToDownload == null || filesToDownload.isEmpty();
    }
}

