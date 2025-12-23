package com.begonia.mediawidget

import android.content.ComponentName
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import com.begonia.mediawidget.widget.ImagePipeline
import com.begonia.mediawidget.widget.MediaWidget
import com.begonia.mediawidget.widget.MusicWidgetKeys
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class MusicMonitorService : NotificationListenerService() {

    private lateinit var mediaSessionManager: MediaSessionManager
    private lateinit var sessionsChangedListener: MediaSessionManager.OnActiveSessionsChangedListener

    private val scope = CoroutineScope(Dispatchers.IO)
    private val mediaCallback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            super.onMetadataChanged(metadata)
            Timber.d("🎵 Метаданные изменились.")
            logMetadata(metadata, currentController?.playbackState)
            updateWidgetData(metadata)
        }
    }

    private var currentController: MediaController? = null

    override fun onListenerConnected() {
        super.onListenerConnected()
        Timber.d("onListenerConnected: Служба успешно подключена 🟢")

        mediaSessionManager = getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager

        sessionsChangedListener =
            MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
                Timber.d("Список плееров изменился. Найдено: ${controllers?.size}")
                updateCurrentController(controllers)
            }

        val componentName = ComponentName(this, MusicMonitorService::class.java)

        try {
            mediaSessionManager.addOnActiveSessionsChangedListener(
                sessionsChangedListener,
                componentName
            )
            val initialControllers = mediaSessionManager.getActiveSessions(componentName)
            updateCurrentController(initialControllers)
        } catch (e: SecurityException) {
            Timber.e(e)
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Timber.d("onListenerDisconnected: Служба отключена 🔴")
        mediaSessionManager.removeOnActiveSessionsChangedListener(sessionsChangedListener)
    }

    private fun updateWidgetData(metadata: MediaMetadata?) {
        scope.launch {
            val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "Unknown title"
            val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "Unknown artist"
            val rawBitmap = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)

            val coverPath = createCoverPath(rawBitmap)

            pushWidgetUpdate(title, artist, coverPath)
        }
    }

    private fun createCoverPath(rawBitmap: Bitmap?): String? {
        return if (rawBitmap != null) {
            val pipeline = ImagePipeline(applicationContext)

            val processedBitmap = pipeline
                .fromBitmap(rawBitmap)
                .scale()
                .roundCorners()
                .process()

            pipeline.saveBitmapToCache(processedBitmap)
        } else {
            null
        }
    }

    private suspend fun pushWidgetUpdate(
        title: String,
        artist: String,
        coverPath: String?
    ) {
        val context = applicationContext
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(MediaWidget::class.java)

        glanceIds.forEach { glanceId ->
            updateAppWidgetState(context, glanceId) { prefs ->
                prefs[MusicWidgetKeys.titleKey] = title
                prefs[MusicWidgetKeys.artistKey] = artist

                if (coverPath != null) {
                    prefs[MusicWidgetKeys.coverPathKey] = coverPath
                } else {
                    prefs.remove(MusicWidgetKeys.coverPathKey)
                }
            }

            MediaWidget().update(context, glanceId)
        }
    }

    private fun updateCurrentController(controllers: List<MediaController>?) {
        if (controllers.isNullOrEmpty()) return

        val controller = controllers.first()
        if (currentController?.sessionToken == controller.sessionToken) return

        Timber.d("Переключение на новый плеер: ${controller.packageName}")

        currentController?.unregisterCallback(mediaCallback)

        currentController = controller
        currentController?.registerCallback(mediaCallback)

        logMetadata(controller.metadata, controller.playbackState)
        updateWidgetData(controller.metadata)
    }

    private fun logMetadata(metadata: MediaMetadata?, state: PlaybackState?) {
        if (metadata == null) return

        val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "Unknown artist"
        val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "Unknown title"

        val isPlaying = state?.state == PlaybackState.STATE_PLAYING

        Timber.d("Текущий трек: $title - $artist / Играет: $isPlaying")
    }
}