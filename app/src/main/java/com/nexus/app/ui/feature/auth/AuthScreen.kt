package com.nexus.app.ui.feature.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.app.R
import com.nexus.app.ui.designsystem.Kicker
import com.nexus.app.ui.designsystem.LocalNexusColors
import com.nexus.app.ui.designsystem.NexusButton
import com.nexus.app.ui.designsystem.NexusButtonStyle
import com.nexus.app.ui.designsystem.NexusIcon
import com.nexus.app.ui.designsystem.NexusIconView
import com.nexus.app.ui.designsystem.NexusMasthead
import com.nexus.app.ui.designsystem.NexusType
import com.nexus.app.ui.designsystem.QuoteBox
import com.nexus.app.ui.designsystem.Spacing
import com.nexus.app.ui.designsystem.UnderlineField

@Composable
fun AuthRoute(viewModel: AuthViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AuthScreen(
        state = state,
        onIdentifierChange = viewModel::onIdentifierChange,
        onPasswordChange = viewModel::onPasswordChange,
        onTogglePassword = viewModel::togglePasswordVisible,
        onSubmit = viewModel::submit,
        onDismissNotice = viewModel::dismissNotice,
        modifier = modifier
    )
}

@Composable
fun AuthScreen(
    state: AuthUiState,
    onIdentifierChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onSubmit: () -> Unit,
    onDismissNotice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalNexusColors.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.Gutter)
    ) {
        NexusMasthead(title = stringResource(R.string.app_name))
        Spacer(Modifier.height(Spacing.Xxl))
        Kicker(label = stringResource(R.string.auth_kicker))
        Spacer(Modifier.height(Spacing.Sm))
        Text(
            text = stringResource(R.string.auth_title),
            style = NexusType.ScreenTitle,
            color = colors.ink
        )
        Spacer(Modifier.height(Spacing.Sm))
        Text(
            text = stringResource(R.string.auth_subtitle),
            style = NexusType.Ui,
            color = colors.mute
        )
        Spacer(Modifier.height(Spacing.Xl))

        val notice = state.notice
        if (notice != null) {
            QuoteBox {
                Text(text = notice, style = NexusType.Ui, color = colors.ink)
                Text(
                    text = stringResource(R.string.action_dismiss),
                    style = NexusType.UiBold,
                    color = colors.accent,
                    modifier = Modifier
                        .padding(top = Spacing.Sm)
                        .clickable(role = Role.Button, onClick = onDismissNotice)
                )
            }
            Spacer(Modifier.height(Spacing.Lg))
        }

        val identifierErrorRes = state.identifierError
        UnderlineField(
            value = state.identifier,
            onValueChange = onIdentifierChange,
            label = stringResource(R.string.auth_identifier_label),
            error = if (identifierErrorRes != null) stringResource(identifierErrorRes) else null,
            enabled = !state.busy,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
        )
        Spacer(Modifier.height(Spacing.Lg))

        val passwordErrorRes = state.passwordError
        UnderlineField(
            value = state.password,
            onValueChange = onPasswordChange,
            label = stringResource(R.string.auth_password_label),
            error = if (passwordErrorRes != null) stringResource(passwordErrorRes) else null,
            enabled = !state.busy,
            password = true,
            passwordVisible = state.passwordVisible,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            trailing = {
                Box(
                    modifier = Modifier
                        .size(Spacing.TouchTarget)
                        .clickable(role = Role.Button, onClick = onTogglePassword),
                    contentAlignment = Alignment.Center
                ) {
                    NexusIconView(
                        icon = if (state.passwordVisible) NexusIcon.EyeOff else NexusIcon.Eye,
                        tint = colors.mute,
                        contentDescription = if (state.passwordVisible) {
                            stringResource(R.string.auth_hide_password)
                        } else {
                            stringResource(R.string.auth_show_password)
                        }
                    )
                }
            }
        )

        val error = state.error
        if (error != null) {
            Spacer(Modifier.height(Spacing.Md))
            Text(text = error, style = NexusType.UiMedium, color = colors.accent)
        }

        Spacer(Modifier.height(Spacing.Xl))
        NexusButton(
            text = stringResource(R.string.auth_submit),
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            style = NexusButtonStyle.Filled,
            loading = state.busy
        )
        Spacer(Modifier.height(Spacing.Xxl))
    }
}
