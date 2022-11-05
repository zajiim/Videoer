package com.neon.videoer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore.Video
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.neon.videoer.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {
    lateinit var binding: ActivityPlayerBinding
    companion object {
        lateinit var player: ExoPlayer
        lateinit var playerList: ArrayList<com.neon.videoer.models.Video>
        var position: Int = -1
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)

        setContentView(binding.root)
        initializeLayout()
    }

    private fun initializeLayout() {
        when(intent.getStringExtra("class")) {
            "AllVideos" -> {
                playerList = ArrayList()
                playerList.addAll(MainActivity.videoList)
            }
            "FolderActivity" -> {
                playerList = ArrayList()
                playerList.addAll(FoldersActivity.currentFolderVideos)
            }
        }
        createPlayer()
    }

    private fun createPlayer() {
        player = ExoPlayer.Builder(this).build()
        binding.playerView.player = player
        val mediaItem = MediaItem.fromUri(playerList[position].artUri)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}

