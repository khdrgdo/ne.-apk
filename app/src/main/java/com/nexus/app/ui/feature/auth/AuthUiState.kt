package com.nexus.app.ui.feature.auth

import androidx.annotation.StringRes

/**
 * Immutable state of the sign-in screen. Field errors are string resource ids (inline validation);
 * [error] is a server/network message that is already Arabic; [notice] comes from a rejected login link.
 */
data class AuthUiState(
    val identifier: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    @StringRes val identifierError: Int? = null,
    @StringRes val passwordError: Int? = null,
    val error: String? = null,
    val notice: String? = null,
    val busy: Boolean = false
)
