/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.lyricon.lyric.style

import android.content.SharedPreferences
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class WidgetStyle(
    var enabled: Boolean = Defaults.ENABLED,
    var showOnlyWhenPlaying: Boolean = Defaults.SHOW_ONLY_WHEN_PLAYING,
    var position: Int = Defaults.POSITION,
    var mediaPath: String = Defaults.MEDIA_PATH,
    var mediaType: Int = Defaults.MEDIA_TYPE,
    var width: Float = Defaults.WIDTH,
    var height: Float = Defaults.HEIGHT,
) : AbstractStyle(), Parcelable {

    override fun onLoad(preferences: SharedPreferences) {
        enabled = preferences.getBoolean("lyric_style_widget_enabled", Defaults.ENABLED)
        showOnlyWhenPlaying = preferences.getBoolean("lyric_style_widget_show_only_when_playing", Defaults.SHOW_ONLY_WHEN_PLAYING)
        position = preferences.getInt("lyric_style_widget_position", Defaults.POSITION)
        mediaPath = preferences.getString("lyric_style_widget_media_path", Defaults.MEDIA_PATH) ?: Defaults.MEDIA_PATH
        mediaType = preferences.getInt("lyric_style_widget_media_type", Defaults.MEDIA_TYPE)
        width = preferences.getFloat("lyric_style_widget_width", Defaults.WIDTH)
        height = preferences.getFloat("lyric_style_widget_height", Defaults.HEIGHT)
    }

    override fun onWrite(editor: SharedPreferences.Editor) {
        editor.putBoolean("lyric_style_widget_enabled", enabled)
        editor.putBoolean("lyric_style_widget_show_only_when_playing", showOnlyWhenPlaying)
        editor.putInt("lyric_style_widget_position", position)
        editor.putString("lyric_style_widget_media_path", mediaPath)
        editor.putInt("lyric_style_widget_media_type", mediaType)
        editor.putFloat("lyric_style_widget_width", width)
        editor.putFloat("lyric_style_widget_height", height)
    }

    object Defaults {
        const val POSITION_FAR_RIGHT = 0
        const val POSITION_LEFT_OF_ICONS = 1

        const val TYPE_IMAGE = 0
        const val TYPE_GIF = 1
        const val TYPE_VIDEO = 2

        const val ENABLED: Boolean = false
        const val SHOW_ONLY_WHEN_PLAYING: Boolean = false
        const val POSITION: Int = POSITION_FAR_RIGHT
        const val MEDIA_PATH: String = ""
        const val MEDIA_TYPE: Int = TYPE_IMAGE
        const val WIDTH: Float = 24f
        const val HEIGHT: Float = 24f
    }

    companion object {
        const val POSITION_FAR_RIGHT = Defaults.POSITION_FAR_RIGHT
        const val POSITION_LEFT_OF_ICONS = Defaults.POSITION_LEFT_OF_ICONS

        const val TYPE_IMAGE = Defaults.TYPE_IMAGE
        const val TYPE_GIF = Defaults.TYPE_GIF
        const val TYPE_VIDEO = Defaults.TYPE_VIDEO
    }
}
