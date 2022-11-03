package com.neon.videoer

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.neon.videoer.databinding.FragmentFoldersBinding
import com.neon.videoer.databinding.FragmentVideosBinding


class FoldersFragment : Fragment() {


    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_folders, container, false)




        val binding = FragmentFoldersBinding.bind(view)
        binding.folderRV.setHasFixedSize(true)
        binding.folderRV.setItemViewCacheSize(10)
        binding.folderRV.layoutManager = LinearLayoutManager(requireContext())
        binding.folderRV.adapter = FolderAdapter(requireContext(), MainActivity.foldersList)
        binding.totalFolders.text = "Total Folders: ${MainActivity.foldersList.size}"
        return view
    }


}