package com.nexus.app.core.session

import com.nexus.app.core.network.ApiErrorKind
import com.nexus.app.core.network.ApiException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class LoginIdentifierTest {
    @Test
    fun emailIsReturnedUnchanged() {
        assertEquals("Ali@Example.com", normalizeLoginIdentifier("  Ali@Example.com "))
    }

    @Test
    fun digitsBecomeStudentEmail() {
        assertEquals("2023123@college.edu", normalizeLoginIdentifier("2023123"))
    }

    @Test
    fun arabicIndicDigitsAreConverted() {
        assertEquals("2023123@college.edu", normalizeLoginIdentifier("٢٠٢٣١٢٣"))
    }

    @Test
    fun otherTextBecomesSubAdminEmail() {
        assertEquals("ali@subadmin.edu", normalizeLoginIdentifier("Ali"))
        assertEquals("ali@subadmin.edu", normalizeLoginIdentifier("Sub_Ali"))
        assertEquals("aliahmed@subadmin.edu", normalizeLoginIdentifier(" ali ahmed "))
    }

    @Test
    fun emptyInputIsValidationError() {
        val e = assertThrows(ApiException::class.java) { normalizeLoginIdentifier("   ") }
        assertEquals(ApiErrorKind.Validation, e.kind)
    }

    @Test
    fun bareSubPrefixIsRejected() {
        val e = assertThrows(ApiException::class.java) { normalizeLoginIdentifier("sub_") }
        assertEquals(ApiErrorKind.Validation, e.kind)
    }

    @Test
    fun subAdminEmailStripsPrefixAndLowercases() {
        assertEquals("bob@subadmin.edu", subAdminEmailFor("Sub_Bob"))
        assertEquals("sub_bob", subAdminUniversityNumberFor("Sub_Bob"))
    }

    @Test
    fun subAdminEmailRejectsDigitsAndBlank() {
        assertThrows(ApiException::class.java) { subAdminEmailFor("12345") }
        assertThrows(ApiException::class.java) { subAdminEmailFor("sub_") }
        assertThrows(ApiException::class.java) { subAdminEmailFor("a@b.com") }
    }

    @Test
    fun toAsciiDigitsLeavesOtherCharacters() {
        assertEquals("a12-b", toAsciiDigits("a١٢-b"))
    }
}
