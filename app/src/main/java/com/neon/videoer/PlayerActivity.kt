package com.neon.videoer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem

import com.neon.videoer.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding
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
        initializeBinding()
    }

    private fun initializeLayout() {
        when(intent.getStringExtra("class")) {
            "AllVideos" -> {
                playerList = ArrayList()
                playerList.addAll(MainActivity.videoList)
                createPlayer()
            }
            "FolderActivity" -> {
                playerList = ArrayList()
                playerList.addAll(FoldersActivity.currentFolderVideos)
                createPlayer()
            }
        }

    }

    private fun initializeBinding() {
        binding.backBtn.setOnClickListener {
            finish()
        }
        binding.playPauseBtn.setOnClickListener {
            if(player.isPlaying) pauseVideo()
            else playVideo()
        }
        binding.nextBtn.setOnClickListener {
            nextPrevVideo()
        }
        binding.prevBtn.setOnClickListener {
            nextPrevVideo(isNext = false)
        }
    }





    private fun createPlayer() {
        binding.videoTitle.text = playerList[position].title
        binding.videoTitle.isSelected = true
        player = ExoPlayer.Builder(this).build()
        binding.playerView.player = player
        val mediaItem = MediaItem.fromUri(playerList[position].artUri)
        player.setMediaItem(mediaItem)
        player.prepare()
        playVideo()
    }

    private fun playVideo() {
        binding.playPauseBtn.setImageResource(R.drawable.pause_icon)
        player.play()
    }
    private fun pauseVideo() {
        binding.playPauseBtn.setImageResource(R.drawable.play_icon)
        player.pause()
    }

    private fun nextPrevVideo(isNext: Boolean = true) {
        if(isNext) setPosition()
        else setPosition(isIncrement = false)
        createPlayer()

    }

    private fun setPosition(isIncrement: Boolean = true) {
        if(isIncrement) {
            if(playerList.size - 1 == position) {
                position = 0
            } else {
                ++position
            }
        } else {
            if(position == 0) {
                position = playerList.size - 1
            } else {
                --position
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}

