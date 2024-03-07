package com.example.pettracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

class AccountTypeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_type)

        val accountTypeUser = findViewById<ImageButton>(R.id.userButton)
        accountTypeUser.setOnClickListener {
            val intent = Intent(
                applicationContext,
                RegisterUserActivity::class.java
            )
            startActivity(intent)
        }

        val accountTypeWalker = findViewById<ImageButton>(R.id.dogWalkerButton)
        accountTypeWalker.setOnClickListener {
            val intent = Intent(
                applicationContext,
                RegisterWalkerActivity::class.java
            )
            startActivity(intent)
        }
    }
}