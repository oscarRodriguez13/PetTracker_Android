package com.example.pettracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class RegisterActivityUser : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        setContentView(R.layout.activity_register) // Asegúrate de usar el ID correcto de tu layout

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etNumberPets)
        val button = findViewById<Button>(R.id.button)

        button.setOnClickListener {
            if (verificarCamposLlenos(etName, etEmail, etPassword, etConfirmPassword)) {
                // Cambia a la siguiente pantalla
                val intent = Intent(this, RegisterPetDataActivity::class.java)
                val bundle = Bundle().apply {
                    putString("nombre", etName.text.toString().trim())
                    putString("email", etEmail.text.toString().trim())
                    putString("password", etPassword.text.toString().trim())
                    putString("numberPets", etConfirmPassword.text.toString().trim())
                }

                // Añadir el Bundle al Intent
                intent.putExtras(bundle)

                // Iniciar la actividad con el Intent que tiene el Bundle
                startActivity(intent)
            } else {
                // Muestra un mensaje de error o indicación
                Toast.makeText(this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun verificarCamposLlenos(vararg campos: EditText): Boolean =
        campos.all { it.text.toString().trim().isNotEmpty() }
}