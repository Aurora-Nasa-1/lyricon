/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.lyricon.statusbarlyric

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.SurfaceTexture
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.BitmapDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.isVisible
import io.github.proify.android.extensions.dp
import io.github.proify.lyricon.lyric.style.WidgetStyle
import java.io.File

@SuppressLint("ViewConstructor")
class StatusWidgetView(context: Context) : FrameLayout(context) {

    private val imageView = ImageView(context).apply {
        scaleType = ImageView.ScaleType.FIT_CENTER
    }

    private var mediaPlayer: MediaPlayer? = null
    private var textureView: TextureView? = null
    private var currentWidgetStyle: WidgetStyle? = null
    private var isPlaying: Boolean = false

    init {
        addView(imageView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    fun applyStyle(style: WidgetStyle) {
        currentWidgetStyle = style
        updateLayout(style)
        if (style.enabled) {
            loadMedia(style)
        } else {
            stopMedia()
            isVisible = false
        }
    }

    private fun updateLayout(style: WidgetStyle) {
        val lp = layoutParams ?: LayoutParams(style.width.dp, style.height.dp)
        lp.width = style.width.dp
        lp.height = style.height.dp
        layoutParams = lp
    }

    fun setPlaying(playing: Boolean) {
        isPlaying = playing
        updateVisibility()
    }

    fun updateVisibility() {
        val style = currentWidgetStyle ?: return
        if (!style.enabled) {
            isVisible = false
            return
        }

        val shouldShow = if (style.showOnlyWhenPlaying) isPlaying else true
        isVisible = shouldShow

        if (shouldShow) {
            startMedia()
        } else {
            pauseMedia()
        }
    }

    private fun loadMedia(style: WidgetStyle) {
        stopMedia()
        val file = File(style.mediaPath)
        if (!file.exists()) {
            Log.e("StatusWidgetView", "Media file not found: ${style.mediaPath}")
            return
        }

        when (style.mediaType) {
            WidgetStyle.TYPE_IMAGE -> loadImage(file)
            WidgetStyle.TYPE_GIF -> loadGif(file)
            WidgetStyle.TYPE_VIDEO -> loadVideo(file)
        }
        updateVisibility()
    }

    private fun loadImage(file: File) {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        imageView.setImageBitmap(bitmap)
        imageView.isVisible = true
        removeTextureView()
    }

    private fun loadGif(file: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(file)
            val drawable = ImageDecoder.decodeDrawable(source)
            imageView.setImageDrawable(drawable)
            if (drawable is AnimatedImageDrawable) {
                drawable.start()
            }
        } else {
            // Fallback or use a library, but since minSdk is 27 and P is 28,
            // maybe just use Movie or simple Bitmap for 27.
            loadImage(file)
        }
        imageView.isVisible = true
        removeTextureView()
    }

    private fun loadVideo(file: File) {
        imageView.isVisible = false
        addTextureView()

        textureView?.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                val s = Surface(surface)
                try {
                    mediaPlayer = MediaPlayer().apply {
                        setSurface(s)
                        setDataSource(context, Uri.fromFile(file))
                        isLooping = true
                        prepareAsync()
                        setOnPreparedListener {
                            if (isVisible) start()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("StatusWidgetView", "Failed to load video", e)
                }
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                stopMedia()
                return true
            }
            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }
    }

    private fun addTextureView() {
        if (textureView == null) {
            textureView = TextureView(context)
            addView(textureView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        }
    }

    private fun removeTextureView() {
        textureView?.let {
            removeView(it)
            textureView = null
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopMedia()
    }

    private fun startMedia() {
        val drawable = imageView.drawable
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && drawable is AnimatedImageDrawable) {
            drawable.start()
        }
        mediaPlayer?.let {
            if (!it.isPlaying) it.start()
        }
    }

    private fun pauseMedia() {
        val drawable = imageView.drawable
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && drawable is AnimatedImageDrawable) {
            drawable.stop()
        }
        mediaPlayer?.let {
            if (it.isPlaying) it.pause()
        }
    }

    private fun stopMedia() {
        mediaPlayer?.let {
            it.stop()
            it.release()
        }
        mediaPlayer = null

        val drawable = imageView.drawable
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && drawable is AnimatedImageDrawable) {
            drawable.stop()
        }
        imageView.setImageDrawable(null)
    }
}
