package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get reference to textView3
        val textView3: TextView = findViewById(R.id.textView3)

        // Get current date and format it
        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        // Set formatted date to textView3
        textView3.text = currentDate

        // Get reference to startButton
        val startButton: Button = findViewById(R.id.startButton)

        // Set OnClickListener to startButton
        startButton.setOnClickListener {
            // Create intent to start the desired activity (replace SecondActivity::class.java with your actual activity)
            val intent = Intent(this@Home, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
