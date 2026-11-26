package com.nexus.app.ui.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Label above, text, then a 1dp rule (ink when focused, accent on error, faint otherwise). */
@Composable
fun UnderlineField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    enabled: Boolean = true,
    password: Boolean = false,
    passwordVisible: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailing: (@Composable () -> Unit)? = null
) {
    val c = LocalNexusColors.current
    var focused by remember { mutableStateOf(false) }
    val ruleColor = when {
        error != null -> c.accent
        focused -> c.ink
        else -> c.faint
    }
    val ruleThickness = if (focused || error != null) NexusDims.RuleMedium else NexusDims.RuleThin
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = label, style = NexusType.Meta, color = c.mute)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f).padding(vertical = 10.dp)) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    enabled = enabled,
                    singleLine = true,
                    textStyle = NexusType.Ui.copy(fontSize = 16.sp, color = c.ink),
                    cursorBrush = SolidColor(c.accent),
                    visualTransformation = if (password && !passwordVisible) {
                        PasswordVisualTransformation()
                    } else {
                        VisualTransformation.None
                    },
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    modifier = Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused }
                )
            }
            if (trailing != null) trailing()
        }
        Rule(thickness = ruleThickness, color = ruleColor)
        if (error != null) {
            Text(text = error, style = NexusType.Meta, color = c.accent, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
