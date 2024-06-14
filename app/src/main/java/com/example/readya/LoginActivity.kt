package com.example.readya
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Ajustar padding para las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configurar el TextView con SpannableString
        val appNameTextView = findViewById<TextView>(R.id.appName)
        val text = "Readya"
        val spannableString = SpannableString(text)

        // Establecer "Readya" en bold
        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Cambiar el color de "ya" a naranja
        val orangeColor: Int = Color.parseColor("#FFA500")
        spannableString.setSpan(
            ForegroundColorSpan(orangeColor),
            4,
            text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        appNameTextView.text = spannableString
    }
}