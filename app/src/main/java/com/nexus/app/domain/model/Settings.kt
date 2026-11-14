package com.nexus.app.domain.model

/** Device-local preferences (DataStore). */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = false,
    val language: AppLanguage = AppLanguage.AR
)
