package com.neon.videoer

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.neon.videoer.databinding.VideoViewBinding
import com.neon.videoer.models.Video

class VideoAdapter(private val context: Context, private var videoList: ArrayList<Video>): RecyclerView.Adapter<VideoAdapter.MyHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(VideoViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.title.text = videoList[position].title
        holder.folder.text = videoList[position].folderName
//        holder.duration.text = DateUtils.formatElapsedTime(videoList[position].duration / 1000)
    }

    override fun getItemCount(): Int {
        return videoList.size
    }




    class MyHolder(binding: VideoViewBinding): RecyclerView.ViewHolder(binding.root) {
        val title = binding.videoName
        val folder = binding.folderName
//        val duration = binding.duration

    }
}

