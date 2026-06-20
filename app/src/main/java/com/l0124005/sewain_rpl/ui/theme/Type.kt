package com.l0124005.sewain_rpl.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.l0124005.sewain_rpl.R

val VolkhovFont = FontFamily(Font(R.font.volkhov_regular))
val MonsterratFont = FontFamily(Font(R.font.montserrat))
val VidalokaFont = FontFamily(Font(R.font.vidaloka_regular))
val MontaguSlabFont = FontFamily(
    Font(R.font.montaguslab_regular, FontWeight.Normal),
    Font(R.font.montaguslab_regular, FontWeight.Medium),
    Font(R.font.montaguslab_semibold, FontWeight.SemiBold),
    Font(R.font.montaguslab_bold, FontWeight.Bold)
)

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)