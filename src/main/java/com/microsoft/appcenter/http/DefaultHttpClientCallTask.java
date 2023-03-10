/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.appcenter.http;

//import android.net.TrafficStats;
import android.os.AsyncTask;
import android.util.Log;

import com.microsoft.appcenter.utils.AppCenterLog;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.HttpsURLConnection;

import static com.microsoft.appcenter.AppCenter.LOG_TAG;
import static com.microsoft.appcenter.http.DefaultHttpClient.CHARSET_NAME;
import static com.microsoft.appcenter.http.DefaultHttpClient.CONTENT_ENCODING_KEY;
import static com.microsoft.appcenter.http.DefaultHttpClient.CONTENT_ENCODING_VALUE;
import static com.microsoft.appcenter.http.DefaultHttpClient.CONTENT_TYPE_KEY;
import static com.microsoft.appcenter.http.DefaultHttpClient.CONTENT_TYPE_VALUE;
import static com.microsoft.appcenter.http.DefaultHttpClient.METHOD_POST;
import static com.microsoft.appcenter.http.HttpUtils.READ_BUFFER_SIZE;
//import static com.microsoft.appcenter.http.HttpUtils.THREAD_STATS_TAG;
import static com.microsoft.appcenter.http.HttpUtils.WRITE_BUFFER_SIZE;
import static com.microsoft.appcenter.http.HttpUtils.createHttpsConnection;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Async task for default HTTP client.
 */
class DefaultHttpClientCallTask extends AsyncTask<Void, Void, Object> {

    /**
     * Default string builder capacity.
     */
    private static final int DEFAULT_STRING_BUILDER_CAPACITY = 16;

    /**
     * Minimum payload length in bytes to use gzip.
     */
    private static final int MIN_GZIP_LENGTH = 1400;

    /**
     * Maximum payload length to use prettify for logging.
     */
    private static final int MAX_PRETTIFY_LOG_LENGTH = 4 * 1024;

    /**
     * Pattern used to replace token in url encoded parameters.
     */
    private static final Pattern TOKEN_REGEX_URL_ENCODED = Pattern.compile("token=[^&]+");

    /**
     * Pattern used to replace token in json responses.
     */
    private static final Pattern TOKEN_REGEX_JSON = Pattern.compile("token\":\"[^\"]+\"");

    /**
     * Pattern used to replace redirect URI in json responses.
     */
    private static final Pattern REDIRECT_URI_REGEX_JSON = Pattern.compile("redirect_uri\":\"[^\"]+\"");

    private final String mUrl;

    private final String mMethod;

    private final Map<String, String> mHeaders;

    private final HttpClient.CallTemplate mCallTemplate;

    private final ServiceCallback mServiceCallback;

    private final Tracker mTracker;

    private final boolean mCompressionEnabled;

    DefaultHttpClientCallTask(String url, String method, Map<String, String> headers, HttpClient.CallTemplate callTemplate, ServiceCallback serviceCallback, Tracker tracker, boolean compressionEnabled) {
        mUrl = url;
        mMethod = method;
        mHeaders = headers;
        mCallTemplate = callTemplate;
        mServiceCallback = serviceCallback;
        mTracker = tracker;
        mCompressionEnabled = compressionEnabled;
    }

    private static InputStream getInputStream(HttpsURLConnection httpsURLConnection) throws IOException {
        int status = httpsURLConnection.getResponseCode();
        if (status >= 200 && status < 400) {
            return httpsURLConnection.getInputStream();
        } else {
            return httpsURLConnection.getErrorStream();
        }
    }

    /**
     * Write payload to output stream.
     */
    private void writePayload(OutputStream out, byte[] payload) throws IOException {
        for (int i = 0; i < payload.length; i += WRITE_BUFFER_SIZE) {
            out.write(payload, i, min(payload.length - i, WRITE_BUFFER_SIZE));
            if (isCancelled()) {
                break;
            }
        }
    }

    /**
     * Dump response stream to a string.
     */
    private String readResponse(HttpsURLConnection httpsURLConnection) throws IOException {

        /*
         * Though content length header value is less than actual payload length (gzip), we want to init
         * buffer with a reasonable start size to optimize (default is 16 and is way too low for this
         * use case).
         */
        StringBuilder builder = new StringBuilder(max(httpsURLConnection.getContentLength(), DEFAULT_STRING_BUILDER_CAPACITY));

        try (InputStream stream = getInputStream(httpsURLConnection)) {
            Reader reader = new InputStreamReader(stream, CHARSET_NAME);
            char[] buffer = new char[READ_BUFFER_SIZE];
            int len;
            while ((len = reader.read(buffer)) > 0) {
                builder.append(buffer, 0, len);
                if (isCancelled()) {
                    break;
                }
            }
            return builder.toString();
        }
    }

    /**
     * Do http call.
     */
    private HttpResponse doHttpCall() throws Exception {
        URL url = new URL(mUrl);
        HttpsURLConnection httpsURLConnection = createHttpsConnection(url);
        try {

            /* Build payload now if POST. */
            httpsURLConnection.setRequestMethod(mMethod);
            String payload = null;
            byte[] binaryPayload = null;
            boolean shouldCompress = false;
            boolean isPost = mMethod.equals(METHOD_POST);
            if (isPost && mCallTemplate != null) {

                /* Get bytes, check if large enough to compress. */
                payload = mCallTemplate.buildRequestBody();
                binaryPayload = payload.getBytes(CHARSET_NAME);
                shouldCompress = mCompressionEnabled && binaryPayload.length >= MIN_GZIP_LENGTH;

                /* If no content type specified, assume json. */
                if (!mHeaders.containsKey(CONTENT_TYPE_KEY)) {
                    mHeaders.put(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE);
                }
            }

            /* If about to compress, add corresponding header. */
            if (shouldCompress) {
                mHeaders.put(CONTENT_ENCODING_KEY, CONTENT_ENCODING_VALUE);
            }

            /* Send headers. */
            for (Map.Entry<String, String> header : mHeaders.entrySet()) {
                httpsURLConnection.setRequestProperty(header.getKey(), header.getValue());
            }
            if (isCancelled()) {
                return null;
            }

            /* Call back before the payload is sent. */
            if (mCallTemplate != null) {
                mCallTemplate.onBeforeCalling(url, mHeaders);
            }

            /* Send payload. */
            if (binaryPayload != null) {

                /* Log payload. */
                if (AppCenterLog.getLogLevel() <= Log.VERBOSE) {
                    if (payload.length() < MAX_PRETTIFY_LOG_LENGTH) {
                        payload = TOKEN_REGEX_URL_ENCODED.matcher(payload).replaceAll("token=***");
                        if (CONTENT_TYPE_VALUE.equals(mHeaders.get(CONTENT_TYPE_KEY))) {
                            payload = new JSONObject(payload).toString(2);
                        }
                    }
                    AppCenterLog.verbose(LOG_TAG, payload);
                }

                /* Compress payload if large enough to be worth it. */
                if (shouldCompress) {
                    ByteArrayOutputStream gzipBuffer = new ByteArrayOutputStream(binaryPayload.length);
                    GZIPOutputStream gzipStream = new GZIPOutputStream(gzipBuffer);
                    gzipStream.write(binaryPayload);
                    gzipStream.close();
                    binaryPayload = gzipBuffer.toByteArray();
                }

                /* Send payload on the wire. */
                httpsURLConnection.setDoOutput(true);
                httpsURLConnection.setFixedLengthStreamingMode(binaryPayload.length);
                OutputStream out = httpsURLConnection.getOutputStream();

                //noinspection TryFinallyCanBeTryWithResources
                try {
                    writePayload(out, binaryPayload);
                } finally {
                    out.close();
                }
            }
            if (isCancelled()) {
                return null;
            }

            /* Read response. */
            int status = httpsURLConnection.getResponseCode();
            String response = readResponse(httpsURLConnection);
            if (AppCenterLog.getLogLevel() <= Log.VERBOSE) {
                String contentType = httpsURLConnection.getHeaderField(CONTENT_TYPE_KEY);
                String logPayload;
                if (contentType == null || contentType.startsWith("text/") || contentType.startsWith("application/")) {
                    logPayload = TOKEN_REGEX_JSON.matcher(response).replaceAll("token\":\"***\"");
                    logPayload = REDIRECT_URI_REGEX_JSON.matcher(logPayload).replaceAll("redirect_uri\":\"***\"");
                } else {
                    logPayload = "<binary>";
                }
                AppCenterLog.verbose(LOG_TAG, "HTTP response status=" + status + " payload=" + logPayload);
            }
            Map<String, String> responseHeaders = new HashMap<>();
            for (Map.Entry<String, List<String>> header : httpsURLConnection.getHeaderFields().entrySet()) {
                responseHeaders.put(header.getKey(), header.getValue().iterator().next());
            }
            HttpResponse httpResponse = new HttpResponse(status, response, responseHeaders);

            /* Accept all 2xx codes. */
            if (status >= 200 && status < 300) {
                return httpResponse;
            }

            /* Generate exception on failure. */
            throw new HttpException(httpResponse);
        } finally {

            /* Release connection. */
            httpsURLConnection.disconnect();
        }
    }

    @Override
    protected Object doInBackground(Void... params) {

        /* Do tag socket to avoid strict mode issue. */
//        TrafficStats.setThreadStatsTag(THREAD_STATS_TAG);
        try {
            return doHttpCall();
        } catch (Exception e) {
            return e;
        } finally {
//            TrafficStats.clearThreadStatsTag();
        }
    }

    @Override
    protected void onPreExecute() {
        mTracker.onStart(this);
    }

    @Override
    protected void onPostExecute(Object result) {
        mTracker.onFinish(this);
        if (result instanceof Exception) {
            mServiceCallback.onCallFailed((Exception) result);
        } else {
            HttpResponse response = (HttpResponse) result;
            mServiceCallback.onCallSucceeded(response);
        }
    }

    @Override
    protected void onCancelled(Object result) {

        /* Handle the result even if it was cancelled. */
        if (result instanceof HttpResponse || result instanceof HttpException) {
            onPostExecute(result);
        } else {
            mTracker.onFinish(this);
        }
    }

    /**
     * The callback used for maintain ongoing call tasks.
     */
    interface Tracker {

        /**
         * Called before the http call operation.
         *
         * @param task The http call.
         */
        void onStart(DefaultHttpClientCallTask task);

        /**
         * Called after the http call operation.
         *
         * @param task The http call.
         */
        void onFinish(DefaultHttpClientCallTask task);
    }
}
