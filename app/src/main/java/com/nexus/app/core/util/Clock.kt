package com.nexus.app.core.util

/** Source of wall-clock time; replaceable in tests. */
fun interface Clock {
    fun nowMillis(): Long
}

object DefaultClock : Clock {
    override fun nowMillis(): Long = System.currentTimeMillis()
}
