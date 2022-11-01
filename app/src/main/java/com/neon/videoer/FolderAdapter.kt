package com.neon.videoer

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.neon.videoer.databinding.FolderViewBinding
import com.neon.videoer.models.Folder

class FolderAdapter(private val context: Context, private var foldersList: ArrayList<Folder>): RecyclerView.Adapter<FolderAdapter.MyHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderAdapter.MyHolder {
        return MyHolder(FolderViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.folderName.text = foldersList[position].folderName
    }

    override fun getItemCount(): Int {
        return foldersList.size
    }

    class MyHolder(binding: FolderViewBinding): RecyclerView.ViewHolder(binding.root) {
        val folderName = binding.folderNameFV

    }


}