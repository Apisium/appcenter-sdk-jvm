/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.appcenter.channel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import com.microsoft.appcenter.http.HttpClient;
import com.microsoft.appcenter.ingestion.Ingestion;
import com.microsoft.appcenter.ingestion.OneCollectorIngestion;
import com.microsoft.appcenter.ingestion.models.Log;
import com.microsoft.appcenter.ingestion.models.json.LogSerializer;
import com.microsoft.appcenter.ingestion.models.one.CommonSchemaLog;
import com.microsoft.appcenter.ingestion.models.one.SdkExtension;
import com.microsoft.appcenter.utils.AppCenterLog;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.microsoft.appcenter.utils.AppCenterLog.LOG_TAG;

/**
 * One Collector channel listener used to redirect selected traffic to One Collector.
 */
public class OneCollectorChannelListener extends AbstractChannelListener {

    /**
     * Number of metrics queue items which will trigger synchronization.
     */
    @VisibleForTesting
    static final int ONE_COLLECTOR_TRIGGER_COUNT = 50;

    /**
     * Maximum number of requests being sent for the group.
     */
    @VisibleForTesting
    static final int ONE_COLLECTOR_TRIGGER_MAX_PARALLEL_REQUESTS = 2;

    /**
     * Postfix for One Collector's groups.
     */
    @VisibleForTesting
    static final String ONE_COLLECTOR_GROUP_NAME_SUFFIX = "/one";

    /**
     * Channel.
     */
    private final Channel mChannel;

    /**
     * Log serializer.
     */
    private final LogSerializer mLogSerializer;

    /**
     * Install id.
     */
    private final UUID mInstallId;

    /**
     * Ingestion instance.
     */
    private final Ingestion mIngestion;

    /**
     * Epochs and sequences grouped by iKey.
     */
    private final Map<String, EpochAndSeq> mEpochsAndSeqsByIKey = new HashMap<>();

    /**
     * Init with channel.
     *
     * @param channel       channel.
     * @param logSerializer log serializer.
     * @param httpClient    the HTTP client.
     * @param installId     installId.
     */
    public OneCollectorChannelListener(@NotNull Channel channel, @NotNull LogSerializer logSerializer, @NotNull HttpClient httpClient, @NotNull UUID installId) {
        this(new OneCollectorIngestion(httpClient, logSerializer), channel, logSerializer, installId);
    }

    @VisibleForTesting
    OneCollectorChannelListener(@NotNull OneCollectorIngestion ingestion, @NotNull Channel channel, @NotNull LogSerializer logSerializer, @NotNull UUID installId) {
        mChannel = channel;
        mLogSerializer = logSerializer;
        mInstallId = installId;
        mIngestion = ingestion;
    }

    /**
     * Update log URL.
     *
     * @param logUrl log URL.
     */
    public void setLogUrl(@NotNull String logUrl) {
        mIngestion.setLogUrl(logUrl);
    }

    @Override
    public void onGroupAdded(@NotNull String groupName, Channel.GroupListener groupListener, long batchTimeInterval) {
        if (isOneCollectorGroup(groupName)) {
            return;
        }
        String oneCollectorGroupName = getOneCollectorGroupName(groupName);
        mChannel.addGroup(oneCollectorGroupName, ONE_COLLECTOR_TRIGGER_COUNT, batchTimeInterval, ONE_COLLECTOR_TRIGGER_MAX_PARALLEL_REQUESTS, mIngestion, groupListener);
    }

    @Override
    public void onGroupRemoved(@NotNull String groupName) {
        if (isOneCollectorGroup(groupName)) {
            return;
        }
        String oneCollectorGroupName = getOneCollectorGroupName(groupName);
        mChannel.removeGroup(oneCollectorGroupName);
    }

    @Override
    public void onPreparedLog(@NotNull Log log, @NotNull String groupName, int flags) {

        /* Nothing to do on common schema log prepared. */
        if (!isOneCollectorCompatible(log)) {
            return;
        }

        /* Convert logs to Common Schema. */
        Collection<CommonSchemaLog> commonSchemaLogs;
        try {
            commonSchemaLogs = mLogSerializer.toCommonSchemaLog(log);
        } catch (IllegalArgumentException e) {
            AppCenterLog.error(LOG_TAG, "Cannot send a log to one collector: " + e.getMessage());
            return;
        }

        /* Add additional part A fields that are not known by the modules during conversion. */
        for (CommonSchemaLog commonSchemaLog : commonSchemaLogs) {

            /* Add flags. */
            commonSchemaLog.setFlags((long) flags);

            /* Add SDK extension missing fields: installId, epoch and seq. libVer is already set. */
            EpochAndSeq epochAndSeq = mEpochsAndSeqsByIKey.get(commonSchemaLog.getIKey());
            if (epochAndSeq == null) {
                epochAndSeq = new EpochAndSeq(UUID.randomUUID().toString());
                mEpochsAndSeqsByIKey.put(commonSchemaLog.getIKey(), epochAndSeq);
            }
            SdkExtension sdk = commonSchemaLog.getExt().getSdk();
            sdk.setEpoch(epochAndSeq.epoch);
            sdk.setSeq(++epochAndSeq.seq);
            sdk.setInstallId(mInstallId);
        }

        /* Enqueue logs to one collector group. */
        String oneCollectorGroupName = getOneCollectorGroupName(groupName);
        for (CommonSchemaLog commonSchemaLog : commonSchemaLogs) {
            mChannel.enqueue(commonSchemaLog, oneCollectorGroupName, flags);
        }
    }

    @Override
    public boolean shouldFilter(@NotNull Log log) {

        /* Don't send the logs to AppCenter if it is being sent to OneCollector. */
        return isOneCollectorCompatible(log);
    }

    /**
     * Get One Collector's group name for original one.
     *
     * @param groupName The group name.
     * @return The One Collector's group name.
     */
    private static String getOneCollectorGroupName(@NotNull String groupName) {
        return groupName + ONE_COLLECTOR_GROUP_NAME_SUFFIX;
    }

    @Override
    public void onClear(@NotNull String groupName) {
        if (isOneCollectorGroup(groupName)) {
            return;
        }
        mChannel.clear(getOneCollectorGroupName(groupName));
    }

    @Override
    public void onPaused(@NotNull String groupName, String targetToken) {
        if (isOneCollectorGroup(groupName)) {
            return;
        }
        mChannel.pauseGroup(getOneCollectorGroupName(groupName), targetToken);
    }

    @Override
    public void onResumed(@NotNull String groupName, String targetToken) {
        if (isOneCollectorGroup(groupName)) {
            return;
        }
        mChannel.resumeGroup(getOneCollectorGroupName(groupName), targetToken);
    }

    /**
     * Checks if the group has One Collector's postfix.
     *
     * @param groupName The group name.
     * @return true if group has One Collector's postfix, false otherwise.
     */
    private static boolean isOneCollectorGroup(@NotNull String groupName) {
        return groupName.endsWith(ONE_COLLECTOR_GROUP_NAME_SUFFIX);
    }

    /**
     * Checks if the log is compatible with One Collector.
     *
     * @param log The log.
     * @return true if the log is compatible with One Collector, false otherwise.
     */
    private static boolean isOneCollectorCompatible(@NotNull Log log) {
        return !(log instanceof CommonSchemaLog) && !log.getTransmissionTargetTokens().isEmpty();
    }

    @Override
    public void onGloballyEnabled(boolean isEnabled) {
        if (!isEnabled) {
            mEpochsAndSeqsByIKey.clear();
        }
    }

    /**
     * Epoch and sequence number for logs.
     */
    private static class EpochAndSeq {

        /**
         * Epoch.
         */
        final String epoch;

        /**
         * Sequence number.
         */
        long seq;

        /**
         * Init.
         */
        EpochAndSeq(String epoch) {
            this.epoch = epoch;
        }
    }
}
