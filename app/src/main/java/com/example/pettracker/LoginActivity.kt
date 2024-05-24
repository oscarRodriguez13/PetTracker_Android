package com.example.pettracker

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import org.json.JSONObject
import java.io.File
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth

        emailEditText = findViewById(R.id.email_input)
        passwordEditText = findViewById(R.id.password_input)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        findViewById<TextView>(R.id.tv_passwordRecover).setOnClickListener {
            startActivity(Intent(applicationContext, PasswordRecoveryActivity::class.java))
        }

        findViewById<Button>(R.id.login_button).setOnClickListener {
            validateUser()
        }

        findViewById<Button>(R.id.register_button).setOnClickListener {
            startActivity(Intent(applicationContext, AccountTypeActivity::class.java))
        }
    }

    private fun validateUser() {
        if (validarCampos())
            auth.signInWithEmailAndPassword(
                emailEditText.text.toString(),
                passwordEditText.text.toString()
            ).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    handleUserType(userId)
                } else {
                    val snackbar = task.exception?.localizedMessage?.let {
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            it, Snackbar.LENGTH_INDEFINITE
                        )
                    }
                    snackbar?.setAction("Error al Iniciar Sesión") { snackbar.dismiss() }
                    snackbar?.show()
                }
            }
    }

    private fun validarCampos(): Boolean {
        var isValid = true

        // Validar correo electrónico
        if (emailEditText.text.toString().isEmpty() ||
            !Patterns.EMAIL_ADDRESS.matcher(emailEditText.text.toString()).matches()
        ) {
            emailEditText.error = "Correo electrónico inválido"
            isValid = false
        } else {
            emailEditText.error = null
        }

        // Validar contraseña
        if (passwordEditText.text.toString().isEmpty()) {
            passwordEditText.error = "Falta ingresar contraseña"
            isValid = false
        } else {
            passwordEditText.error = null
        }

        return isValid
    }

    private fun handleUserType(userId: String?) {
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("Usuarios").child(userId ?: "")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val tipoUsuario = dataSnapshot.child("tipoUsuario").getValue(String::class.java)
                if (tipoUsuario != null) {
                    val intent = when (tipoUsuario.toIntOrNull()) {
                        1 -> Intent(applicationContext, HomeActivity::class.java)
                        2 -> Intent(applicationContext, HomeWalkerActivity::class.java)
                        else -> {
                            Toast.makeText(this@LoginActivity, "Tipo de usuario no válido", Toast.LENGTH_SHORT).show()
                            return
                        }
                    }
                    intent.putExtra("EMAIL", emailEditText.text.toString())
                    emailEditText.setText("")
                    passwordEditText.setText("")
                    startActivity(intent)
                } else {
                    // El tipo de usuario no está definido en la base de datos
                    Toast.makeText(this@LoginActivity, "Tipo de usuario no definido", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Error al acceder a la base de datos
                Toast.makeText(this@LoginActivity, "Error al acceder a la base de datos", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
