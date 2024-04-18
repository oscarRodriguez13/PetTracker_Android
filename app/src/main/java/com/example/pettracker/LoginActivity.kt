package com.example.pettracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.File
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailEditText = findViewById(R.id.email_input)
        passwordEditText = findViewById(R.id.password_input)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        findViewById<TextView>(R.id.tv_passwordRecover).setOnClickListener {
            startActivity(Intent(applicationContext, PasswordRecoveryActivity::class.java))
        }

        findViewById<Button>(R.id.login_button).setOnClickListener {
            handleLogin()
        }

        findViewById<Button>(R.id.register_button).setOnClickListener {
            startActivity(Intent(applicationContext, AccountTypeActivity::class.java))
        }
    }

    private fun handleLogin() {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        if (validateUser(email, password)) {
            // El redireccionamiento se maneja dentro de validateUser
        } else {
            Toast.makeText(this, "Email o contraseña incorrectos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateUser(email: String, password: String): Boolean {
        return try {
            val file = File(getExternalFilesDir(null), "usuarios.json")
            if (file.exists()) {
                val json = file.bufferedReader().use { it.readText() }
                val usuariosArray = JSONObject(json).getJSONArray("usuarios")
                val userList = (0 until usuariosArray.length()).map { usuariosArray.getJSONObject(it) }
                val user = userList.firstOrNull { it.getString("email") == email && it.getString("password") == password }
                if (user != null) {
                    val tipoUsuario = user.getInt("tipoUsuario")
                    handleUserType(tipoUsuario, email)
                    true // Usuario válido
                } else {
                    false // Usuario inválido
                }
            } else {
                Toast.makeText(this, "Archivo de usuarios no encontrado", Toast.LENGTH_SHORT).show()
                false // Archivo no encontrado
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
            false // Error al leer el archivo JSON
        } catch (ex: Exception) {
            ex.printStackTrace()
            false // Otros errores
        }
    }

    private fun handleUserType(tipoUsuario: Int, email: String) {
        val intent = when (tipoUsuario) {
            1 -> Intent(applicationContext, HomeActivity::class.java)
            2 -> Intent(applicationContext, HomeWalkerActivity::class.java)
            else -> {
                Toast.makeText(this, "Tipo de usuario no válido", Toast.LENGTH_SHORT).show()
                return
            }
        }
        intent.putExtra("EMAIL", email)
        emailEditText.setText("")
        passwordEditText.setText("")
        startActivity(intent)
    }
}
