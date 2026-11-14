package com.nexus.app.core.session

import com.nexus.app.core.network.ApiClient
import com.nexus.app.core.network.ApiErrorKind
import com.nexus.app.core.network.ApiException
import com.nexus.app.core.util.Clock
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SessionManagerTest {
    private class MemoryStorage : SessionStorage {
        var value: String? = null
        override fun save(value: String) { this.value = value }
        override fun read(): String? = value
        override fun clear() { value = null }
    }

    private class MemoryFlowStore : AuthFlowStore {
        var flow: AuthFlow? = null
        override fun save(flow: AuthFlow) { this.flow = flow }
        override fun read(): AuthFlow? = flow
        override fun clear() { flow = null }
    }

    private var nowMillis = 1_700_000_000_000L
    private val nowSeconds: Long get() = nowMillis / 1000L

    private lateinit var server: MockWebServer
    private lateinit var storage: MemoryStorage
    private lateinit var flows: MemoryFlowStore
    private lateinit var api: ApiClient
    private lateinit var manager: SessionManager

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        storage = MemoryStorage()
        flows = MemoryFlowStore()
        api = ApiClient(server.url("/").toString(), "PUBKEY")
        manager = SessionManager(api, storage, flows, Clock { nowMillis })
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun tokenBody(access: String, refresh: String, userId: String = "u1") =
        """{"access_token":"$access","refresh_token":"$refresh","expires_in":3600,"user":{"id":"$userId"}}"""

    private fun seed(access: String = "A1", refresh: String = "R1", expiresAt: Long = nowSeconds + 3600, user: String = "u1") {
        storage.value = UserSession(access, refresh, expiresAt, user).toJson()
    }

    @Test
    fun signInStoresSessionAndPublishesActive() = runBlocking {
        server.enqueue(MockResponse().setBody(tokenBody("A1", "R1")))
        val id = manager.signIn("2023123", "secret")
        assertEquals("u1", id)
        assertEquals(SessionState.Active("u1"), manager.state.value)
        assertEquals("u1", manager.currentUserId)
        assertEquals("A1", UserSession.fromJson(storage.value!!)?.accessToken)
        val request = server.takeRequest()
        assertEquals("PUBKEY", request.getHeader("apikey"))
        assertNull(request.getHeader("Authorization"))
        assertTrue(request.body.readUtf8().contains("2023123@college.edu"))
    }

    @Test
    fun signInFailureStoresNothing() {
        server.enqueue(MockResponse().setResponseCode(400).setBody("""{"msg":"Invalid login credentials"}"""))
        val e = assertThrows(ApiException::class.java) { runBlocking { manager.signIn("2023123", "bad") } }
        assertEquals(ApiErrorKind.Validation, e.kind)
        assertNull(storage.value)
        assertEquals(SessionState.Initializing, manager.state.value)
    }

    @Test
    fun restoreWithoutStoredSessionIsSignedOut() = runBlocking {
        manager.restore()
        assertEquals(SessionState.SignedOut, manager.state.value)
    }

    @Test
    fun restoreWithValidTokenMakesNoRequest() = runBlocking {
        seed()
        manager.restore()
        assertEquals(SessionState.Active("u1"), manager.state.value)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun tokenOutsideWindowIsReturnedWithoutRefresh() = runBlocking {
        seed(expiresAt = nowSeconds + 61)
        manager.restore()
        assertEquals("A1", manager.accessToken())
        assertEquals(0, server.requestCount)
    }

    @Test
    fun refreshUsesRefreshGrantWithoutBearerAndKeepsUserId() = runBlocking {
        seed(expiresAt = nowSeconds + 59)
        server.enqueue(MockResponse().setBody("""{"access_token":"A2","refresh_token":"R2","expires_in":3600}"""))
        manager.restore()
        assertEquals("A2", manager.accessToken())
        assertEquals("u1", manager.currentUserId)
        val request = server.takeRequest()
        assertEquals("/auth/v1/token?grant_type=refresh_token", request.path)
        assertNull(request.getHeader("Authorization"))
        assertTrue(request.body.readUtf8().contains("R1"))
        assertEquals("R2", UserSession.fromJson(storage.value!!)?.refreshToken)
    }

    @Test
    fun concurrentCallersShareOneRefresh() = runBlocking {
        seed()
        manager.restore()
        nowMillis += 3_550_000L // token now expires in 50 s, inside the 60 s window
        server.enqueue(MockResponse().setBody(tokenBody("A2", "R2")))
        val tokens = coroutineScope {
            listOf(async { manager.accessToken() }, async { manager.accessToken() }, async { manager.accessToken() }).awaitAll()
        }
        assertEquals(listOf("A2", "A2", "A2"), tokens)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun unauthorizedRefreshForcesNewTokenEvenInsideValidity() = runBlocking {
        seed(access = "A1")
        manager.restore()
        server.enqueue(MockResponse().setBody(tokenBody("A2", "R2")))
        assertEquals("A2", manager.refreshAfterUnauthorized("A1"))
        assertEquals(1, server.requestCount)
    }

    @Test
    fun staleTokenAlreadyReplacedIsNotRefreshedAgain() = runBlocking {
        seed(access = "A9")
        manager.restore()
        assertEquals("A9", manager.refreshAfterUnauthorized("OLD"))
        assertEquals(0, server.requestCount)
    }

    @Test
    fun rejectedRefreshTokenSignsOutLocally() = runBlocking {
        seed(expiresAt = nowSeconds + 10)
        server.enqueue(MockResponse().setResponseCode(400).setBody("""{"error":"invalid_grant","error_description":"Invalid Refresh Token"}"""))
        manager.restore()
        assertEquals(SessionState.SignedOut, manager.state.value)
        assertNull(storage.value)
        assertNull(manager.currentUserId)
    }

    @Test
    fun refreshRejectionDuringRequestThrowsUnauthorized() {
        seed()
        server.enqueue(MockResponse().setResponseCode(400).setBody("""{"error":"invalid_grant","error_description":"Invalid Refresh Token"}"""))
        val e = assertThrows(ApiException::class.java) {
            runBlocking {
                manager.restore()
                manager.refreshAfterUnauthorized("A1")
            }
        }
        assertEquals(ApiErrorKind.Unauthorized, e.kind)
        assertEquals(SessionState.SignedOut, manager.state.value)
    }

    @Test
    fun serverErrorDuringRestoreRefreshKeepsSession() = runBlocking {
        seed(expiresAt = nowSeconds + 10)
        server.enqueue(MockResponse().setResponseCode(503))
        manager.restore()
        assertEquals(SessionState.Active("u1"), manager.state.value)
        assertTrue(storage.value != null)
    }

    @Test
    fun signOutUsesLocalScopeAndClearsEvenWhenServerFails() = runBlocking {
        seed()
        manager.restore()
        server.enqueue(MockResponse().setResponseCode(500))
        manager.signOut()
        assertEquals(SessionState.SignedOut, manager.state.value)
        assertNull(storage.value)
        assertNull(manager.currentUserId)
        val request = server.takeRequest()
        assertEquals("/auth/v1/logout?scope=local", request.path)
        assertEquals("Bearer A1", request.getHeader("Authorization"))
        assertEquals("POST", request.method)
    }

    @Test
    fun signOutWithoutSessionMakesNoRequest() = runBlocking {
        manager.signOut()
        assertEquals(SessionState.SignedOut, manager.state.value)
        assertEquals(0, server.requestCount)
    }

    // ------------------------------------------------------------ deep links

    private fun fragment(flow: AuthFlow, extra: String = "") =
        "access_token=AT&refresh_token=RT&expires_in=3600&state=${flow.state}&nonce=${flow.nonce}$extra"

    @Test
    fun foreignLinksAreNotHandled() = runBlocking {
        assertEquals(DeepLinkResult.NotHandled, manager.acceptDeepLink("https", "example.com", "x=1"))
        assertEquals(DeepLinkResult.NotHandled, manager.acceptDeepLink("com.nexus.app", "other", "x=1"))
    }

    @Test
    fun linkWithoutPendingFlowIsRejected() = runBlocking {
        val result = manager.acceptDeepLink("com.nexus.app", "login-callback", "access_token=AT&refresh_token=RT&state=abc")
        assertTrue(result is DeepLinkResult.Rejected)
        assertEquals(0, server.requestCount)
        assertNull(storage.value)
    }

    @Test
    fun wrongStateIsRejectedAndFlowIsConsumed() = runBlocking {
        val flow = manager.beginWebAuth()
        val bad = manager.acceptDeepLink("com.nexus.app", "login-callback", "access_token=AT&refresh_token=RT&state=nope")
        assertTrue(bad is DeepLinkResult.Rejected)
        assertNull(flows.flow)
        val again = manager.acceptDeepLink("com.nexus.app", "login-callback", fragment(flow))
        assertTrue(again is DeepLinkResult.Rejected)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun errorParametersAreRejected() = runBlocking {
        val flow = manager.beginWebAuth()
        val result = manager.acceptDeepLink("com.nexus.app", "login-callback", fragment(flow, "&error=access_denied"))
        assertTrue(result is DeepLinkResult.Rejected)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun wrongNonceIsRejected() = runBlocking {
        val flow = manager.beginWebAuth()
        val result = manager.acceptDeepLink(
            "com.nexus.app", "login-callback",
            "access_token=AT&refresh_token=RT&state=${flow.state}&nonce=zzz"
        )
        assertTrue(result is DeepLinkResult.Rejected)
    }

    @Test
    fun expiredFlowIsRejected() = runBlocking {
        val flow = manager.beginWebAuth()
        nowMillis += SessionManager.FLOW_MAX_AGE_MILLIS + 1
        val result = manager.acceptDeepLink("com.nexus.app", "login-callback", fragment(flow))
        assertTrue(result is DeepLinkResult.Rejected)
    }

    @Test
    fun validLinkIsVerifiedWithServerBeforePersisting() = runBlocking {
        val flow = manager.beginWebAuth()
        server.enqueue(MockResponse().setBody("""{"id":"u7","email":"a@b.c"}"""))
        val result = manager.acceptDeepLink("com.nexus.app", "login-callback", fragment(flow))
        assertEquals(DeepLinkResult.Accepted(recovery = false), result)
        assertEquals(SessionState.Active("u7"), manager.state.value)
        val request = server.takeRequest()
        assertEquals("/auth/v1/user", request.path)
        assertEquals("Bearer AT", request.getHeader("Authorization"))
        assertEquals("u7", UserSession.fromJson(storage.value!!)?.userId)
        assertNull(flows.flow)
    }

    @Test
    fun recoveryLinkIsFlagged() = runBlocking {
        val flow = manager.beginWebAuth()
        server.enqueue(MockResponse().setBody("""{"id":"u7"}"""))
        val result = manager.acceptDeepLink("com.nexus.app", "login-callback", fragment(flow, "&type=recovery"))
        assertEquals(DeepLinkResult.Accepted(recovery = true), result)
    }

    @Test
    fun tokenRejectedByServerIsNotPersisted() = runBlocking {
        val flow = manager.beginWebAuth()
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"message":"invalid JWT"}"""))
        val result = manager.acceptDeepLink("com.nexus.app", "login-callback", fragment(flow))
        assertTrue(result is DeepLinkResult.Rejected)
        assertNull(storage.value)
        assertEquals(SessionState.Initializing, manager.state.value)
    }

    @Test
    fun parseFragmentDecodesValues() {
        val map = SessionManager.parseFragment("a=1&b=x%20y&c&d=%zz")
        assertEquals("1", map["a"])
        assertEquals("x y", map["b"])
        assertEquals("", map["c"])
        assertNull(map["d"])
    }
}
