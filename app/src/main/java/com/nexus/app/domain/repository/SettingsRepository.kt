package com.nexus.app.domain.repository

import com.nexus.app.domain.model.AppLanguage
import com.nexus.app.domain.model.AppSettings
import com.nexus.app.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/**
 * Device-local preferences (DataStore, no network). Setters are suspend and throw only on storage I/O failure.
 */
interface SettingsRepository {
    /** DataStore: theme mode, dynamic color and language. */
    val settings: Flow<AppSettings>

    /** DataStore: recent search queries, newest first. */
    val recentSearches: Flow<List<String>>

    /** DataStore key theme_mode. */
    suspend fun setThemeMode(mode: ThemeMode)

    /** DataStore key dynamic_color. */
    suspend fun setDynamicColor(enabled: Boolean)

    /** DataStore key language. */
    suspend fun setLanguage(language: AppLanguage)

    /** DataStore key recent_searches (deduplicated, max 10). */
    suspend fun addRecentSearch(query: String)

    /** DataStore key recent_searches. */
    suspend fun removeRecentSearch(query: String)

    /** DataStore key recent_searches cleared. */
    suspend fun clearRecentSearches()
}
