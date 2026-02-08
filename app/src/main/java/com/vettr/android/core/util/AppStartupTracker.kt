package com.vettr.android.core.util

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.util.Log

/**
 * ContentProvider-based startup tracker that measures time from process start to Application.onCreate.
 *
 * ContentProvider.onCreate() is called very early in the app lifecycle, before Application.onCreate(),
 * making it ideal for measuring cold start time. This approach is recommended by Android's App Startup library.
 *
 * The startup time is stored in a companion object so it can be accessed by VettrApp to calculate
 * the total duration and report it to ObservabilityService.
 */
class AppStartupTracker : ContentProvider() {

    companion object {
        private const val TAG = "AppStartupTracker"

        /**
         * Timestamp when the ContentProvider was initialized (process start time).
         * This is effectively the app cold start time.
         */
        @Volatile
        var startTime: Long = 0L
            private set

        /**
         * Calculate the duration from startup to now.
         * @return Duration in milliseconds
         */
        fun getElapsedTime(): Long {
            return if (startTime > 0) {
                System.currentTimeMillis() - startTime
            } else {
                0L
            }
        }

        /**
         * Reset the start time (used for testing).
         */
        internal fun reset() {
            startTime = 0L
        }
    }

    override fun onCreate(): Boolean {
        startTime = System.currentTimeMillis()
        Log.d(TAG, "App startup tracking initialized at $startTime")
        return true
    }

    // Required ContentProvider methods (not used for startup tracking)
    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0
}
