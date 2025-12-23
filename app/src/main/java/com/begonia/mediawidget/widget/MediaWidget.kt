package com.begonia.mediawidget.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.layout.wrapContentHeight
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.begonia.mediawidget.R
import com.begonia.mediawidget.ui.theme.ThemeExtras

class MediaWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {

        provideContent {
            val prefs = currentState<Preferences>()

            val coverPath = prefs[MusicWidgetKeys.coverPathKey]
            val bitmap = if (coverPath != null) {
                BitmapFactory.decodeFile(coverPath)
            } else {
                ImagePipeline(context)
                    .fromRes(R.drawable.image_placeholder)
                    .scale()
                    .roundCorners()
                    .process()
            }

            val title = prefs[MusicWidgetKeys.titleKey] ?: "2DWRLD"
            val artist = prefs[MusicWidgetKeys.artistKey] ?: "Midix"

            GlanceTheme {
                WidgetContent(
                    title = title,
                    artist = artist,
                    bitmap = bitmap,
                )
            }
        }
    }
}

@Suppress("ParamsComparedByRef")
@Composable
private fun WidgetContent(
    title: String,
    artist: String,
    bitmap: Bitmap?,
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ThemeExtras.widgetBackgroundColor)
            .appWidgetBackground()
            .cornerRadius(28.dp)
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(start = 16.dp, bottom = 16.dp, end = 3.dp, top = 3.dp),
        ) {
            MediaDisplay(
                bitmap = bitmap,
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .defaultWeight()
            )

            Spacer(modifier = GlanceModifier.height(16.dp))

            MediaInfo(
                title = title,
                artist = artist,
                modifier = GlanceModifier.fillMaxWidth()
            )
        }
    }
}

@Suppress("ParamsComparedByRef")
@Composable
private fun MediaDisplay(
    bitmap: Bitmap?,
    modifier: GlanceModifier = GlanceModifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top
    ) {
        Image(
            provider = if (bitmap != null) ImageProvider(bitmap)
            else ImageProvider(R.drawable.image_placeholder),
            contentDescription = "Album Art",
            contentScale = ContentScale.Fit,
            modifier = GlanceModifier
                .fillMaxWidth()
                .wrapContentHeight()
                .defaultWeight()
                .padding(top = 16.dp)
        )

        Spacer(modifier = GlanceModifier.width(8.dp))

        Image(
            provider = ImageProvider(R.drawable.ic_launcher_renaissance_foreground),
            contentDescription = "Service Icon",
            colorFilter = ColorFilter.tint(ThemeExtras.iconColor),
            modifier = GlanceModifier.size(39.dp)
        )
    }
}

@Composable
fun MediaInfo(
    title: String,
    artist: String,
    modifier: GlanceModifier = GlanceModifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = TextStyle(
                color = ThemeExtras.textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            ),
            maxLines = 1
        )

        Text(
            text = artist,
            style = TextStyle(
                color = ThemeExtras.textColor,
                fontSize = 12.sp
            ),
            maxLines = 1
        )
    }
}
