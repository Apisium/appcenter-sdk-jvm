/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.appcenter.ingestion.models.json;

import org.jetbrains.annotations.NotNull;

import com.microsoft.appcenter.ingestion.models.Log;
import com.microsoft.appcenter.ingestion.models.LogContainer;
import com.microsoft.appcenter.ingestion.models.one.CommonSchemaLog;

import org.json.JSONException;

import java.util.Collection;

public interface LogSerializer {

    @NotNull
    String serializeLog(@NotNull Log log) throws JSONException;

    @NotNull
    Log deserializeLog(@NotNull String json, String type) throws JSONException;

    Collection<CommonSchemaLog> toCommonSchemaLog(@NotNull Log log);

    @NotNull
    String serializeContainer(@NotNull LogContainer container) throws JSONException;

    @NotNull
    LogContainer deserializeContainer(@NotNull String json, String type) throws JSONException;

    void addLogFactory(@NotNull String logType, @NotNull LogFactory logFactory);
}
