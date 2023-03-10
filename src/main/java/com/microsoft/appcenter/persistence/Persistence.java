/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.appcenter.persistence;

//import org.jetbrains.annotations.IntRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.microsoft.appcenter.Flags;
import com.microsoft.appcenter.ingestion.models.Log;
import com.microsoft.appcenter.ingestion.models.json.LogSerializer;

import java.io.Closeable;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Abstract class for Persistence service.
 */
public abstract class Persistence implements Closeable {

    /**
     * Log serializer override.
     */
    private LogSerializer mLogSerializer;

    /**
     * Writes a log to the storage with the given {@code group}.
     *
     * @param log   The log to be placed in the storage.
     * @param group The group of the storage for the log.
     * @param flags The persistence flags.
     * @return Log identifier from persistence after saving.
     * @throws PersistenceException Exception will be thrown if Persistence cannot write a log to the storage.
     */
    public abstract long putLog(@NotNull Log log, @NotNull String group,
//                                @IntRange(from = Flags.NORMAL, to = Flags.CRITICAL)
                                int flags) throws PersistenceException;

    /**
     * Deletes a log with the give ID from the {@code group}.
     *
     * @param group   The group of the storage for logs.
     * @param batchId The ID for a set of logs.
     */
    public abstract void deleteLogs(@NotNull String group, @NotNull String batchId);

    /**
     * Deletes all logs for the given {@code group}.
     *
     * @param group The group of the storage for logs.
     */
    public abstract void deleteLogs(String group);

    /**
     * Gets the number of logs for the given {@code group}.
     *
     * @param group The group of the storage for logs.
     * @return The number of logs for the given {@code group}.
     */
    public abstract int countLogs(@NotNull String group);

    /**
     * Gets an array of logs for the given {@code group}.
     *
     * @param group            The group of the storage for logs.
     * @param pausedTargetKeys List of target token keys to exclude from the log query.
     * @param limit            The max number of logs to be returned.
     * @param outLogs          A list to receive {@link Log} objects.
     * @return An ID for {@code outLogs}. {@code null} if no logs exist.
     */
    @Nullable
    public abstract String getLogs(@NotNull String group, @NotNull Collection<String> pausedTargetKeys,
//                                   @IntRange(from = 0)
                                   int limit, @NotNull List<Log> outLogs);

    /**
     * Clears all associations between logs of the {@code group} and ids returned by {@link #getLogs(String, Collection, int, List)}}.
     */
    public abstract void clearPendingLogState();

    /**
     * Gets a {@link LogSerializer}.
     *
     * @return The log serializer instance.
     */
    LogSerializer getLogSerializer() {
        if (mLogSerializer == null) {
            throw new IllegalStateException("logSerializer not configured");
        }
        return mLogSerializer;
    }

    /**
     * Sets a {@link LogSerializer}.
     *
     * @param logSerializer The log serializer instance.
     */
    public void setLogSerializer(@NotNull LogSerializer logSerializer) {
        mLogSerializer = logSerializer;
    }

    /**
     * Set maximum SQLite database size.
     *
     * @param maxStorageSizeInBytes Maximum SQLite database size.
     * @return true if database size was set, otherwise false.
     */
    public abstract boolean setMaxStorageSize(long maxStorageSizeInBytes);

    /**
     * Thrown when {@link Persistence} cannot write a log to the storage.
     */
    public static class PersistenceException extends Exception {
        public PersistenceException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        @SuppressWarnings("SameParameterValue")
        PersistenceException(String detailMessage) {
            super(detailMessage);
        }
    }
}
