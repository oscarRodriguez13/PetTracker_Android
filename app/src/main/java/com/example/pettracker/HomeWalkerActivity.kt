package com.example.pettracker

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.pettracker.domain.Datos
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.api.IMapController
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.TilesOverlay

class HomeWalkerActivity : AppCompatActivity(){

    private lateinit var userEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_walker)


        userEmail = intent.getStringExtra("EMAIL") ?: ""


        var btnBuscarPaseo = findViewById<Button>(R.id.btnBuscarPaseo)
        btnBuscarPaseo.setOnClickListener {
            intent = Intent(this, ProgramarPaseoActivity::class.java)
            startActivity(intent)
        }

        var btnPaseosProgramados = findViewById<Button>(R.id.btnPaseosProgramados)
        btnPaseosProgramados.setOnClickListener {
            intent = Intent(this, PaseosProgramadosActivity::class.java)
            startActivity(intent)
        }

        var btnPaseosActuales = findViewById<Button>(R.id.btnPaseosActuales)
        btnPaseosActuales.setOnClickListener {
            intent = Intent(this, PaseosActualesActivity::class.java)
            startActivity(intent)
        }


        setpBarraTareas()



    }

    private fun setpBarraTareas(){
        val historialButton = findViewById<Button>(R.id.buttonOption2)
        historialButton.setOnClickListener {
            val intent = Intent(
                applicationContext,
                HistorialWalkerActivity::class.java
            )
            startActivity(intent)
        }

        val settingsButton = findViewById<Button>(R.id.buttonOption3)
        settingsButton.setOnClickListener {
            val intent = Intent(applicationContext, SettingsWalkerActivity::class.java)

            intent.putExtra("EMAIL", userEmail)

            startActivity(intent)
        }
    }



}