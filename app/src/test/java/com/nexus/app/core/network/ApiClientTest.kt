package com.nexus.app.core.network

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ApiClientTest {
    private class FakeProvider(
        var token: String? = "T1",
        private val refreshed: String? = "T2"
    ) : AuthTokenProvider {
        var refreshCalls = 0
        var staleSeen: String? = null

        override suspend fun accessToken(): String? = token

        override suspend fun refreshAfterUnauthorized(staleToken: String): String? {
            refreshCalls++
            staleSeen = staleToken
            token = refreshed
            return refreshed
        }
    }

    private lateinit var server: MockWebServer
    private lateinit var client: ApiClient

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        client = ApiClient(server.url("/").toString(), "PUBKEY")
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun authPostSendsApikeyOnly() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"ok":true}"""))
        val body = client.authPost("/auth/v1/token?grant_type=password", JSONObject().put("email", "a@b.c"))
        assertEquals("""{"ok":true}""", body)
        val request = server.takeRequest()
        assertEquals("PUBKEY", request.getHeader("apikey"))
        assertNull(request.getHeader("Authorization"))
        assertEquals("POST", request.method)
        assertEquals("/auth/v1/token?grant_type=password", request.path)
    }

    @Test
    fun restSendsApikeyAndBearer() = runBlocking {
        client.tokenProvider = FakeProvider()
        server.enqueue(MockResponse().setBody("[]"))
        client.rest("/rest/v1/posts?select=*")
        val request = server.takeRequest()
        assertEquals("PUBKEY", request.getHeader("apikey"))
        assertEquals("Bearer T1", request.getHeader("Authorization"))
        assertEquals("GET", request.method)
        assertEquals(0L, request.bodySize)
    }

    @Test
    fun postWithoutBodySendsEmptyObjectAndExtraHeaders() = runBlocking {
        client.tokenProvider = FakeProvider()
        server.enqueue(MockResponse().setBody("{}"))
        client.rest("/rest/v1/rpc/x", "POST", null, mapOf("Prefer" to "return=representation"))
        val request = server.takeRequest()
        assertEquals("{}", request.body.readUtf8())
        assertEquals("return=representation", request.getHeader("Prefer"))
    }

    @Test
    fun rpcPostsToRpcPath() = runBlocking {
        client.tokenProvider = FakeProvider()
        server.enqueue(MockResponse().setBody("\"id1\""))
        val raw = client.rpc("create_thing", JSONObject().put("a", 1))
        assertEquals("id1", raw.extractId())
        val request = server.takeRequest()
        assertEquals("/rest/v1/rpc/create_thing", request.path)
        assertEquals("""{"a":1}""", request.body.readUtf8())
    }

    @Test
    fun unauthorizedTriggersRefreshAndSingleRetry() = runBlocking {
        val provider = FakeProvider()
        client.tokenProvider = provider
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"code":"PGRST301","message":"JWT expired"}"""))
        server.enqueue(MockResponse().setBody("""[{"id":"1"}]"""))
        val body = client.rest("/rest/v1/posts")
        assertEquals("""[{"id":"1"}]""", body)
        assertEquals(1, provider.refreshCalls)
        assertEquals("T1", provider.staleSeen)
        assertEquals(2, server.requestCount)
        assertEquals("Bearer T1", server.takeRequest().getHeader("Authorization"))
        assertEquals("Bearer T2", server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun secondUnauthorizedIsNotRetriedAgain() {
        val provider = FakeProvider()
        client.tokenProvider = provider
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setBody("never used"))
        val e = assertThrows(ApiException::class.java) { runBlocking { client.rest("/rest/v1/posts") } }
        assertEquals(ApiErrorKind.Unauthorized, e.kind)
        assertEquals(1, provider.refreshCalls)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun nullRefreshResultRethrowsOriginalError() {
        val provider = FakeProvider(refreshed = null)
        client.tokenProvider = provider
        server.enqueue(MockResponse().setResponseCode(401))
        val e = assertThrows(ApiException::class.java) { runBlocking { client.rest("/rest/v1/posts") } }
        assertEquals(ApiErrorKind.Unauthorized, e.kind)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun noTokenMeansUnauthorizedWithoutNetwork() {
        client.tokenProvider = FakeProvider(token = null)
        val e = assertThrows(ApiException::class.java) { runBlocking { client.rest("/rest/v1/posts") } }
        assertEquals(ApiErrorKind.Unauthorized, e.kind)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun missingProviderMeansUnauthorized() {
        val e = assertThrows(ApiException::class.java) { runBlocking { client.rest("/rest/v1/posts") } }
        assertEquals(ApiErrorKind.Unauthorized, e.kind)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun otherErrorsAreMappedAndNotRetried() {
        val provider = FakeProvider()
        client.tokenProvider = provider
        server.enqueue(MockResponse().setResponseCode(403).setBody("""{"message":"permission denied for table x"}"""))
        server.enqueue(MockResponse().setResponseCode(404))
        server.enqueue(MockResponse().setResponseCode(500).setBody("boom"))
        val forbidden = assertThrows(ApiException::class.java) { runBlocking { client.rest("/rest/v1/a") } }
        assertEquals(ApiErrorKind.Forbidden, forbidden.kind)
        assertEquals(ApiErrorMapper.MSG_FORBIDDEN, forbidden.userMessage)
        val notFound = assertThrows(ApiException::class.java) { runBlocking { client.rest("/rest/v1/b") } }
        assertEquals(ApiErrorKind.NotFound, notFound.kind)
        val serverError = assertThrows(ApiException::class.java) { runBlocking { client.rest("/rest/v1/c") } }
        assertEquals(ApiErrorKind.Server, serverError.kind)
        assertEquals(500, serverError.statusCode)
        assertEquals(0, provider.refreshCalls)
    }

    @Test
    fun connectionFailureIsNetworkError() {
        client.tokenProvider = FakeProvider()
        server.shutdown()
        val e = assertThrows(ApiException::class.java) { runBlocking { client.rest("/rest/v1/a") } }
        assertEquals(ApiErrorKind.Network, e.kind)
        assertTrue(e.isTransient)
    }

    @Test
    fun requestWithTokenNeverRefreshes() {
        val provider = FakeProvider()
        client.tokenProvider = provider
        server.enqueue(MockResponse().setResponseCode(401))
        assertThrows(ApiException::class.java) {
            runBlocking { client.requestWithToken("/auth/v1/user", "GET", null, "EXPLICIT") }
        }
        assertEquals(0, provider.refreshCalls)
        assertEquals("Bearer EXPLICIT", server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun absoluteStorageUrlHandlesRelativeAndAbsolute() {
        val base = client.baseUrl
        assertEquals("$base/storage/v1/object/sign/b/p?token=x", client.absoluteStorageUrl("/object/sign/b/p?token=x"))
        assertEquals("$base/storage/v1/object/sign/b/p?token=x", client.absoluteStorageUrl("object/sign/b/p?token=x"))
        assertEquals("https://cdn.example/x", client.absoluteStorageUrl("https://cdn.example/x"))
    }

    @Test
    fun signedUrlParsesResponse() = runBlocking {
        client.tokenProvider = FakeProvider()
        server.enqueue(MockResponse().setBody("""{"signedURL":"/object/sign/avatars/a%20b.png?token=abc"}"""))
        val url = client.signedUrl("avatars", "a b.png")
        assertTrue(url.endsWith("/storage/v1/object/sign/avatars/a%20b.png?token=abc"))
        assertEquals("/storage/v1/object/sign/avatars/a%20b.png", server.takeRequest().path)
    }
}
