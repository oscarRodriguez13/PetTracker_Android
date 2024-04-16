package com.example.pettracker

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.File
import java.util.Calendar
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private lateinit var userEmail: String

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        userEmail = intent.getStringExtra("EMAIL") ?: ""

        val etHoraInicial = findViewById<EditText>(R.id.etHoraInicial)
        val etHoraFinal = findViewById<EditText>(R.id.etHoraFinal)
        val btnSolicitudPaseo = findViewById<Button>(R.id.btn_solicitud_paseo)
        val tvOption = findViewById<TextView>(R.id.tv_option)

        // Obtener la hora actual
        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val minute = now.get(Calendar.MINUTE)

        // Crear un diálogo de selección de hora
        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                // Manejar la hora seleccionada aquí
                val selectedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
                etHoraInicial.setText("Hora inicial: " + selectedTime)
            },
            hour,
            minute,
            true // Si deseas usar formato de 24 horas
        )

        // Mostrar el diálogo de selección de hora cuando se haga clic en el EditText
        etHoraInicial.setOnClickListener {
            timePickerDialog.show()
        }


        // Crear un diálogo de selección de hora
        val timePickerDialog2 = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                // Manejar la hora seleccionada aquí
                val selectedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
                etHoraFinal.setText("Hora final: " + selectedTime)
            },
            hour,
            minute,
            true // Si deseas usar formato de 24 horas
        )

        // Mostrar el diálogo de selección de hora cuando se haga clic en el EditText
        etHoraFinal.setOnClickListener {
            timePickerDialog2.show()
        }

        btnSolicitudPaseo.setOnClickListener {
            if (verificarCamposLlenos(etHoraInicial, etHoraFinal)) {
                // Cambia a la siguiente pantalla
                val intent = Intent(this, SolicitarPaseoActivity::class.java)
                val bundle = Bundle().apply {
                    putString("duracion", etHoraInicial.text.toString().trim())
                    putString("duracion", etHoraFinal.text.toString().trim())
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

        val historialButton = findViewById<Button>(R.id.buttonOption2)
        historialButton.setOnClickListener {
            val intent = Intent(
                applicationContext,
                HistorialActivity::class.java
            )
            startActivity(intent)
        }

        val settingsButton = findViewById<Button>(R.id.buttonOption3)
        settingsButton.setOnClickListener {
            val intent = Intent(applicationContext, SettingsActivity::class.java)

            intent.putExtra("EMAIL", userEmail)

            startActivity(intent)
        }

        val selectOptionsButton = findViewById<Button>(R.id.button_options)

        selectOptionsButton.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Seleccione las opciones")

            val mascotas = getMascotasFromJson(userEmail)
            val options = mascotas.map { it.getString("nombre") }.toTypedArray()
            val selectedOptions = booleanArrayOf(false, false, false, false)

            builder.setMultiChoiceItems(options, selectedOptions) { _, which, isChecked ->
                selectedOptions[which] = isChecked
            }

            builder.setPositiveButton("Aceptar") { _, _ ->
                val selectedItemsText = options.indices
                    .filter { selectedOptions[it] }
                    .joinToString { options[it] }

                // Mostrar las opciones seleccionadas
                tvOption.text = "Mascotas seleccionadas: $selectedItemsText"
            }

            builder.setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }

            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun getMascotasFromJson(email: String): List<JSONObject> {
        try {
            val file = File(getExternalFilesDir(null), "usuarios.json")
            val json = file.bufferedReader().use { it.readText() }
            val usuariosArray = JSONObject(json).getJSONArray("usuarios")

            for (i in 0 until usuariosArray.length()) {
                val user = usuariosArray.getJSONObject(i)
                if (user.getString("email") == email) {
                    return user.getJSONArray("mascotas").let { mascotasArray ->
                        (0 until mascotasArray.length()).map { mascotasArray.getJSONObject(it) }
                    }
                }
            }
            return emptyList()
        } catch (ex: Exception) {
            ex.printStackTrace()
            return emptyList()
        }
    }


    private fun verificarCamposLlenos(vararg campos: EditText): Boolean =
        campos.all { it.text.toString().trim().isNotEmpty() }
}
