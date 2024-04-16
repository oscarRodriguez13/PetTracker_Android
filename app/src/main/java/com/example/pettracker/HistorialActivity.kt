package com.example.pettracker

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.widget.ListView
import com.example.pettracker.domain.HistorialAdapter
import com.example.pettracker.domain.HistorialItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

class HistorialActivity : AppCompatActivity() {

    private var mlista: ListView? = null
    private var mHistorialAdapter: HistorialAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        // Inicialización de la ListView
        mlista = findViewById(R.id.historial)

        // Cargar JSON desde los assets
        val jsonString = loadJSONFromAsset()

        // Parsear el JSON a una lista de objetos HistorialItem
        val historialList = parseJSON(jsonString) ?: listOf()

        // Inicialización del adaptador con la lista parseada
        mHistorialAdapter = HistorialAdapter(this, historialList)
        mlista?.adapter = mHistorialAdapter

        mlista?.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            // Obtener el historial de paseo seleccionado
            val selectedPaseo = historialList[position]

            // Crear un Bundle para almacenar los datos del paseo
            val paseoBundle = Bundle().apply {
                putString("nombreMascota", selectedPaseo.nombreMascota)
                putString("fecha", selectedPaseo.fecha)
                putString("nombrePaseador", selectedPaseo.nombrePaseador)
                putString("hora_inicial", selectedPaseo.hora_inicial)
                putString("hora_final", selectedPaseo.hora_final)
                putString("precio", selectedPaseo.precio)
                putInt("calificacion", selectedPaseo.calificacion)
                putString("comentario", selectedPaseo.comentario)
            }

            // Crear un Intent para enviar la información a la actividad de detalles
            val intent = Intent(this@HistorialActivity, DetallesHistorialActivity::class.java).apply {
                putExtras(paseoBundle)
            }

            // Iniciar la actividad de detalles
            startActivity(intent)
        }


        val buttonPaseos = findViewById<Button>(R.id.buttonOption1)
        buttonPaseos.setOnClickListener {
            val intent = Intent(
                applicationContext,
                HomeActivity::class.java
            )
            startActivity(intent)
        }

        val settingsButton = findViewById<Button>(R.id.buttonOption3)
        settingsButton.setOnClickListener {
            val intent = Intent(
                applicationContext,
                SettingsActivity::class.java
            )
            startActivity(intent)
        }
    }

    private fun loadJSONFromAsset(): String? {
        return try {
            val inputStream = assets.open("historial.json")
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

    private fun parseJSON(jsonString: String?): List<HistorialItem>? {
        return jsonString?.let {
            val gson = Gson()
            val type = object : TypeToken<List<HistorialItem>>() {}.type
            gson.fromJson<List<HistorialItem>>(it, type)
        }
    }
}
