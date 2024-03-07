package com.example.pettracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        findViewById<TextView>(R.id.tv_passwordRecover).setOnClickListener {
            startActivity(Intent(applicationContext, PasswordRecoveryActivity::class.java))
        }

        val emailEditText = findViewById<EditText>(R.id.email_input)
        val passwordEditText = findViewById<EditText>(R.id.password_input)

        findViewById<Button>(R.id.login_button).setOnClickListener {
            if (validateUser(emailEditText.text.toString(), passwordEditText.text.toString())) {
                startActivity(Intent(applicationContext, HomeActivity::class.java))
            } else {
                Toast.makeText(this, "Email o contraseña incorrectos", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.register_button).setOnClickListener {
            startActivity(Intent(applicationContext, AccountTypeActivity::class.java))
        }
    }

    private fun validateUser(email: String, password: String): Boolean {
        return try {
            val json = assets.open("usuarios.json").bufferedReader().use { it.readText() }
            val usuariosArray = JSONObject(json).getJSONArray("usuarios")
            (0 until usuariosArray.length()).any {
                val user = usuariosArray.getJSONObject(it)
                user.getString("email") == email && user.getString("password") == password
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
            false
        }
    }
}
