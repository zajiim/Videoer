package com.neon.videoer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.neon.videoer.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {
    lateinit var binding: ActivityPlayerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)

        setContentView(binding.root)
    }
}