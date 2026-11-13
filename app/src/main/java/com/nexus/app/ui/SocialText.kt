package com.nexus.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle

@Composable
fun ExpandablePostText(
    content: String,
    modifier: Modifier = Modifier,
    collapsedLines: Int = 4,
    onMention: ((String) -> Unit)? = null
) {
    var expanded by rememberSaveable(content) { mutableStateOf(false) }
    val long = remember(content) { content.length > 260 || content.count { it == '\n' } >= collapsedLines }
    Column(modifier) {
        MentionStyledText(content, maxLines = if (long && !expanded) collapsedLines else Int.MAX_VALUE, onMention = onMention)
        if (long) TextButton(onClick = { expanded = !expanded }) {
            Text(if (expanded) "عرض أقل" else "عرض المزيد", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun MentionStyledText(
    content: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    onMention: ((String) -> Unit)? = null
) {
    val accent = MaterialTheme.colorScheme.primary
    val annotated = remember(content, accent) {
        val regex = Regex("@[\\p{L}\\p{N}_-]{2,}")
        buildAnnotatedString {
            var cursor = 0
            regex.findAll(content).forEach { match ->
                append(content.substring(cursor, match.range.first))
                pushStringAnnotation("mention", match.value.drop(1).replace('_', ' '))
                withStyle(SpanStyle(color = accent, fontWeight = FontWeight.SemiBold)) { append(match.value) }
                pop()
                cursor = match.range.last + 1
            }
            append(content.substring(cursor))
        }
    }
    ClickableText(
        text = annotated,
        modifier = modifier,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodyLarge,
        onClick = { offset ->
            val tag = annotated.getStringAnnotations("mention", offset, offset).firstOrNull()?.item
            if (!tag.isNullOrBlank()) onMention?.invoke(tag)
        }
    )
}


fun relativeTimeArabic(value: String): String {
    val instant = runCatching { java.time.Instant.parse(value) }.getOrNull() ?: return value.take(16).replace('T', ' ')
    val seconds = ((System.currentTimeMillis() - instant.toEpochMilli()) / 1000L).coerceAtLeast(0L)
    return when {
        seconds < 60 -> "الآن"
        seconds < 3600 -> "منذ ${seconds / 60} دقيقة"
        seconds < 86400 -> "منذ ${seconds / 3600} ساعة"
        seconds < 604800 -> "منذ ${seconds / 86400} يوم"
        else -> java.time.format.DateTimeFormatter.ofPattern("d MMM", java.util.Locale("ar")).withZone(java.time.ZoneId.systemDefault()).format(instant)
    }
}
