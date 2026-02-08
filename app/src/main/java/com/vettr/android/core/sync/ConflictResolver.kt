package com.vettr.android.core.sync

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves conflicts between local and server data during sync.
 * Supports multiple resolution strategies for different use cases.
 */
@Singleton
class ConflictResolver @Inject constructor() {

    /**
     * Represents a conflict between local and server data.
     */
    data class Conflict<T>(
        val local: T?,
        val server: T?,
        val localTimestamp: Long,
        val serverTimestamp: Long
    )

    /**
     * Result of conflict resolution.
     */
    sealed class ResolutionResult<T> {
        data class Resolved<T>(val data: T) : ResolutionResult<T>()
        data class ManualRequired<T>(val conflict: Conflict<T>) : ResolutionResult<T>()
    }

    /**
     * Resolution strategy types.
     */
    enum class Strategy {
        /**
         * Choose data with the most recent timestamp.
         */
        LAST_WRITE_WINS,

        /**
         * Always prefer local data over server data.
         */
        LOCAL_WINS,

        /**
         * Always prefer server data over local data.
         */
        SERVER_WINS,

        /**
         * Require manual intervention to resolve the conflict.
         */
        MANUAL
    }

    /**
     * Resolve a conflict using the specified strategy.
     *
     * @param conflict The conflict to resolve
     * @param strategy The resolution strategy to use
     * @return The resolution result
     */
    fun <T> resolve(
        conflict: Conflict<T>,
        strategy: Strategy
    ): ResolutionResult<T> {
        return when (strategy) {
            Strategy.LAST_WRITE_WINS -> resolveLastWriteWins(conflict)
            Strategy.LOCAL_WINS -> resolveLocalWins(conflict)
            Strategy.SERVER_WINS -> resolveServerWins(conflict)
            Strategy.MANUAL -> ResolutionResult.ManualRequired(conflict)
        }
    }

    /**
     * Resolve using last-write-wins strategy.
     * Chooses data with the most recent timestamp.
     */
    private fun <T> resolveLastWriteWins(conflict: Conflict<T>): ResolutionResult<T> {
        return when {
            conflict.local == null && conflict.server == null -> {
                throw IllegalArgumentException("Both local and server data are null")
            }
            conflict.local == null -> {
                ResolutionResult.Resolved(conflict.server!!)
            }
            conflict.server == null -> {
                ResolutionResult.Resolved(conflict.local)
            }
            conflict.localTimestamp >= conflict.serverTimestamp -> {
                ResolutionResult.Resolved(conflict.local)
            }
            else -> {
                ResolutionResult.Resolved(conflict.server)
            }
        }
    }

    /**
     * Resolve using local-wins strategy.
     * Always prefers local data if it exists.
     */
    private fun <T> resolveLocalWins(conflict: Conflict<T>): ResolutionResult<T> {
        return when {
            conflict.local != null -> ResolutionResult.Resolved(conflict.local)
            conflict.server != null -> ResolutionResult.Resolved(conflict.server)
            else -> throw IllegalArgumentException("Both local and server data are null")
        }
    }

    /**
     * Resolve using server-wins strategy.
     * Always prefers server data if it exists.
     */
    private fun <T> resolveServerWins(conflict: Conflict<T>): ResolutionResult<T> {
        return when {
            conflict.server != null -> ResolutionResult.Resolved(conflict.server)
            conflict.local != null -> ResolutionResult.Resolved(conflict.local)
            else -> throw IllegalArgumentException("Both local and server data are null")
        }
    }

    /**
     * Resolve a list of conflicts using the specified strategy.
     *
     * @param conflicts List of conflicts to resolve
     * @param strategy The resolution strategy to use
     * @return Pair of resolved items and unresolved conflicts (if strategy is MANUAL)
     */
    fun <T> resolveAll(
        conflicts: List<Conflict<T>>,
        strategy: Strategy
    ): Pair<List<T>, List<Conflict<T>>> {
        val resolved = mutableListOf<T>()
        val unresolved = mutableListOf<Conflict<T>>()

        conflicts.forEach { conflict ->
            when (val result = resolve(conflict, strategy)) {
                is ResolutionResult.Resolved -> resolved.add(result.data)
                is ResolutionResult.ManualRequired -> unresolved.add(result.conflict)
            }
        }

        return Pair(resolved, unresolved)
    }
}
