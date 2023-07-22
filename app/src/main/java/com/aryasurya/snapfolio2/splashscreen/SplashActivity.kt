package com.aryasurya.snapfolio2.splashscreen

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.aryasurya.snapfolio2.LoginActivity
import com.aryasurya.snapfolio2.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val btnStart = findViewById<Button>(R.id.btn_start);

        btnStart.setOnClickListener{
            val toLogin = Intent(this@SplashActivity, LoginActivity::class.java)
            startActivity(toLogin)
        }
    }
}