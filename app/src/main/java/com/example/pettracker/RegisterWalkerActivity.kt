package com.example.pettracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.Writer

class RegisterWalkerActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etExperience: EditText
    private lateinit var buttonRegister: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_walker)

        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etExperience = findViewById(R.id.etExperience)
        buttonRegister = findViewById(R.id.button)

        buttonRegister.setOnClickListener {
            writeJSONObject()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun writeJSONObject() {
        val usuariosArray = readJSONObject()

        // Crear el objeto de usuario con sus datos
        val userObject = JSONObject().apply {
            put("nombre", etName.text.toString())
            put("email", etEmail.text.toString())
            put("password", etPassword.text.toString())
            put("tipoUsuario", "2") // Puedes definir el tipo de usuario como quieras
        }

        println("NAME $etName")
        println("email $etEmail")
        println("pass $etPassword")




        // Añadir el usuario al array de usuarios
        usuariosArray.put(userObject)

        var output: Writer?
        val filename = "usuarios.json"

        try {
            val file = File(baseContext.getExternalFilesDir(null), filename)
            Log.i("USER", "Ubicacion de archivo: $file")
            output = BufferedWriter(FileWriter(file))
            output.write("{\"usuarios\": $usuariosArray}")
            output.close()
            Toast.makeText(applicationContext, "Usuario guardado", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("USER", "Error al guardar el usuario: ${e.message}", e)
        }
    }

    private fun readJSONObject(): JSONArray {
        var jsonArray = JSONArray()

        val filename = "usuarios.json"
        try {
            val file = File(baseContext.getExternalFilesDir(null), filename)
            if (file.exists()) {
                val content = file.readText()
                jsonArray = JSONObject(content).getJSONArray("usuarios")
            }
        } catch (e: Exception) {
            Log.e("READ_JSON", "Error al leer el archivo JSON: ${e.message}", e)
        }

        return jsonArray
    }

}
