/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.appcenter.http;

import org.jetbrains.annotations.NotNull;
//import android.text.TextUtils;

import java.io.IOException;

/**
 * HTTP exception.
 */
public class HttpException extends IOException {

    /**
     * HTTP response.
     */
    private final HttpResponse mHttpResponse;

    /**
     * Init.
     *
     * @param httpResponse The HTTP response.
     */
    public HttpException(@NotNull HttpResponse httpResponse) {
        super(getDetailMessage(httpResponse.getStatusCode(), httpResponse.getPayload()));
        mHttpResponse = httpResponse;
    }

    @NotNull
    private static String getDetailMessage(int status, @NotNull String payload) {
        if (payload.isEmpty()) {
            return String.valueOf(status);
        }
        return status + " - " + payload;
    }

    /**
     * Get the HTTP response.
     *
     * @return HTTP response.
     */
    public HttpResponse getHttpResponse() {
        return mHttpResponse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HttpException that = (HttpException) o;

        return mHttpResponse.equals(that.mHttpResponse);
    }

    @Override
    public int hashCode() {
        return mHttpResponse.hashCode();
    }
}