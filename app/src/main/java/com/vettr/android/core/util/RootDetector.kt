package com.vettr.android.core.util

import java.io.File

/**
 * Lightweight runtime root detection.
 *
 * Checks for common root indicators without requiring external libraries.
 * Not foolproof — advanced rooting tools can mask themselves — but catches the
 * common case and satisfies app-store reviewer expectations.
 *
 * Policy: never block the user; surface an informational warning only.
 */
object RootDetector {

    /** Returns true if the device shows signs of being rooted. Lazily evaluated once. */
    val isRooted: Boolean by lazy {
        hasSuBinary || hasRootApps || hasDangerousDebugProp
    }

    // -------------------------------------------------------------------------
    // Private checks
    // -------------------------------------------------------------------------

    private val suPaths = listOf(
        "/system/bin/su",
        "/system/xbin/su",
        "/sbin/su",
        "/data/local/su",
        "/data/local/bin/su",
        "/data/local/xbin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
    )

    private val rootAppPaths = listOf(
        "/system/app/Superuser.apk",
        "/system/app/SuperSU.apk",
        "/sbin/.magisk",
        "/sbin/.core/mirror",
        "/data/adb/magisk",
    )

    /** True if any known su binary is present. */
    private val hasSuBinary: Boolean
        get() = suPaths.any { File(it).exists() }

    /** True if any known root-management app artifact is present. */
    private val hasRootApps: Boolean
        get() = rootAppPaths.any { File(it).exists() }

    /**
     * True if ro.debuggable == 1, which is set on custom/debug builds that
     * allow root access via adb. Production ROMs always return 0.
     */
    private val hasDangerousDebugProp: Boolean
        get() = try {
            val process = Runtime.getRuntime().exec(arrayOf("getprop", "ro.debuggable"))
            val result = process.inputStream.bufferedReader().readLine()?.trim()
            process.destroy()
            result == "1"
        } catch (_: Exception) {
            false
        }
}
