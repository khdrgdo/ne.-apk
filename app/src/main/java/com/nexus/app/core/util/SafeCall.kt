package com.nexus.app.core.util

import com.nexus.app.core.network.ApiException
import com.nexus.app.core.network.toApiException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Runs [block] and converts any failure into `Result.failure(ApiException)`.
 * Coroutine cancellation is always rethrown. The block may call suspend functions (it is inlined).
 */
inline fun <T> safeCall(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e.toApiException())
    }

/** Arabic, user-presentable message for any throwable. */
val Throwable.displayMessage: String
    get() = toApiException().userMessage

/** Arabic message of a failed result, or null when the result is a success. */
fun <T> Result<T>.errorMessageOrNull(): String? = exceptionOrNull()?.displayMessage

/** The [ApiException] of a failed result, or null when the result is a success. */
fun <T> Result<T>.apiExceptionOrNull(): ApiException? = exceptionOrNull()?.toApiException()
