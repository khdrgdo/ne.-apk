package com.nexus.app.core.network

import java.io.IOException
import java.net.SocketTimeoutException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiErrorMappingTest {
    @Test
    fun statusKinds() {
        assertEquals(ApiErrorKind.Unauthorized, ApiErrorMapper.fromResponse(401, "").kind)
        assertEquals(ApiErrorKind.Forbidden, ApiErrorMapper.fromResponse(403, "").kind)
        assertEquals(ApiErrorKind.NotFound, ApiErrorMapper.fromResponse(404, "").kind)
        assertEquals(ApiErrorKind.Validation, ApiErrorMapper.fromResponse(400, "{}").kind)
        assertEquals(ApiErrorKind.Validation, ApiErrorMapper.fromResponse(409, "{}").kind)
        assertEquals(ApiErrorKind.Server, ApiErrorMapper.fromResponse(500, "<html>").kind)
        assertEquals(ApiErrorKind.Server, ApiErrorMapper.fromResponse(429, "{}").kind)
    }

    @Test
    fun statusCodeIsKept() {
        assertEquals(503, ApiErrorMapper.fromResponse(503, "").statusCode)
    }

    @Test
    fun invalidCredentialsAreTranslated() {
        val e = ApiErrorMapper.fromResponse(400, """{"error_code":"invalid_credentials","msg":"Invalid login credentials"}""")
        assertEquals(ApiErrorMapper.MSG_INVALID_CREDENTIALS, e.userMessage)
        assertEquals("Invalid login credentials", e.serverMessage)
        assertEquals("invalid_credentials", e.errorCode)
    }

    @Test
    fun invalidRefreshTokenMapsToSessionExpired() {
        val e = ApiErrorMapper.fromResponse(
            400,
            """{"error":"invalid_grant","error_description":"Invalid Refresh Token: Already Used"}"""
        )
        assertEquals(ApiErrorMapper.MSG_SESSION_EXPIRED, e.userMessage)
    }

    @Test
    fun arabicServerMessagePassesThrough() {
        val e = ApiErrorMapper.fromResponse(400, """{"message":"الرقم الجامعي مستخدم"}""")
        assertEquals("الرقم الجامعي مستخدم", e.userMessage)
    }

    @Test
    fun rlsCodeIsForbiddenEvenOnBadRequestStatus() {
        val e = ApiErrorMapper.fromResponse(400, """{"code":"42501","message":"new row violates row-level security policy"}""")
        assertEquals(ApiErrorKind.Forbidden, e.kind)
        assertEquals(ApiErrorMapper.MSG_FORBIDDEN, e.userMessage)
    }

    @Test
    fun jwtExpiredCodeIsUnauthorized() {
        val e = ApiErrorMapper.fromResponse(400, """{"code":"PGRST301","message":"JWT expired"}""")
        assertEquals(ApiErrorKind.Unauthorized, e.kind)
    }

    @Test
    fun fallbackMessages() {
        assertEquals(ApiErrorMapper.MSG_SERVER, ApiErrorMapper.fromResponse(500, "oops").userMessage)
        assertEquals(ApiErrorMapper.MSG_RATE_LIMIT, ApiErrorMapper.fromResponse(429, "{}").userMessage)
        assertEquals(ApiErrorMapper.MSG_NOT_FOUND, ApiErrorMapper.fromResponse(404, "").userMessage)
        assertEquals(ApiErrorMapper.MSG_SESSION_EXPIRED, ApiErrorMapper.fromResponse(401, "").userMessage)
    }

    @Test
    fun translateReturnsNullWhenUnknown() {
        assertNull(ApiErrorMapper.translate("something odd", null))
    }

    @Test
    fun throwablesAreMapped() {
        assertEquals(ApiErrorKind.Network, IOException("x").toApiException().kind)
        val timeout = SocketTimeoutException("t").toApiException()
        assertEquals(ApiErrorKind.Network, timeout.kind)
        assertEquals(ApiErrorMapper.MSG_TIMEOUT, timeout.userMessage)
        val iae = IllegalArgumentException("حقل مطلوب").toApiException()
        assertEquals(ApiErrorKind.Validation, iae.kind)
        assertEquals("حقل مطلوب", iae.userMessage)
        assertEquals(ApiErrorMapper.MSG_UNKNOWN, RuntimeException("English").toApiException().userMessage)
    }

    @Test
    fun uploadLimitIsValidation() {
        val e = UploadLimitException("الملف كبير").toApiException()
        assertEquals(ApiErrorKind.Validation, e.kind)
        assertEquals("الملف كبير", e.userMessage)
    }

    @Test
    fun apiExceptionIsReturnedAsIs() {
        val original = ApiException(ApiErrorKind.NotFound, "x")
        assertTrue(original === original.toApiException())
    }

    @Test
    fun transientFlag() {
        assertTrue(ApiException(ApiErrorKind.Network, "x").isTransient)
        assertTrue(ApiException(ApiErrorKind.Server, "x").isTransient)
        assertFalse(ApiException(ApiErrorKind.Validation, "x").isTransient)
    }
}
