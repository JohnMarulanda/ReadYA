package com.example.readya

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
            startActivity(Intent(this, MainActivity::class.java))
        Handler().postDelayed(Runnable {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2000) //2 Segundos de delay
    }
}