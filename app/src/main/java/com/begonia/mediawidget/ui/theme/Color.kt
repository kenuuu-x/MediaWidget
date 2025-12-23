package com.begonia.mediawidget.ui.theme

import android.annotation.SuppressLint
import androidx.compose.ui.graphics.Color
import androidx.glance.unit.ColorProvider
import com.begonia.mediawidget.R

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

object ThemeExtras {

    val iconColor: ColorProvider
        @SuppressLint("RestrictedApi")
        get() = ColorProvider(R.color.icon_color)

    val widgetBackgroundColor: ColorProvider
        @SuppressLint("RestrictedApi")
        get() = ColorProvider(R.color.background_color)

    val textColor: ColorProvider
        @SuppressLint("RestrictedApi")
        get() = ColorProvider(R.color.text_color)

}