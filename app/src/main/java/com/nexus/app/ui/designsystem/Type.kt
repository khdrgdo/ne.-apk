package com.nexus.app.ui.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.nexus.app.R

/** Serif display family (headlines, body of posts). Font files are downloaded by CI into res/font. */
val AmiriFamily = FontFamily(
    Font(R.font.amiri_regular, FontWeight.Normal),
    Font(R.font.amiri_bold, FontWeight.Bold)
)

/** Sans UI family. */
val TajawalFamily = FontFamily(
    Font(R.font.tajawal_regular, FontWeight.Normal),
    Font(R.font.tajawal_medium, FontWeight.Medium),
    Font(R.font.tajawal_bold, FontWeight.Bold),
    Font(R.font.tajawal_extrabold, FontWeight.ExtraBold)
)

/** Named text styles of Direction A. */
object NexusType {
    val Masthead = TextStyle(fontFamily = AmiriFamily, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 44.sp)
    val ScreenTitle = TextStyle(fontFamily = AmiriFamily, fontWeight = FontWeight.Bold, fontSize = 38.sp, lineHeight = 52.sp)
    val SectionTitle = TextStyle(fontFamily = AmiriFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 34.sp)
    val EntryTitle = TextStyle(fontFamily = AmiriFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 30.sp)
    val PostBody = TextStyle(fontFamily = AmiriFamily, fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 30.sp)
    val Ui = TextStyle(fontFamily = TajawalFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 22.sp)
    val UiMedium = TextStyle(fontFamily = TajawalFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 22.sp)
    val UiBold = TextStyle(fontFamily = TajawalFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 22.sp)
    val Meta = TextStyle(fontFamily = TajawalFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp)
    val Tiny = TextStyle(fontFamily = TajawalFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp)
    val Tag = TextStyle(fontFamily = TajawalFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp, lineHeight = 14.sp)
    val Numeral = TextStyle(fontFamily = TajawalFamily, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, lineHeight = 28.sp)
    val NumeralSerif = TextStyle(fontFamily = AmiriFamily, fontWeight = FontWeight.Bold, fontSize = 38.sp, lineHeight = 46.sp)
}

internal val NexusMaterialTypography = Typography(
    displayLarge = NexusType.ScreenTitle,
    displayMedium = NexusType.ScreenTitle,
    displaySmall = NexusType.SectionTitle,
    headlineLarge = NexusType.ScreenTitle,
    headlineMedium = NexusType.SectionTitle,
    headlineSmall = NexusType.EntryTitle,
    titleLarge = NexusType.SectionTitle,
    titleMedium = NexusType.EntryTitle,
    titleSmall = NexusType.UiBold,
    bodyLarge = NexusType.PostBody,
    bodyMedium = NexusType.Ui,
    bodySmall = NexusType.Meta,
    labelLarge = NexusType.UiBold,
    labelMedium = NexusType.Meta,
    labelSmall = NexusType.Tiny
)
