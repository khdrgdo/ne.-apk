package com.nexus.app.core.network

import java.net.URLEncoder

/** Percent-encodes [value] for use in a query value (space becomes %20, never '+'). */
fun urlEncode(value: String): String = URLEncoder.encode(value, "UTF-8").replace("+", "%20")

/** Percent-encodes each segment of a storage object path, keeping '/' separators. */
fun encodeStoragePath(path: String): String =
    path.trim('/').split('/').joinToString("/") { urlEncode(it) }
