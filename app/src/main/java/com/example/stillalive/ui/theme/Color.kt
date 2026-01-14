package com.example.stillalive.ui.theme

import androidx.compose.ui.graphics.Color

// 马卡龙色系定义
val MacaronPink = Color(0xFFFFB7B2)   // 柔和粉
val MacaronGreen = Color(0xFFE2F0CB)  // 柔和绿
val MacaronBlue = Color(0xFFB5EAD7)   // 柔和蓝
val MacaronPurple = Color(0xFFC7CEEA) // 柔和紫
val MacaronYellow = Color(0xFFFFFFD1) // 柔和黄
val MacaronOrange = Color(0xFFFFDAC1) // 柔和橙

val LightColors = androidx.compose.material3.lightColorScheme(
    primary = Color(0xFF8E99F3), // 稍微深一点的紫色作为主色，保持可读性
    onPrimary = Color.White,
    primaryContainer = MacaronBlue,
    onPrimaryContainer = Color(0xFF002018),
    secondary = MacaronGreen,
    onSecondary = Color(0xFF1A2C00),
    secondaryContainer = MacaronGreen,
    onSecondaryContainer = Color(0xFF052100),
    tertiary = MacaronPink,
    onTertiary = Color(0xFF3E001D),
    tertiaryContainer = MacaronPink,
    onTertiaryContainer = Color(0xFF3E001D),
    background = Color(0xFFFDFBF7), // 暖白背景
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = MacaronYellow,
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    error = MacaronOrange,
    onError = Color.Black
)
