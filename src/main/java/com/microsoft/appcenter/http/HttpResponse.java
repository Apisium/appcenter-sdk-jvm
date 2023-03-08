/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.appcenter.http;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP response.
 */
public class HttpResponse {

    /**
     * HTTP status code.
     */
    private final int statusCode;

    /**
     * HTTP payload.
     */
    private final String payload;

    /**
     * HTTP headers.
     */
    private final Map<String, String> headers;

    /**
     * Init with empty response body.
     *
     * @param status HTTP status code.
     */
    public HttpResponse(int status) {
        this(status, "");
    }

    /**
     * Init.
     *
     * @param status  HTTP status code.
     * @param payload HTTP payload.
     */
    public HttpResponse(int status, @NotNull String payload) {
        this(status, payload, new HashMap<String, String>());
    }

    /**
     * Init.
     *
     * @param status  HTTP status code.
     * @param payload HTTP payload.
     * @param headers HTTP responseHeaders.
     */
    public HttpResponse(int status, @NotNull String payload, @NotNull Map<String, String> headers) {
        this.payload = payload;
        this.statusCode = status;
        this.headers = headers;
    }

    /**
     * Get the HTTP status code.
     *
     * @return HTTP status code.
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Get the HTTP payload (response body).
     *
     * @return HTTP payload. Can be empty string.
     */
    @NotNull
    public String getPayload() {
        return payload;
    }

    /**
     * Get the HTTP headers (response headers).
     *
     * @return HTTP headers.
     */
    @NotNull
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HttpResponse that = (HttpResponse) o;

        return (statusCode == that.statusCode && payload.equals(that.payload) && headers.equals(that.headers));
    }

    @Override
    public int hashCode() {
        int result = statusCode;
        result = 31 * result + payload.hashCode();
        result = 31 * result + headers.hashCode();
        return result;
    }
}
