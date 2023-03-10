/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.appcenter.ingestion.models.json;

import org.jetbrains.annotations.NotNull;

import com.microsoft.appcenter.ingestion.models.Log;
import com.microsoft.appcenter.ingestion.models.LogContainer;
import com.microsoft.appcenter.ingestion.models.one.CommonSchemaLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.microsoft.appcenter.ingestion.models.CommonProperties.TYPE;

public class DefaultLogSerializer implements LogSerializer {

    private static final String LOGS = "logs";

    private final Map<String, LogFactory> mLogFactories = new HashMap<>();

    @NotNull
    private JSONStringer writeLog(JSONStringer writer, Log log) throws JSONException {
        writer.object();
        log.write(writer);
        writer.endObject();
        return writer;
    }

    @NotNull
    private Log readLog(JSONObject object, String type) throws JSONException {
        if (type == null) {
            type = object.getString(TYPE);
        }
        LogFactory logFactory = mLogFactories.get(type);
        if (logFactory == null) {
            throw new JSONException("Unknown log type: " + type);
        }
        Log log = logFactory.create();
        log.read(object);
        return log;
    }

    @NotNull
    @Override
    public String serializeLog(@NotNull Log log) throws JSONException {
        return writeLog(new JSONStringer(), log).toString();
    }

    @NotNull
    @Override
    public Log deserializeLog(@NotNull String json, String type) throws JSONException {
        return readLog(new JSONObject(json), type);
    }

    @Override
    public Collection<CommonSchemaLog> toCommonSchemaLog(@NotNull Log log) {
        return mLogFactories.get(log.getType()).toCommonSchemaLogs(log);
    }

    @NotNull
    @Override
    public String serializeContainer(@NotNull LogContainer logContainer) throws JSONException {

        /* Init JSON serializer. */
        JSONStringer writer = new JSONStringer();

        /* Start writing JSON. */
        writer.object();
        writer.key(LOGS).array();
        for (Log log : logContainer.getLogs()) {
            writeLog(writer, log);
        }
        writer.endArray();
        writer.endObject();
        return writer.toString();
    }

    @NotNull
    @Override
    public LogContainer deserializeContainer(@NotNull String json, String type) throws JSONException {
        JSONObject jContainer = new JSONObject(json);
        LogContainer container = new LogContainer();
        JSONArray jLogs = jContainer.getJSONArray(LOGS);
        List<Log> logs = new ArrayList<>();
        for (int i = 0; i < jLogs.length(); i++) {
            JSONObject jLog = jLogs.getJSONObject(i);
            Log log = readLog(jLog, type);
            logs.add(log);
        }
        container.setLogs(logs);
        return container;
    }

    @Override
    public void addLogFactory(@NotNull String logType, @NotNull LogFactory logFactory) {
        mLogFactories.put(logType, logFactory);
    }
}
