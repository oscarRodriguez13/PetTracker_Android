package com.example.pettracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.pettracker.domain.HistorialAdapter
import com.example.pettracker.domain.HistorialItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

class HistorialActivity : AppCompatActivity() {

    private lateinit var mlista: ListView
    private lateinit var mHistorialAdapter: HistorialAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        setupListView()
        setupButtonPaseos()
        setupSettingsButton()
    }

    private fun setupListView() {
        mlista = findViewById(R.id.historial)
        val historialList = parseJSON(loadJSONFromAsset()) ?: listOf()
        mHistorialAdapter = HistorialAdapter(this, historialList)
        mlista.adapter = mHistorialAdapter

        mlista.setOnItemClickListener { _, _, position, _ ->
            val selectedPaseo = historialList[position]
            val paseoBundle = createBundle(selectedPaseo)
            startDetailsActivity(paseoBundle)
        }
    }

    private fun setupButtonPaseos() {
        findViewById<Button>(R.id.buttonOption1).setOnClickListener {
            startNewActivity(HomeActivity::class.java)
        }
    }

    private fun setupSettingsButton() {
        findViewById<Button>(R.id.buttonOption3).setOnClickListener {
            startNewActivity(SettingsActivity::class.java)
        }
    }

    private fun createBundle(selectedPaseo: HistorialItem): Bundle {
        return Bundle().apply {
            putString("nombreMascota", selectedPaseo.nombreMascota)
            putString("fecha", selectedPaseo.fecha)
            putString("nombrePaseador", selectedPaseo.nombrePaseador)
            putString("hora_inicial", selectedPaseo.hora_inicial)
            putString("hora_final", selectedPaseo.hora_final)
            putString("precio", selectedPaseo.precio)
            putInt("calificacion", selectedPaseo.calificacion)
            putString("comentario", selectedPaseo.comentario)
        }
    }

    private fun startDetailsActivity(paseoBundle: Bundle) {
        val intent = Intent(this@HistorialActivity, DetallesHistorialActivity::class.java).apply {
            putExtras(paseoBundle)
        }
        startActivity(intent)
    }

    private fun startNewActivity(activityClass: Class<*>) {
        val intent = Intent(applicationContext, activityClass)
        startActivity(intent)
    }

    private fun loadJSONFromAsset(): String? {
        return try {
            assets.open("historial.json").use { inputStream ->
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                String(buffer, Charsets.UTF_8)
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }
    }

    private fun parseJSON(jsonString: String?): List<HistorialItem>? {
        return jsonString?.let {
            Gson().fromJson(it, object : TypeToken<List<HistorialItem>>() {}.type)
        }
    }
}
