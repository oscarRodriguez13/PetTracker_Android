package com.example.pettracker.generalActivities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import com.example.pettracker.R

class AccountTypeActivity : AppCompatActivity() {

    //hola
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
            finish()
        }

        val accountTypeWalker = findViewById<ImageButton>(R.id.dogWalkerButton)
        accountTypeWalker.setOnClickListener {
            val intent = Intent(
                applicationContext,
                RegisterWalkerActivity::class.java
            )
            startActivity(intent)
            finish()
        }
    }
}