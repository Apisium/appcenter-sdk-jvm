/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.appcenter.crashes;

import org.jetbrains.annotations.VisibleForTesting;

//import com.microsoft.appcenter.utils.ShutdownHelper;

class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private boolean mIgnoreDefaultExceptionHandler = false;

    private Thread.UncaughtExceptionHandler mDefaultUncaughtExceptionHandler;

    @Override
    public void uncaughtException(Thread thread, Throwable exception) {
        if (System.getProperty("com.microsoft.appcenter.crashes.uncaughtexception.autosend", "").equals("true")) {
            Crashes.trackCrash(exception, thread, null);
        } else Crashes.getInstance().saveUncaughtException(thread, exception);
        if (mDefaultUncaughtExceptionHandler != null) {
            mDefaultUncaughtExceptionHandler.uncaughtException(thread, exception);
        } else {
//            ShutdownHelper.shutdown(10);
        }
    }

    @VisibleForTesting
    void setIgnoreDefaultExceptionHandler(boolean ignoreDefaultExceptionHandler) {
        mIgnoreDefaultExceptionHandler = ignoreDefaultExceptionHandler;
        if (ignoreDefaultExceptionHandler) {
            mDefaultUncaughtExceptionHandler = null;
        }
    }

    @VisibleForTesting
    Thread.UncaughtExceptionHandler getDefaultUncaughtExceptionHandler() {
        return mDefaultUncaughtExceptionHandler;
    }

    void register() {
        if (!mIgnoreDefaultExceptionHandler) {
            mDefaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        } else {
            mDefaultUncaughtExceptionHandler = null;
        }
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    void unregister() {
        Thread.setDefaultUncaughtExceptionHandler(mDefaultUncaughtExceptionHandler);
    }
}
