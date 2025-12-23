package com.begonia.mediawidget.widget

import androidx.datastore.preferences.core.stringPreferencesKey

object MusicWidgetKeys {
    val titleKey = stringPreferencesKey("track_title")
    val artistKey = stringPreferencesKey("track_artist")
    val coverPathKey = stringPreferencesKey("cover_image_path")
}