package com.vettr.android.core.util

import android.content.Context
import android.content.pm.PackageInfo
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages app version information and update checks.
 *
 * Provides access to:
 * - Current app version (name and code)
 * - Build information
 * - Update availability (mock implementation for now)
 */
@Singleton
class VersionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Get current app version name (e.g., "1.0.0").
     */
    fun getVersionName(): String {
        return try {
            val packageInfo = getPackageInfo()
            packageInfo.versionName ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    /**
     * Get current app version code (integer build number).
     */
    fun getVersionCode(): Long {
        return try {
            val packageInfo = getPackageInfo()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Get formatted version string (e.g., "1.0.0 (1)").
     */
    fun getFormattedVersion(): String {
        return "${getVersionName()} (${getVersionCode()})"
    }

    /**
     * Get build type (debug or release).
     */
    fun getBuildType(): String {
        return if (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            "Debug"
        } else {
            "Release"
        }
    }

    /**
     * Check if an update is available (mock implementation).
     *
     * In a real implementation, this would:
     * 1. Call Play Store API or custom backend
     * 2. Compare remote version with local version
     * 3. Return update availability with metadata
     *
     * @return UpdateInfo with availability status
     */
    suspend fun checkForUpdates(): UpdateInfo {
        // Mock implementation - always returns no update available
        // In production, this would make a network call to check for updates
        return UpdateInfo(
            isAvailable = false,
            latestVersion = getVersionName(),
            releaseNotes = null,
            isRequired = false
        )
    }

    private fun getPackageInfo(): PackageInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                android.content.pm.PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
    }
}

/**
 * Represents update information from the update check.
 */
data class UpdateInfo(
    val isAvailable: Boolean,
    val latestVersion: String,
    val releaseNotes: String?,
    val isRequired: Boolean
)
