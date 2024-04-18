package com.example.pettracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.pettracker.domain.HistorialWalkerAdapter
import com.example.pettracker.domain.HistorialWalkerItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

class HistorialWalkerActivity : AppCompatActivity() {

    private lateinit var mlista: ListView
    private lateinit var mHistorialAdapter: HistorialWalkerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial_walker)

        setupListView()
        setupButtonPaseos()
        setupSettingsButton()
    }

    private fun setupListView() {
        mlista = findViewById(R.id.historial)
        val historialList = parseJSON(loadJSONFromAsset()) ?: listOf()
        mHistorialAdapter = HistorialWalkerAdapter(this, historialList)
        mlista.adapter = mHistorialAdapter

        mlista.setOnItemClickListener { _, _, position, _ ->
            val selectedPaseo = historialList[position]
            val paseoBundle = createBundle(selectedPaseo)
            startDetailsActivity(paseoBundle)
        }
    }

    private fun setupButtonPaseos() {
        findViewById<Button>(R.id.buttonOption1).setOnClickListener {
            startNewActivity(HomeWalkerActivity::class.java)
        }
    }

    private fun setupSettingsButton() {
        findViewById<Button>(R.id.buttonOption3).setOnClickListener {
            startNewActivity(SettingsWalkerActivity::class.java)
        }
    }

    private fun createBundle(selectedPaseo: HistorialWalkerItem): Bundle {
        return Bundle().apply {
            putString("fecha", selectedPaseo.fecha)
            putString("nombreDuenho", selectedPaseo.nombreDuenho)
            putString("hora_inicial", selectedPaseo.hora_inicial)
            putString("hora_final", selectedPaseo.hora_final)
            putString("precio", selectedPaseo.precio)
            putString("estado", selectedPaseo.estado)
        }
    }

    private fun startDetailsActivity(paseoBundle: Bundle) {
        val intent = Intent(this@HistorialWalkerActivity, DetallesHistorialWalkerActivity::class.java).apply {
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
            assets.open("historial_paseos_programados.json").use { inputStream ->
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

    private fun parseJSON(jsonString: String?): List<HistorialWalkerItem>? {
        return jsonString?.let {
            Gson().fromJson(it, object : TypeToken<List<HistorialWalkerItem>>() {}.type)
        }
    }
}
