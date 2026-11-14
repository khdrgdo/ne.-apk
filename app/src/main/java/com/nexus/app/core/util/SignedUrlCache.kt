package com.nexus.app.core.util

/**
 * In-memory LRU cache of signed storage URLs with a time-to-live (default 50 minutes, signed URLs live 60 minutes),
 * so every image/avatar path is signed only once per session window.
 */
class SignedUrlCache(
    private val ttlMillis: Long = DEFAULT_TTL_MILLIS,
    private val maxEntries: Int = 512,
    private val clock: Clock = DefaultClock
) {
    private class Entry(val url: String, val expiresAtMillis: Long)

    private val map = object : LinkedHashMap<String, Entry>(64, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Entry>?): Boolean = size > maxEntries
    }

    /** Cache key for a storage object. */
    fun key(bucket: String, path: String): String = "$bucket/$path"

    @Synchronized
    fun get(key: String): String? {
        val entry = map[key] ?: return null
        if (entry.expiresAtMillis <= clock.nowMillis()) {
            map.remove(key)
            return null
        }
        return entry.url
    }

    @Synchronized
    fun put(key: String, url: String) {
        map[key] = Entry(url, clock.nowMillis() + ttlMillis)
    }

    @Synchronized
    fun invalidate(key: String) {
        map.remove(key)
    }

    @Synchronized
    fun clear() {
        map.clear()
    }

    /** Returns the cached URL or calls [sign]; a non-null result is cached. Failures are not cached. */
    suspend fun getOrSign(bucket: String, path: String, sign: suspend () -> String?): String? {
        val key = key(bucket, path)
        get(key)?.let { return it }
        val signed = sign() ?: return null
        put(key, signed)
        return signed
    }

    companion object {
        const val DEFAULT_TTL_MILLIS: Long = 50L * 60L * 1000L
    }
}
