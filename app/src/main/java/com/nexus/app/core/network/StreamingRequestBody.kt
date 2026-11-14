package com.nexus.app.core.network

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.IOException
import java.io.InputStream

/**
 * Streams an upload from an [InputStream] in 8 KiB chunks without loading the file in memory.
 * [opener] is called on every `writeTo`, so the body can be re-sent (for example after a 401 refresh).
 * [length] of -1 means unknown (chunked). When [maxBytes] > 0 and exceeded, [UploadLimitException] with
 * [tooLargeMessage] is thrown.
 */
class StreamingRequestBody(
    private val mediaType: MediaType?,
    private val length: Long,
    private val maxBytes: Long,
    private val tooLargeMessage: String,
    private val opener: () -> InputStream?
) : RequestBody() {
    override fun contentType(): MediaType? = mediaType

    override fun contentLength(): Long = length

    override fun writeTo(sink: BufferedSink) {
        val input = opener() ?: throw IOException("unreadable upload source")
        input.use { stream ->
            val buffer = ByteArray(BUFFER_SIZE)
            var total = 0L
            while (true) {
                val read = stream.read(buffer)
                if (read < 0) break
                total += read
                if (maxBytes > 0L && total > maxBytes) throw UploadLimitException(tooLargeMessage)
                sink.write(buffer, 0, read)
            }
        }
    }

    private companion object {
        const val BUFFER_SIZE = 8192
    }
}
