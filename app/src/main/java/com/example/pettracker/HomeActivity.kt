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

        setupTimePickers()
        setupSolicitudPaseo()
        setupHistorialButton()
        setupSettingsButton()
        setupSelectOptionsButton()
    }

    private fun setupTimePickers() {
        val etHoraInicial = findViewById<EditText>(R.id.etHoraInicial)
        val etHoraFinal = findViewById<EditText>(R.id.etHoraFinal)

        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val minute = now.get(Calendar.MINUTE)

        setupTimePicker(etHoraInicial, hour, minute) { selectedTime ->
            etHoraInicial.setText("Hora inicial: $selectedTime")
        }

        setupTimePicker(etHoraFinal, hour, minute) { selectedTime ->
            etHoraFinal.setText("Hora final: $selectedTime")
        }
    }

    private fun setupTimePicker(editText: EditText, hour: Int, minute: Int, onTimeSet: (String) -> Unit) {
        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val selectedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
                onTimeSet(selectedTime)
            },
            hour,
            minute,
            true
        )

        editText.setOnClickListener {
            timePickerDialog.show()
        }
    }

    private fun setupSolicitudPaseo() {
        val btnSolicitudPaseo = findViewById<Button>(R.id.btn_solicitud_paseo)

        btnSolicitudPaseo.setOnClickListener {
            val etHoraInicial = findViewById<EditText>(R.id.etHoraInicial)
            val etHoraFinal = findViewById<EditText>(R.id.etHoraFinal)

            if (verificarCamposLlenos(etHoraInicial, etHoraFinal)) {
                val intent = Intent(this, SolicitarPaseoActivity::class.java).apply {
                    putExtras(Bundle().apply {
                        putString("duracion", etHoraInicial.text.toString().trim())
                        putString("duracion", etHoraFinal.text.toString().trim())
                    })
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupHistorialButton() {
        findViewById<Button>(R.id.buttonOption2).setOnClickListener {
            startActivity(Intent(applicationContext, HistorialActivity::class.java))
        }
    }

    private fun setupSettingsButton() {
        findViewById<Button>(R.id.buttonOption3).setOnClickListener {
            startActivity(Intent(applicationContext, SettingsActivity::class.java).apply {
                putExtra("EMAIL", userEmail)
            })
        }
    }

    private fun setupSelectOptionsButton() {
        val tvOption = findViewById<TextView>(R.id.tv_option)
        findViewById<Button>(R.id.button_options).setOnClickListener {
            showOptionsDialog(tvOption)
        }
    }

    private fun showOptionsDialog(tvOption: TextView) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Seleccione las opciones")

        val options = arrayOf("Tony", "Alaska", "Firulais")
        val selectedOptions = booleanArrayOf(false, false, false)

        builder.setMultiChoiceItems(options, selectedOptions) { _, which, isChecked ->
            selectedOptions[which] = isChecked
        }

        builder.setPositiveButton("Aceptar") { _, _ ->
            val selectedItemsText = options.indices
                .filter { selectedOptions[it] }
                .joinToString { options[it] }

            tvOption.text = "Mascotas seleccionadas: $selectedItemsText"
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }

    private fun verificarCamposLlenos(vararg campos: EditText) = campos.all { it.text.toString().trim().isNotEmpty() }
}
