package com.neon.videoer

import android.annotation.SuppressLint
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import com.neon.videoer.databinding.ActivityFoldersBinding
import com.neon.videoer.databinding.FragmentFoldersBinding
import com.neon.videoer.models.Folder
import com.neon.videoer.models.Video
import java.io.File

class FoldersActivity : AppCompatActivity() {

    companion object {

        lateinit var currentFolderVideos: ArrayList<Video>
    }


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityFoldersBinding.inflate(layoutInflater)
        setTheme(R.style.customBlueNav)
        setContentView(binding.root)
        val position = intent.getIntExtra("position", 0)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = MainActivity.foldersList[position].folderName
        currentFolderVideos = getAllVideos(MainActivity.foldersList[position].id)
        binding.videoRVFA.setHasFixedSize(true)
        binding.videoRVFA.setItemViewCacheSize(10)
        binding.videoRVFA.layoutManager = LinearLayoutManager(this)
        binding.videoRVFA.adapter = VideoAdapter(this, currentFolderVideos, isFolder = true)
        binding.totalVideosFA.text = "Total Videos: ${currentFolderVideos.size}"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }


    @SuppressLint("Range")
    private fun getAllVideos(folderId: String): ArrayList<Video> {
        val tempList = ArrayList<Video>()
        val selection = MediaStore.Video.Media.BUCKET_ID + " like? "
        val projection = arrayOf(
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.BUCKET_ID,
        )
        val cursor = this.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, selection, arrayOf(folderId),
            "${MediaStore.Video.Media.DATE_ADDED} DESC"
        )
        if (cursor != null)
            if (cursor.moveToNext())
                do {

                    val titleC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE))
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID))
                    val folderNameC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))

                    val sizeC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE))
                    val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
                    val durationC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION))

                    try {
                        val file = File(pathC)
                        val artUriC = Uri.fromFile(file)
                        val video = Video(
                            title = titleC,
                            id = idC,
                            folderName = folderNameC,
                            duration = durationC,
                            size = sizeC,
                            path = pathC,
                            artUri = artUriC
                        )
                        //Adding videos to VideoFragment
                        if (file.exists()) tempList.add(video)

                    } catch (e: Exception) {
                        Log.e("ERROR", e.message ?: "")

                    }
                } while (cursor.moveToNext())
        cursor?.close()
        return tempList
    }
}