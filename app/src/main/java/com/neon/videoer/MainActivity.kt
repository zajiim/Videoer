package com.neon.videoer

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle

import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.neon.videoer.databinding.ActivityMainBinding
import com.neon.videoer.models.Folder
import com.neon.videoer.models.Video
import java.io.File
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle

    companion object {
        lateinit var videoList: ArrayList<Video>
        lateinit var foldersList: ArrayList<Folder>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setTheme(R.style.customBlueNav)
        setContentView(binding.root)

        toggle = ActionBarDrawerToggle(this, binding.root, R.string.open, R.string.close)
        binding.root.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if(requestStoragePermission()) {
            foldersList = ArrayList()
            videoList = getAllVideos()
            setFragment(VideosFragment())
        }



        //Bottom Nav ClickListener
        binding.bottomNav.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.videoView -> setFragment(VideosFragment())
                R.id.folderView -> setFragment(FoldersFragment())
            }
            return@setOnItemSelectedListener true
        }

        //Side NavBar ClickListener
        binding.navView.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.feedbackNav -> Toast.makeText(this, "Feedback", Toast.LENGTH_SHORT).show()
                R.id.themesNav -> Toast.makeText(this, "Themes", Toast.LENGTH_SHORT).show()
                R.id.sortOrderNav -> Toast.makeText(this, "Sort", Toast.LENGTH_SHORT).show()
                R.id.aboutNav -> Toast.makeText(this, "About", Toast.LENGTH_SHORT).show()
                R.id.exitNav -> exitProcess(1)
            }
            return@setNavigationItemSelectedListener true
        }

    }

    // Function for selecting fragments
    private fun setFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentFL, fragment)
        transaction.disallowAddToBackStack()
        transaction.commit()
    }

    //Requesting permission
    private fun requestStoragePermission(): Boolean {
        if(ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE),7)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 7) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                foldersList = ArrayList()
                videoList = getAllVideos()
                setFragment(VideosFragment())

            } else {
                ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE),7)
            }

        }



    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)

    }






    @SuppressLint("Range")
    private fun getAllVideos(): ArrayList<Video>{
        val tempList = ArrayList<Video>()
        val tempFolderList = ArrayList<String>()
        val projection = arrayOf(MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.BUCKET_ID,
        )
        val cursor = this.contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null,
            "${MediaStore.Video.Media.DATE_ADDED} DESC")
        if(cursor != null)
            if(cursor.moveToNext())
                do {

                    val titleC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE))
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID))
                    val folderNameC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))
                    val folderIdC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID))
                    val sizeC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE))
                    val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
                    val durationC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION))

                    try {
                        val file = File(pathC)
                        val artUriC = Uri.fromFile(file)
                        val video = Video(title = titleC,
                            id = idC,
                            folderName = folderNameC,
                            duration = durationC,
                            size = sizeC,
                            path = pathC,
                            artUri = artUriC)
                        //Adding videos to VideoFragment
                        if(file.exists()) tempList.add(video)
                        //Adding folders to FolderFragment
                        if(!tempFolderList.contains(folderNameC)) {
                            tempFolderList.add(folderNameC)
                            foldersList.add(Folder(id = folderIdC, folderName = folderNameC))
                        }
                    }catch (e:Exception){
                        Log.e("ERROR", e.message?:"")

                    }
                }while (cursor.moveToNext())
        cursor?.close()
        return tempList
    }






}