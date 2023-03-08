/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.appcenter.channel;


import org.jetbrains.annotations.NotNull;

import com.microsoft.appcenter.ingestion.models.Log;

/**
 * Empty implementation to make callbacks optional.
 */
public class AbstractChannelListener implements Channel.Listener {

    @Override
    public void onGroupAdded(@NotNull String groupName, Channel.GroupListener groupListener, long batchTimeInterval) {
    }

    @Override
    public void onGroupRemoved(@NotNull String groupName) {
    }

    @Override
    public void onPreparingLog(@NotNull Log log, @NotNull String groupName) {
    }

    @Override
    public void onPreparedLog(@NotNull Log log, @NotNull String groupName, int flags) {
    }

    @Override
    public boolean shouldFilter(@NotNull Log log) {
        return false;
    }

    @Override
    public void onGloballyEnabled(boolean isEnabled) {
    }

    @Override
    public void onClear(@NotNull String groupName) {
    }

    @Override
    public void onPaused(@NotNull String groupName, String targetToken) {
    }

    @Override
    public void onResumed(@NotNull String groupName, String targetToken) {
    }
}
