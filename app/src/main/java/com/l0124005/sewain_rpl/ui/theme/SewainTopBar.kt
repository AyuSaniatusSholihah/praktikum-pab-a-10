package com.l0124005.sewain_rpl.ui.theme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.text.style.TextAlign

@Composable
fun SewainTopBar(
    // Opsional: action icon di kanan (misal filter, cart, dll)
    navigationIcon: ImageVector? = null,
    onNavigationClick: () -> Unit = {},
    actionIcon: ImageVector? = null,
    actionTint: Color = BluePrimary,
    onActionClick: () -> Unit = {}
) {
    Surface(color = Color.White, shadowElevation = 1.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Navigation Icon (Hamburger)
            if (navigationIcon != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = navigationIcon,
                        contentDescription = "Navigation",
                        tint = NavyPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                Spacer(Modifier.width(48.dp))
            }

            // Logo SEWAIN
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color(0xFF484848))) {
                        append("SEWA")
                    }
                    withStyle(style = SpanStyle(color = Color(0xFF6A87A1))) {
                        append("IN")
                    }
                },
                fontSize = 24.sp,
                fontFamily = VidalokaFont,
                fontWeight = FontWeight.Normal,
                letterSpacing = 1.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            // Icon kanan (opsional)
            if (actionIcon != null) {
                IconButton(onClick = onActionClick) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = null,
                        tint = actionTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            } else {
                Spacer(Modifier.width(48.dp))
            }
        }
    }
    HorizontalDivider(color = Color(0xFFE8E8E8), thickness = 0.5.dp)
}