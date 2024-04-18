package com.example.pettracker

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pettracker.domain.HistorialAdapter
import com.example.pettracker.domain.HistorialItem
import com.example.pettracker.domain.HistorialWalkerAdapter
import com.example.pettracker.domain.HistorialWalkerItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

class HistorialWalkerActivity : AppCompatActivity() {

    private var mlista: ListView? = null
    private var mHistorialAdapter: HistorialWalkerAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial_walker)
        mlista = findViewById(R.id.historial)

        // Cargar JSON desde los assets
        val jsonString = loadJSONFromAsset()

        // Parsear el JSON a una lista de objetos HistorialItem
        val historialList = parseJSON(jsonString) ?: listOf()

        // Inicialización del adaptador con la lista parseada
        mHistorialAdapter = HistorialWalkerAdapter(this, historialList)
        mlista?.adapter = mHistorialAdapter

        mlista?.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            // Obtener el historial de paseo seleccionado
            val selectedPaseo = historialList[position]

            // Crear un Bundle para almacenar los datos del paseo
            val paseoBundle = Bundle().apply {
                putString("fecha", selectedPaseo.fecha)
                putString("nombreDuenho", selectedPaseo.nombreDuenho)
                putString("hora_inicial", selectedPaseo.hora_inicial)
                putString("hora_final", selectedPaseo.hora_final)
                putString("precio", selectedPaseo.precio)
                putString("estado", selectedPaseo.estado)
            }

            // Crear un Intent para enviar la información a la actividad de detalles
            val intent = Intent(this@HistorialWalkerActivity, DetallesHistorialWalkerActivity::class.java).apply {
                putExtras(paseoBundle)
            }

            // Iniciar la actividad de detalles
            startActivity(intent)
        }
    }
    private fun loadJSONFromAsset(): String? {
        return try {
            val inputStream = assets.open("historial_paseos_programados.json")
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

    private fun parseJSON(jsonString: String?): List<HistorialWalkerItem>? {
        return jsonString?.let {
            val gson = Gson()
            val type = object : TypeToken<List<HistorialWalkerItem>>() {}.type
            gson.fromJson<List<HistorialWalkerItem>>(it, type)
        }
    }

}