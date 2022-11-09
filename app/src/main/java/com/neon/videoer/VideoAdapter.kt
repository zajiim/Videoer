package com.neon.videoer

import android.content.Context
import android.content.Intent
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.neon.videoer.databinding.VideoViewBinding
import com.neon.videoer.models.Video

class VideoAdapter(private val context: Context, private var videoList: ArrayList<Video>, private val isFolder: Boolean = false): RecyclerView.Adapter<VideoAdapter.MyHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(VideoViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.title.text = videoList[position].title
        holder.folder.text = videoList[position].folderName
        holder.duration.text = DateUtils.formatElapsedTime((videoList[position].duration).toLong() / 1000)
        Glide.with(context)
            .asBitmap()
            .load(videoList[position].artUri)
            .apply(RequestOptions().placeholder(R.mipmap.ic_video_player_icon).centerCrop())
            .into(holder.image)
        holder.root.setOnClickListener {
            when {
                isFolder -> {
                    PlayerActivity.pipStatus = 1
                    sendIntent(videoPosition = position, ref = "FolderActivity")
                }
                else -> {
                    PlayerActivity.pipStatus = 2
                    sendIntent(videoPosition = position, ref = "AllVideos")
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return videoList.size
    }

    private fun sendIntent(videoPosition: Int, ref: String) {
        PlayerActivity.position = videoPosition
        val intent = Intent(context, PlayerActivity::class.java)
        intent.putExtra("class", ref)
        ContextCompat.startActivity(context, intent, null)
    }




    class MyHolder(binding: VideoViewBinding): RecyclerView.ViewHolder(binding.root) {
        val title = binding.videoName
        val folder = binding.folderName
        val duration = binding.duration
        val image = binding.videoImage
        val root = binding.root

    }
}

