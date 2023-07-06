package com.topstep.fitcloud.sample2.utils

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioManager
import android.media.MediaPlayer
import timber.log.Timber
import java.io.IOException

class MediaPlayerHelper {

    private var mediaPlayer: MediaPlayer? = null

    fun startPlay(context: Context, fileName: String) {
        val player = mediaPlayer ?: MediaPlayer().apply {
            setAudioStreamType(AudioManager.STREAM_MUSIC)
            setOnPreparedListener {
                try {
                    it.start()
                } catch (e: Exception) {
                    Timber.w(e)
                }
            }
        }.also { mediaPlayer = it }
        if (player.isPlaying) {
            player.stop()
        }
        player.reset()
        try {
            val fd: AssetFileDescriptor = context.resources.assets.openFd(fileName)
            player.setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
            player.prepareAsync()
        } catch (e: IOException) {
            Timber.w(e)
        }
    }

    fun stopPlay() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.reset()
        }
    }

}