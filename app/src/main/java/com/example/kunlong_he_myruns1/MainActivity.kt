package com.example.kunlong_he_myruns1

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)        

        val intent = Intent(this, UserProfileActivity::class.java)
        startActivity(intent)
        finish()
    }
}