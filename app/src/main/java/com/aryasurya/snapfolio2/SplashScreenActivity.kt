package com.aryasurya.snapfolio2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import com.aryasurya.snapfolio2.splashscreen.SplashActivity

class SplashScreenActivity : AppCompatActivity() {
    private val SPLASH_DELAY: Long = 1000 // Durasi tampilan splash screen dalam milidetik (misalnya 3000 ms = 3 detik)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // Mengatur tampilan menjadi fullscreen jika diinginkan
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        // Menjalankan timer untuk pindah ke activity selanjutnya setelah tampilan splash screen selesai
        Handler().postDelayed({
            // Pindah ke activity selanjutnya (misalnya MainActivity)
            val intent = Intent(this@SplashScreenActivity, SplashActivity::class.java)
            startActivity(intent)
            finish()
        }, SPLASH_DELAY)
    }
}

