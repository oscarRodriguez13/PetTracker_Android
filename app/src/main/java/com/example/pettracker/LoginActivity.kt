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

        val tvPasswordRecover = findViewById<TextView>(R.id.tv_passwordRecover)
        tvPasswordRecover.setOnClickListener {
            val intent = Intent(applicationContext, PasswordRecoveryActivity::class.java)
            startActivity(intent)
        }

        val emailEditText = findViewById<EditText>(R.id.email_input)
        val passwordEditText = findViewById<EditText>(R.id.password_input)

        val loginButton = findViewById<Button>(R.id.login_button)
        loginButton.setOnClickListener {
            if (validateUser(emailEditText.text.toString(), passwordEditText.text.toString())) {
                val intent = Intent(applicationContext, HomeActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Email o contraseña incorrectos", Toast.LENGTH_SHORT).show()
            }
        }

        val registerButton = findViewById<Button>(R.id.register_button)
        registerButton.setOnClickListener {
            val intent = Intent(applicationContext, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validateUser(email: String, password: String): Boolean {
        val json = loadJSONFromAsset("usuarios.json")
        if (json != null) {
            val jsonObject = JSONObject(json)
            val usuariosArray = jsonObject.getJSONArray("usuarios")
            for (i in 0 until usuariosArray.length()) {
                val user = usuariosArray.getJSONObject(i)
                if (user.getString("email") == email && user.getString("password") == password) {
                    return true
                }
            }
        }
        return false
    }

    private fun loadJSONFromAsset(filename: String): String? {
        return try {
            val inputStream = assets.open(filename)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
    }
}
