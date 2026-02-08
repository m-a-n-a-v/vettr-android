package com.vettr.android.core.sync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ConflictResolverTest {

    private lateinit var resolver: ConflictResolver

    @Before
    fun setup() {
        resolver = ConflictResolver()
    }

    // ========== LAST_WRITE_WINS Strategy Tests ==========

    @Test
    fun `lastWriteWins chooses local when local timestamp is newer`() {
        val conflict = ConflictResolver.Conflict(
            local = "local-data",
            server = "server-data",
            localTimestamp = 2000L,
            serverTimestamp = 1000L
        )

        val result = resolver.resolve(conflict, ConflictResolver.Strategy.LAST_WRITE_WINS)

        assertTrue(result is ConflictResolver.ResolutionResult.Resolved)
        assertEquals("local-data", (result as ConflictResolver.ResolutionResult.Resolved).data)
    }

    @Test
    fun `lastWriteWins chooses server when server timestamp is newer`() {
        val conflict = ConflictResolver.Conflict(
            local = "local-data",
            server = "server-data",
            localTimestamp = 1000L,
            serverTimestamp = 2000L
        )

        val result = resolver.resolve(conflict, ConflictResolver.Strategy.LAST_WRITE_WINS)

        assertTrue(result is ConflictResolver.ResolutionResult.Resolved)
        assertEquals("server-data", (result as ConflictResolver.ResolutionResult.Resolved).data)
    }

    @Test
    fun `lastWriteWins chooses local when timestamps are equal`() {
        val conflict = ConflictResolver.Conflict(
            local = "local-data",
            server = "server-data",
            localTimestamp = 1000L,
            serverTimestamp = 1000L
        )

        val result = resolver.resolve(conflict, ConflictResolver.Strategy.LAST_WRITE_WINS)

        assertTrue(result is ConflictResolver.ResolutionResult.Resolved)
        assertEquals("local-data", (result as ConflictResolver.ResolutionResult.Resolved).data)
    }

    @Test
    fun `lastWriteWins chooses server when local is null`() {
        val conflict = ConflictResolver.Conflict(
            local = null,
            server = "server-data",
            localTimestamp = 1000L,
            serverTimestamp = 1000L
        )

        val result = resolver.resolve(conflict, ConflictResolver.Strategy.LAST_WRITE_WINS)

        assertTrue(result is ConflictResolver.ResolutionResult.Resolved)
        assertEquals("server-data", (result as ConflictResolver.ResolutionResult.Resolved).data)
    }

    @Test
    fun `lastWriteWins chooses local when server is null`() {
        val conflict = ConflictResolver.Conflict(
            local = "local-data",
            server = null,
            localTimestamp = 1000L,
            serverTimestamp = 1000L
        )

        val result = resolver.resolve(conflict, ConflictResolver.Strategy.LAST_WRITE_WINS)

        assertTrue(result is ConflictResolver.ResolutionResult.Resolved)
        assertEquals("local-data", (result as ConflictResolver.ResolutionResult.Resolved).data)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `lastWriteWins throws when both local and server are null`() {
        val conflict = ConflictResolver.Conflict<String>(
            local = null,
            server = null,
            localTimestamp = 1000L,
            serverTimestamp = 1000L
        )

        resolver.resolve(conflict, ConflictResolver.Strategy.LAST_WRITE_WINS)
    }

    // ========== LOCAL_WINS Strategy Tests ==========

    @Test
    fun `localWins always chooses local when both exist`() {
        val conflict = ConflictResolver.Conflict(
            local = "local-data",
            server = "server-data",
            localTimestamp = 1000L,
            serverTimestamp = 2000L
        )

        val result = resolver.resolve(conflict, ConflictResolver.Strategy.LOCAL_WINS)

        assertTrue(result is ConflictResolver.ResolutionResult.Resolved)
        assertEquals("local-data", (result as ConflictResolver.ResolutionResult.Resolved).data)
    }

    @Test
    fun `localWins chooses server when local is null`() {
        val conflict = ConflictResolver.Conflict(
            local = null,
            server = "server-data",
            localTimestamp = 1000L,
            serverTimestamp = 1000L
        )

        val result = resolver.resolve(conflict, ConflictResolver.Strategy.LOCAL_WINS)

        assertTrue(result is ConflictResolver.ResolutionResult.Resolved)
        assertEquals("server-data", (result as ConflictResolver.ResolutionResult.Resolved).data)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `localWins throws when both local and server are null`() {
        val conflict = ConflictResolver.Conflict<String>(
            local = null,
            server = null,
            localTimestamp = 1000L,
            serverTimestamp = 1000L
        )

        resolver.resolve(conflict, ConflictResolver.Strategy.LOCAL_WINS)
    }

    // ========== SERVER_WINS Strategy Tests ==========

    @Test
    fun `serverWins always chooses server when both exist`() {
        val conflict = ConflictResolver.Conflict(
            local = "local-data",
            server = "server-data",
            localTimestamp = 2000L,
            serverTimestamp = 1000L
        )

        val result = resolver.resolve(conflict, ConflictResolver.Strategy.SERVER_WINS)

        assertTrue(result is ConflictResolver.ResolutionResult.Resolved)
        assertEquals("server-data", (result as ConflictResolver.ResolutionResult.Resolved).data)
    }

    @Test
    fun `serverWins chooses local when server is null`() {
        val conflict = ConflictResolver.Conflict(
            local = "local-data",
            server = null,
            localTimestamp = 1000L,
            serverTimestamp = 1000L
        )

        val result = resolver.resolve(conflict, ConflictResolver.Strategy.SERVER_WINS)

        assertTrue(result is ConflictResolver.ResolutionResult.Resolved)
        assertEquals("local-data", (result as ConflictResolver.ResolutionResult.Resolved).data)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `serverWins throws when both local and server are null`() {
        val conflict = ConflictResolver.Conflict<String>(
            local = null,
            server = null,
            localTimestamp = 1000L,
            serverTimestamp = 1000L
        )

        resolver.resolve(conflict, ConflictResolver.Strategy.SERVER_WINS)
    }

    // ========== MANUAL Strategy Tests ==========

    @Test
    fun `manual returns ManualRequired result`() {
        val conflict = ConflictResolver.Conflict(
            local = "local-data",
            server = "server-data",
            localTimestamp = 1000L,
            serverTimestamp = 1000L
        )

        val result = resolver.resolve(conflict, ConflictResolver.Strategy.MANUAL)

        assertTrue(result is ConflictResolver.ResolutionResult.ManualRequired)
        assertEquals(conflict, (result as ConflictResolver.ResolutionResult.ManualRequired).conflict)
    }

    @Test
    fun `manual works with null values`() {
        val conflict = ConflictResolver.Conflict<String>(
            local = null,
            server = "server-data",
            localTimestamp = 1000L,
            serverTimestamp = 1000L
        )

        val result = resolver.resolve(conflict, ConflictResolver.Strategy.MANUAL)

        assertTrue(result is ConflictResolver.ResolutionResult.ManualRequired)
    }

    // ========== resolveAll Tests ==========

    @Test
    fun `resolveAll with lastWriteWins resolves all conflicts`() {
        val conflicts = listOf(
            ConflictResolver.Conflict(
                local = "local1",
                server = "server1",
                localTimestamp = 2000L,
                serverTimestamp = 1000L
            ),
            ConflictResolver.Conflict(
                local = "local2",
                server = "server2",
                localTimestamp = 1000L,
                serverTimestamp = 2000L
            ),
            ConflictResolver.Conflict(
                local = "local3",
                server = null,
                localTimestamp = 1000L,
                serverTimestamp = 1000L
            )
        )

        val (resolved, unresolved) = resolver.resolveAll(
            conflicts,
            ConflictResolver.Strategy.LAST_WRITE_WINS
        )

        assertEquals(3, resolved.size)
        assertEquals(0, unresolved.size)
        assertEquals("local1", resolved[0])
        assertEquals("server2", resolved[1])
        assertEquals("local3", resolved[2])
    }

    @Test
    fun `resolveAll with manual returns all as unresolved`() {
        val conflicts = listOf(
            ConflictResolver.Conflict(
                local = "local1",
                server = "server1",
                localTimestamp = 2000L,
                serverTimestamp = 1000L
            ),
            ConflictResolver.Conflict(
                local = "local2",
                server = "server2",
                localTimestamp = 1000L,
                serverTimestamp = 2000L
            )
        )

        val (resolved, unresolved) = resolver.resolveAll(
            conflicts,
            ConflictResolver.Strategy.MANUAL
        )

        assertEquals(0, resolved.size)
        assertEquals(2, unresolved.size)
    }

    @Test
    fun `resolveAll with localWins resolves all to local values`() {
        val conflicts = listOf(
            ConflictResolver.Conflict(
                local = "local1",
                server = "server1",
                localTimestamp = 1000L,
                serverTimestamp = 2000L
            ),
            ConflictResolver.Conflict(
                local = "local2",
                server = "server2",
                localTimestamp = 1000L,
                serverTimestamp = 2000L
            )
        )

        val (resolved, unresolved) = resolver.resolveAll(
            conflicts,
            ConflictResolver.Strategy.LOCAL_WINS
        )

        assertEquals(2, resolved.size)
        assertEquals(0, unresolved.size)
        assertEquals("local1", resolved[0])
        assertEquals("local2", resolved[1])
    }

    @Test
    fun `resolveAll with serverWins resolves all to server values`() {
        val conflicts = listOf(
            ConflictResolver.Conflict(
                local = "local1",
                server = "server1",
                localTimestamp = 2000L,
                serverTimestamp = 1000L
            ),
            ConflictResolver.Conflict(
                local = "local2",
                server = "server2",
                localTimestamp = 2000L,
                serverTimestamp = 1000L
            )
        )

        val (resolved, unresolved) = resolver.resolveAll(
            conflicts,
            ConflictResolver.Strategy.SERVER_WINS
        )

        assertEquals(2, resolved.size)
        assertEquals(0, unresolved.size)
        assertEquals("server1", resolved[0])
        assertEquals("server2", resolved[1])
    }

    @Test
    fun `resolveAll handles empty list`() {
        val conflicts = emptyList<ConflictResolver.Conflict<String>>()

        val (resolved, unresolved) = resolver.resolveAll(
            conflicts,
            ConflictResolver.Strategy.LAST_WRITE_WINS
        )

        assertEquals(0, resolved.size)
        assertEquals(0, unresolved.size)
    }

    // ========== Complex Data Type Tests ==========

    data class TestEntity(val id: String, val value: String)

    @Test
    fun `resolver works with complex data types`() {
        val localEntity = TestEntity("1", "local")
        val serverEntity = TestEntity("1", "server")

        val conflict = ConflictResolver.Conflict(
            local = localEntity,
            server = serverEntity,
            localTimestamp = 2000L,
            serverTimestamp = 1000L
        )

        val result = resolver.resolve(conflict, ConflictResolver.Strategy.LAST_WRITE_WINS)

        assertTrue(result is ConflictResolver.ResolutionResult.Resolved)
        assertEquals(localEntity, (result as ConflictResolver.ResolutionResult.Resolved).data)
    }
}
