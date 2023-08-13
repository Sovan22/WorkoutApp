package com.example.a7minuteworkout

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.a7minuteworkout.databinding.ActivityOnFinishBinding

class onFinish : AppCompatActivity() {

    private var binding: ActivityOnFinishBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOnFinishBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.finishButton?.setOnClickListener {
            finish()
            val intent = Intent(this@onFinish,MainActivity::class.java )
            startActivity(intent)
        }


    }
    override fun onDestroy() {
        super.onDestroy()

        binding = null
    }
}