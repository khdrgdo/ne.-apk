package com.nexus.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage

@Composable
fun PostMediaGrid(images: List<String>, modifier: Modifier = Modifier) {
    if (images.isEmpty()) return
    val visible = images.take(4)
    var selected by rememberSaveable(images) { mutableStateOf<String?>(null) }
    val gap = 2.dp
    Column(modifier.fillMaxWidth()) {
        when (visible.size) {
            1 -> PostMediaCell(visible[0], Modifier.fillMaxWidth().aspectRatio(16f / 9f)) { selected = visible[0] }
            2 -> Row(Modifier.fillMaxWidth().height(218.dp), horizontalArrangement = Arrangement.spacedBy(gap)) {
                PostMediaCell(visible[0], Modifier.weight(1f).fillMaxHeight()) { selected = visible[0] }
                PostMediaCell(visible[1], Modifier.weight(1f).fillMaxHeight()) { selected = visible[1] }
            }
            3 -> Row(Modifier.fillMaxWidth().height(246.dp), horizontalArrangement = Arrangement.spacedBy(gap)) {
                PostMediaCell(visible[0], Modifier.weight(1.15f).fillMaxHeight()) { selected = visible[0] }
                Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(gap)) {
                    PostMediaCell(visible[1], Modifier.fillMaxWidth().weight(1f)) { selected = visible[1] }
                    PostMediaCell(visible[2], Modifier.fillMaxWidth().weight(1f)) { selected = visible[2] }
                }
            }
            else -> Column(Modifier.fillMaxWidth().height(300.dp), verticalArrangement = Arrangement.spacedBy(gap)) {
                Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap)) {
                    PostMediaCell(visible[0], Modifier.weight(1f).fillMaxHeight()) { selected = visible[0] }
                    PostMediaCell(visible[1], Modifier.weight(1f).fillMaxHeight()) { selected = visible[1] }
                }
                Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap)) {
                    PostMediaCell(visible[2], Modifier.weight(1f).fillMaxHeight()) { selected = visible[2] }
                    PostMediaCell(visible[3], Modifier.weight(1f).fillMaxHeight(), (images.size - 4).coerceAtLeast(0)) { selected = visible[3] }
                }
            }
        }
    }
    if (selected != null) Dialog(onDismissRequest = { selected = null }) {
        Surface(shape = RoundedCornerShape(14.dp), color = Color.Black) {
            Box(Modifier.fillMaxWidth().heightIn(max = 650.dp)) {
                AsyncImage(model = selected, contentDescription = "عرض الصورة", modifier = Modifier.fillMaxWidth().heightIn(max = 650.dp), contentScale = ContentScale.Fit)
                IconButton(onClick = { selected = null }, modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).background(Color.Black.copy(alpha = .55f), RoundedCornerShape(50))) {
                    Icon(Icons.Outlined.Close, "إغلاق", tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun PostMediaCell(url: String, modifier: Modifier, overflow: Int = 0, onClick: () -> Unit) {
    Box(modifier.clip(RoundedCornerShape(3.dp)).clickable(onClick = onClick)) {
        AsyncImage(model = url, contentDescription = "صورة في المنشور", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        if (overflow > 0) Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .55f)), contentAlignment = Alignment.Center) {
            Text("+$overflow", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
    }
}
