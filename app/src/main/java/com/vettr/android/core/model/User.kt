package com.vettr.android.core.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * User entity for authentication and user preferences.
 * Stores authenticated user data and subscription information.
 *
 * @property id Unique identifier (auto-generated UUID)
 * @property email User's email address
 * @property displayName User's display name
 * @property avatarUrl URL to user's avatar image (nullable)
 * @property tier Subscription tier (e.g., "Free", "Pro", "Enterprise")
 * @property createdAt Account creation timestamp (Unix epoch milliseconds)
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val email: String,

    @ColumnInfo(name = "display_name")
    val displayName: String,

    @ColumnInfo(name = "avatar_url")
    val avatarUrl: String?,

    val tier: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
