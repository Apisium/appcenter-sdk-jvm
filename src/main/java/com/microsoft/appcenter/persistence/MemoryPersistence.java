package com.microsoft.appcenter.persistence;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.microsoft.appcenter.ingestion.models.Log;

import java.io.IOException;
import java.util.*;

public class MemoryPersistence extends Persistence {
    private final HashMap<String, List<Log>> mLogs = new HashMap<>();
    private final HashMap<String, Set<Log>> batches = new HashMap<>();
    private int id = 0;
    @Override
    public long putLog(@NotNull Log log, @NotNull String group, int flags) throws PersistenceException {
        mLogs.computeIfAbsent(group, k -> new ArrayList<>()).add(log);
        return id++;
    }

    @Override
    public void deleteLogs(@NotNull String group, @NotNull String batchId) {
        Set<Log> logs = batches.get(batchId);
        if (logs == null) return;
        List<Log> logs2 = mLogs.get(group);
        if (logs2 == null) return;
        logs2.removeAll(logs);
    }

    @Override
    public void deleteLogs(String group) {
        mLogs.remove(group);
    }

    @Override
    public int countLogs(@NotNull String group) {
        List<Log> log = mLogs.get(group);
        return log == null ? 0 : log.size();
    }

    @Nullable
    @Override
    public String getLogs(@NotNull String group, @NotNull Collection<String> pausedTargetKeys, int limit, @NotNull List<Log> outLogs) {
        String batchId = UUID.randomUUID().toString();
        List<Log> log = mLogs.get(group);
        if (log == null) return batchId;
        Set<Log> log2 = new HashSet<>(log.subList(0, Math.min(limit, log.size())));
        batches.put(batchId, log2);
        outLogs.addAll(log2);
        return batchId;
    }

    @Override
    public void clearPendingLogState() {
        mLogs.clear();
        batches.clear();
    }

    @Override
    public boolean setMaxStorageSize(long maxStorageSizeInBytes) {
        return false;
    }

    @Override
    public void close() throws IOException {
        clearPendingLogState();
    }
}
