package com.example.pettracker.walkerActivities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pettracker.R
import com.example.pettracker.adapter.ProgramarPaseoAdapter
import com.example.pettracker.apis.NominatimService
import com.example.pettracker.domain.ProgramarPaseoItem
import com.example.pettracker.walkerActivities.HistorialWalkerActivity
import com.example.pettracker.walkerActivities.PerfilDuenhoActivity
import com.example.pettracker.walkerActivities.SettingsWalkerActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

class ProgramarPaseoActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var uidDueño:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_programar_paseo)
        setupBarraHerramientas()

        auth = Firebase.auth
        database = FirebaseDatabase.getInstance().reference.child("SolicitudesPaseo")

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val profiles = mutableListOf<ProgramarPaseoItem>()

        val adapter = ProgramarPaseoAdapter(profiles) { programarPaseoItem ->
            abrirDetallePerfil(programarPaseoItem)
        }
        recyclerView.adapter = adapter

        // Establecer el LinearLayoutManager para vertical
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        // Obtener y mostrar datos de Firebase
        obtenerDatosFirebase(adapter)
    }

    private fun obtenerDatosFirebase(adapter: ProgramarPaseoAdapter) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profiles = mutableListOf<ProgramarPaseoItem>()
                for (childSnapshot in snapshot.children) {
                    val estado = childSnapshot.child("estado").getValue(String::class.java)
                    if (estado == "no iniciado") {

                        var duracionHoras: Double = 0.0
                        val horaInicio = childSnapshot.child("horaInicio").getValue(String::class.java)
                        val horaFin = childSnapshot.child("horaFin").getValue(String::class.java)
                        if (horaInicio != null && horaFin != null) {
                            duracionHoras = calcularDuracionHora(horaInicio, horaFin)
                        }

                        val petIdsSnapshot = childSnapshot.child("petIds")
                        val cantidad = petIdsSnapshot.childrenCount.toString()

                        uidDueño = childSnapshot.child("uidDueño").getValue(String::class.java)!!

                        if (uidDueño != null) {
                            obtenerNombreYFotoPorUID(uidDueño) { nombre, fotoUrl ->
                                val profile = ProgramarPaseoItem(fotoUrl ?: "", nombre ?: "", "Duración: $duracionHoras hrs", "Mascotas: $cantidad")
                                profiles.add(profile)
                                adapter.actualizarLista(profiles)
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun obtenerNombreYFotoPorUID(uid: String, callback: (String?, String?) -> Unit) {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Usuarios")
        usersRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nombre = snapshot.child("nombre").getValue(String::class.java)
                val fotoRef = FirebaseStorage.getInstance().reference.child("Usuarios").child(uid).child("profile")
                fotoRef.downloadUrl.addOnSuccessListener { uri ->
                    callback(nombre, uri.toString())
                }.addOnFailureListener {
                    callback(nombre, null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null, null)
            }
        })
    }
    private fun setupBarraHerramientas() {
        val historialButton = findViewById<Button>(R.id.buttonOption2)
        historialButton.setOnClickListener {
            val intent = Intent(
                this@ProgramarPaseoActivity,
                HistorialWalkerActivity::class.java
            )
            startActivity(intent)
        }

        val settingsButton = findViewById<Button>(R.id.buttonOption3)
        settingsButton.setOnClickListener {
            val intent = Intent(
                applicationContext,
                SettingsWalkerActivity::class.java
            )
            startActivity(intent)
        }
    }

    private fun abrirDetallePerfil(profile: ProgramarPaseoItem) {
        val intent = Intent(this, PerfilDuenhoActivity::class.java)
        intent.putExtra("uid", uidDueño)
        println("UID $uidDueño")
        startActivity(intent)
    }




    private fun calcularDuracionHora(horaInicio: String, horaFin: String): Double {
        val formato = SimpleDateFormat("HH:mm", Locale.getDefault())
        val horaInicial = formato.parse(horaInicio)
        val horaFinal = formato.parse(horaFin)

        val diferenciaMillis = abs(horaFinal.time - horaInicial.time)

        val duracionHoras = diferenciaMillis.toDouble() / (1000 * 60 * 60)

        return duracionHoras
    }


}