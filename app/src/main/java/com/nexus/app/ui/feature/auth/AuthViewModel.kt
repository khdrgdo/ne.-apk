package com.nexus.app.ui.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.app.R
import com.nexus.app.core.network.ApiErrorMapper
import com.nexus.app.core.network.ApiException
import com.nexus.app.core.session.SessionManager
import com.nexus.app.core.session.normalizeLoginIdentifier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val sessionManager: SessionManager,
    private val notices: MutableStateFlow<String?>
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            notices.collect { message ->
                _state.update { it.copy(notice = message) }
            }
        }
    }

    fun onIdentifierChange(value: String) {
        _state.update { it.copy(identifier = value, identifierError = null, error = null) }
    }

    fun onPasswordChange(value: String) {
        _state.update { it.copy(password = value, passwordError = null, error = null) }
    }

    fun togglePasswordVisible() {
        _state.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun dismissNotice() {
        notices.value = null
    }

    fun submit() {
        val current = _state.value
        if (current.busy) return

        var identifierError: Int? = null
        var passwordError: Int? = null
        if (current.identifier.isBlank()) {
            identifierError = R.string.auth_error_identifier_required
        } else {
            try {
                normalizeLoginIdentifier(current.identifier)
            } catch (e: ApiException) {
                identifierError = R.string.auth_error_identifier_invalid
            }
        }
        if (current.password.isEmpty()) {
            passwordError = R.string.auth_error_password_required
        }
        if (identifierError != null || passwordError != null) {
            _state.update { it.copy(identifierError = identifierError, passwordError = passwordError) }
            return
        }

        notices.value = null
        _state.update { it.copy(busy = true, error = null, identifierError = null, passwordError = null) }
        viewModelScope.launch {
            try {
                sessionManager.signIn(current.identifier, current.password)
                // The session state flips to Active and the shell replaces this screen.
                _state.update { it.copy(busy = false, password = "", passwordVisible = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ApiException) {
                _state.update { it.copy(busy = false, error = e.userMessage) }
            } catch (e: Exception) {
                _state.update { it.copy(busy = false, error = ApiErrorMapper.MSG_UNKNOWN) }
            }
        }
    }
}
