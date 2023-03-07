package com.microsoft.appcenter.persistence;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.microsoft.appcenter.ingestion.models.Log;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

public class MemoryPersistence extends Persistence {
    private final HashMap<String, List<Log>> mLogs = new HashMap<>();
    private final HashMap<String, Set<Log>> batches = new HashMap<>();
    private int id = 0;
    @Override
    public long putLog(@NonNull Log log, @NonNull String group, int flags) throws PersistenceException {
        mLogs.computeIfAbsent(group, k -> new ArrayList<>()).add(log);
        return id++;
    }

    @Override
    public void deleteLogs(@NonNull String group, @NonNull String batchId) {
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
    public int countLogs(@NonNull String group) {
        List<Log> log = mLogs.get(group);
        return log == null ? 0 : log.size();
    }

    @Nullable
    @Override
    public String getLogs(@NonNull String group, @NonNull Collection<String> pausedTargetKeys, int limit, @NonNull List<Log> outLogs) {
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
