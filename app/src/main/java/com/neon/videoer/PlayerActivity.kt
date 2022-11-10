package com.neon.videoer

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.neon.videoer.databinding.ActivityPlayerBinding
import com.neon.videoer.databinding.MoreFeaturesBinding
import com.neon.videoer.databinding.PlaybackSpeedDialogBinding
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList

class PlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding
    private lateinit var runnable: Runnable
    private var isSubtitle: Boolean = true
    private lateinit var trackSelector: DefaultTrackSelector
    companion object {
        lateinit var player: ExoPlayer
        lateinit var playerList: ArrayList<com.neon.videoer.models.Video>
        var position: Int = -1
        private var repeat: Boolean = false
        private var isFullScreen: Boolean = false
        private var isLocked: Boolean = false

        private var speed: Float = 1.0f
        var pipStatus: Int = 0
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        //For Notch phones above Pie
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

        }
        binding = ActivityPlayerBinding.inflate(layoutInflater)

        setTheme(R.style.playerActivityTheme)
        setContentView(binding.root)
        //For immersive Mode
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).let{ controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE


        }
        initializeLayout()
        initializeBinding()
        binding.backwardFL.setOnClickListener ( DoubleClickListener(callback = object :DoubleClickListener.Callback{
            override fun doubleClicked() {
                binding.playerView.showController()
                binding.backwardBtn.visibility = View.VISIBLE
                player.seekTo(player.currentPosition - 5000)

            }

        }))

        binding.forwardFL.setOnClickListener ( DoubleClickListener(callback = object :DoubleClickListener.Callback{
            override fun doubleClicked() {
                binding.playerView.showController()
                binding.forwardBtn.visibility = View.VISIBLE
                player.seekTo(player.currentPosition + 5000)

            }

        }))
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

        if(repeat) binding.repeatBtn.setImageResource(com.google.android.exoplayer2.ui.R.drawable.exo_controls_repeat_all)
        else binding.repeatBtn.setImageResource(com.google.android.exoplayer2.ui.R.drawable.exo_controls_repeat_off)

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
        binding.repeatBtn.setOnClickListener {
            if (repeat) {
                repeat = false
                player.repeatMode = Player.REPEAT_MODE_OFF
                binding.repeatBtn.setImageResource(com.google.android.exoplayer2.ui.R.drawable.exo_controls_repeat_off)
            } else {
                repeat = true
                player.repeatMode = Player.REPEAT_MODE_ONE
                binding.repeatBtn.setImageResource(com.google.android.exoplayer2.ui.R.drawable.exo_controls_repeat_all)
            }
        }

        binding.fullScreenBtn.setOnClickListener {
            if(isFullScreen) {
                isFullScreen = false
                playInFullscreen(false)
            } else {
                isFullScreen = true
                playInFullscreen(true)
            }
        }

        binding.lockBtn.setOnClickListener {
            if(!isLocked) {
                isLocked = true
                binding.playerView.hideController()
                binding.playerView.useController = false
                binding.lockBtn.setImageResource(R.drawable.lock_close_icon)
            } else {
                isLocked = false
                binding.playerView.useController = true
                binding.playerView.showController()
                binding.lockBtn.setImageResource(R.drawable.lock_open_icon)
            }
        }

        binding.moreFeaturesBtn.setOnClickListener {
            pauseVideo()
            val customDialog = LayoutInflater.from(this).inflate(R.layout.more_features, binding.root, false)
            val bindingMF = MoreFeaturesBinding.bind(customDialog)
            val dialog = MaterialAlertDialogBuilder(this).setView(customDialog)
                .setOnCancelListener {
                    playVideo()
                }
                .setBackground(ColorDrawable(0x00090809)).create()
            dialog.show()

            bindingMF.audioTrackBtn.setOnClickListener {
                dialog.dismiss()
                playVideo()
                val audioTrack = ArrayList<String>()
                for(i in 0 until player.currentTrackGroups.length) {
                    if(player.currentTrackGroups.get(i).getFormat(0).selectionFlags == C.SELECTION_FLAG_DEFAULT) {
                        audioTrack.add(Locale(player.currentTrackGroups.get(i).getFormat(0).language.toString()).displayLanguage)
                    }
                }


                val tempTracks = audioTrack.toArray(arrayOfNulls<CharSequence>(audioTrack.size))
                MaterialAlertDialogBuilder(this, R.style.alertDialog)
                    .setTitle("Select Audio Track")
                    .setOnCancelListener {
                        playVideo()
                    }
                    .setBackground(ColorDrawable(0x00090809))
                    .setItems(tempTracks){_, position ->
                        Toast.makeText(this, audioTrack[position] + " Selected", Toast.LENGTH_SHORT).show()
                        trackSelector.setParameters(trackSelector.buildUponParameters().setPreferredAudioLanguage(audioTrack[position]))

                    }
                    .create()
                    .show()
            }

            bindingMF.subtitleBtn.setOnClickListener {
                if(isSubtitle) {
                    trackSelector.parameters = DefaultTrackSelector.ParametersBuilder(this).setRendererDisabled(
                        C.TRACK_TYPE_VIDEO, true
                    ).build()
                    Toast.makeText(this, "Subtitles Off", Toast.LENGTH_SHORT).show()
                    isSubtitle = false
                } else {
                    trackSelector.parameters = DefaultTrackSelector.ParametersBuilder(this).setRendererDisabled(
                        C.TRACK_TYPE_VIDEO, false
                    ).build()
                    Toast.makeText(this, "Subtitles On", Toast.LENGTH_SHORT).show()
                    isSubtitle = true

                }
                dialog.dismiss()
                playVideo()
            }
            bindingMF.audioEnhanceBtn.setOnClickListener {

                Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

            bindingMF.playbackSpeedBtn.setOnClickListener {
                dialog.dismiss()
                playVideo()
                val customDialogPlaybackSpeed = LayoutInflater.from(this).inflate(R.layout.playback_speed_dialog, binding.root, false)
                val bindingPlaybackSpeed = PlaybackSpeedDialogBinding.bind((customDialogPlaybackSpeed))
                val dialogPlaybackSpeed = MaterialAlertDialogBuilder(this).setView(customDialogPlaybackSpeed)
                    .setCancelable(false)
                    .setPositiveButton("OK") {self, _ ->
                        self.dismiss()

                    }
                    .setBackground(ColorDrawable(0x00090809))
                    .create()
                dialogPlaybackSpeed.show()
                bindingPlaybackSpeed.playbackSpeedText.text = "${DecimalFormat("#.##").format(speed)} x"
                bindingPlaybackSpeed.playbackMinusBtn.setOnClickListener {
                    changeSpeed(isIncrement = false)
                    bindingPlaybackSpeed.playbackSpeedText.text = "${DecimalFormat("#.##").format(speed)} x"
                }
                bindingPlaybackSpeed.playbackPlusBtn.setOnClickListener {
                    changeSpeed(isIncrement = true)
                    bindingPlaybackSpeed.playbackSpeedText.text = "${DecimalFormat("#.##").format(speed)} x"
                }
            }

            bindingMF.pipModeBtn.setOnClickListener {
                val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                val status = appOps.checkOpNoThrow(AppOpsManager.OPSTR_PICTURE_IN_PICTURE, android.os.Process.myUid(), packageName) == AppOpsManager.MODE_ALLOWED
                if(status) {
                    this.enterPictureInPictureMode(PictureInPictureParams.Builder().build())
                    dialog.dismiss()
                    binding.playerView.hideController()
                    playVideo()
                    pipStatus = 0
                } else {
                    val intent = Intent("android.settings.PICTURE_IN_PICTURE_SETTINGS", Uri.parse("package: $packageName"))
                    startActivity(intent)
                }
            }
        }
    }





    private fun createPlayer() {
        try {
            player.release()
        } catch (e: Exception) {}
        trackSelector = DefaultTrackSelector(this)
        speed = 1.0f
        binding.videoTitle.text = playerList[position].title
        binding.videoTitle.isSelected = true
        player = ExoPlayer.Builder(this).setTrackSelector(trackSelector).build()
        binding.playerView.player = player
        val mediaItem = MediaItem.fromUri(playerList[position].artUri)
        player.setMediaItem(mediaItem)
        player.prepare()
        playVideo()
        player.addListener(object : Player.Listener{
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                //if the video ended
                if(playbackState == Player.STATE_ENDED) nextPrevVideo()
            }
        })
        playInFullscreen(isFullScreen)
        setVisibility()

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
        if(!repeat) {
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

    }

    private fun playInFullscreen(enable: Boolean) {
        if(enable) {
            binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            binding.fullScreenBtn.setImageResource(R.drawable.fullscreen_exit_icon)
        }else {
            binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            binding.fullScreenBtn.setImageResource(R.drawable.fullscreen_icon)
        }
    }

    private fun setVisibility() {
        runnable = Runnable {
            if(binding.playerView.isControllerVisible) changeVisibility(View.VISIBLE)
            else changeVisibility(View.INVISIBLE)
            Handler(Looper.getMainLooper()).postDelayed(runnable, 300)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }

    private fun changeVisibility(visibility: Int) {
        binding.topController.visibility = visibility
        binding.bottomController.visibility = visibility
        if(isLocked) binding.lockBtn.visibility = View.VISIBLE
        else binding.lockBtn.visibility = visibility
        binding.backwardBtn.visibility = View.GONE
        binding.forwardBtn.visibility = View.GONE

    }

    private fun changeSpeed(isIncrement: Boolean) {
        if(isIncrement) {
            if(speed <= 2.9f) {
                speed += 0.10f
            }
        } else {
            if(speed > 0.20f) {
                speed -= 0.10f
            }
        }
        player.setPlaybackSpeed(speed)
    }


    @SuppressLint("MissingSuperCall")
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        if(pipStatus != 0) {
            finish()
            val intent = Intent(this, PlayerActivity::class.java)
            when(pipStatus) {
                1 -> intent.putExtra("class", "FolderActivity")
                2 -> intent.putExtra("class", "AllVideos")
            }
            startActivity(intent)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}

