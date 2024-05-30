package com.example.pettracker.customerActivities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.pettracker.R
import com.example.pettracker.domain.Datos
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.osmdroid.api.IMapController
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.TilesOverlay

class SeguimientoPaseoActivity : AppCompatActivity(), SensorEventListener, LocationListener {

    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private var sensorManager: SensorManager? = null
    private lateinit var locationManager: LocationManager
    private var lightSensor: Sensor? = null
    private var marker: Marker? = null
    private var mGeocoder: Geocoder? = null
    private var geoPoint: GeoPoint? = null
    private lateinit var roadManager: RoadManager
    private lateinit var osmMap: MapView
    private var isFirstLocationUpdate = true
    private var paseadorMarker: Marker? = null
    private var solicitudId: String? = null
    private var uidPaseador: String? = null
    private lateinit var databaseReference: DatabaseReference
    private var userId: String? = null
    private lateinit var nombreDuenhoTextView: TextView
    private lateinit var horaFinalTextView: TextView
    private lateinit var cantidadMascotasTextView: TextView

    private var solicitudStateListener: ValueEventListener? = null
    private var solicitudRef: DatabaseReference? = null


    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seguimiento_paseo)

        solicitudId = intent.getStringExtra("solicitudId")
        uidPaseador = intent.getStringExtra("uidPaseador")

        // Obtén el userId del usuario autenticado
        val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        userId = user?.uid

        databaseReference = FirebaseDatabase.getInstance().getReference("Usuarios")

        nombreDuenhoTextView = findViewById(R.id.nombre_duenho)
        horaFinalTextView = findViewById(R.id.hora_final)
        cantidadMascotasTextView = findViewById(R.id.cantidad_mascotas)

        Configuration.getInstance().userAgentValue = applicationContext.packageName

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        roadManager = OSRMRoadManager(this, "ANDROID")

        handlePermissions()

        osmMap = findViewById(R.id.osmMap)
        osmMap.setTileSource(TileSourceFactory.MAPNIK)
        osmMap.setMultiTouchControls(true)

        mGeocoder = Geocoder(baseContext)

        val centerButton = findViewById<ImageButton>(R.id.centerButton)

        centerButton.setOnClickListener {
            centerCameraOnUser()
        }

        uidPaseador?.let { addPaseadorMarkerFromDatabase(it) }

        // Carga los datos de la solicitud y actualiza la UI
        solicitudId?.let { loadSolicitudData(it) }


        // Agrega el listener para el estado de la solicitud
        solicitudId?.let { addSolicitudStateListener(it) }
    }

    private fun loadSolicitudData(solicitudId: String) {
        val solicitudRef = FirebaseDatabase.getInstance().getReference("SolicitudesPaseo/$solicitudId")
        solicitudRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val uidDuenho = snapshot.child("uidDueño").getValue(String::class.java)
                val horaFin = snapshot.child("horaFin").getValue(String::class.java)
                val petIds = snapshot.child("petIds").children

                uidDuenho?.let { loadDuenhoData(it) }
                horaFin?.let { horaFinalTextView.text = "Hora final: " + it }

                val petCount = petIds.count()
                cantidadMascotasTextView.text = "Cantidad mascotas: $petCount"
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SeguimientoPaseoActivity, "Error al cargar los datos de la solicitud: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadDuenhoData(uidDuenho: String) {
        val duenhoRef = FirebaseDatabase.getInstance().getReference("Usuarios/$uidDuenho")
        duenhoRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nombreDuenho = snapshot.child("nombre").getValue(String::class.java)
                nombreDuenho?.let { nombreDuenhoTextView.text = it }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SeguimientoPaseoActivity, "Error al cargar los datos del dueño: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun handlePermissions() {
        when {
            ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                mFusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                    onLocationChanged(location)
                }
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION) -> {
                requestPermissions(
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    Datos.MY_PERMISSION_REQUEST_LOCATION
                )
            }
            else -> {
                requestPermissions(
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    Datos.MY_PERMISSION_REQUEST_LOCATION
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        sensorManager?.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10f, this)
        super.onResume()
        osmMap.onResume()
        geoPoint?.let {
            val mapController: IMapController = osmMap.controller
            mapController.setZoom(15.0)
            mapController.setCenter(it)
        }
    }

    override fun onPause() {
        super.onPause()
        osmMap.onPause()
        sensorManager?.unregisterListener(this)
        locationManager.removeUpdates(this)
    }

    private fun addPaseadorMarkerFromDatabase(uidPaseador: String) {
        val database = FirebaseDatabase.getInstance().getReference("Usuarios/$uidPaseador")
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val latitud = (snapshot.child("latitud").getValue(String::class.java))?.toDouble()
                val longitud = (snapshot.child("longitud").getValue(String::class.java))?.toDouble()

                if (latitud != null && longitud != null) {
                    val paseadorGeoPoint = GeoPoint(latitud, longitud)

                    // Elimina el marcador anterior si existe
                    paseadorMarker?.let { osmMap.overlays.remove(it) }

                    // Crea un nuevo marcador para el paseador
                    paseadorMarker = Marker(osmMap)
                    paseadorMarker?.position = paseadorGeoPoint
                    paseadorMarker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    paseadorMarker?.title = "Paseador(a): ${nombreDuenhoTextView.text}"

                    // Obtener el drawable de la imagen personalizada
                    val customMarkerDrawable = ContextCompat.getDrawable(this@SeguimientoPaseoActivity, R.drawable.icn_marcador_paseador)

                    // Escalar la imagen al tamaño predeterminado (48x48 píxeles)
                    val width = 48
                    val height = 48
                    val scaledDrawable = Bitmap.createScaledBitmap(
                        (customMarkerDrawable as BitmapDrawable).bitmap,
                        width,
                        height,
                        false
                    )

                    // Asignar la imagen escalada al marcador
                    paseadorMarker?.icon = BitmapDrawable(resources, scaledDrawable)

                    // Agrega el marcador del paseador al mapa
                    osmMap.overlays.add(paseadorMarker)
                    osmMap.invalidate()
                } else {
                    Toast.makeText(this@SeguimientoPaseoActivity, "No se pudo obtener la ubicación del paseador", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@SeguimientoPaseoActivity, "Error al obtener la ubicación del paseador: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onLocationChanged(location: Location) {
        geoPoint = GeoPoint(location.latitude, location.longitude)
        val mapController: IMapController = osmMap.controller
        mapController.setCenter(geoPoint)
        mapController.setZoom(20.0)

        if (marker == null) {
            marker = Marker(osmMap)
            osmMap.overlays.add(marker)
            marker?.position = geoPoint
            marker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker?.title = "Tú"
        } else {
            marker?.position = geoPoint
        }
        osmMap.invalidate()

        // Agregar el marcador aleatorio solo la primera vez que se obtiene la ubicación
        if (isFirstLocationUpdate) {
            addPaseadorMarkerFromDatabase(uidPaseador!!)
            isFirstLocationUpdate = false
        }

        // Actualiza la ubicación en Firebase
        updateLocationInFirebase(location)
    }

    private fun updateLocationInFirebase(location: Location) {
        userId?.let {
            val userLocation = mapOf(
                "latitud" to location.latitude.toString(),
                "longitud" to location.longitude.toString()
            )

            databaseReference.child(it).updateChildren(userLocation).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Ubicación actualizada en Firebase", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al actualizar la ubicación en Firebase", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun centerCameraOnUser() {
        marker?.let {
            val mapController: IMapController = osmMap.controller
            mapController.setCenter(it.position)
            mapController.setZoom(15.0)
        } ?: run {
            Toast.makeText(this, "Ubicación no disponible", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (event.sensor.type == Sensor.TYPE_LIGHT) {
                val lux = event.values[0]
                if (lux < 30) {
                    osmMap.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
                } else {
                    osmMap.overlayManager.tilesOverlay.setColorFilter(null)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No implementation needed
    }

    private fun addSolicitudStateListener(solicitudId: String) {
        solicitudRef = FirebaseDatabase.getInstance().getReference("SolicitudesPaseo/$solicitudId/estado")
        solicitudStateListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val estado = snapshot.getValue(String::class.java)
                if (estado == "terminado") {
                    val intent = Intent(this@SeguimientoPaseoActivity, FinalizarPaseoActivity::class.java)
                    intent.putExtra("solicitudId", solicitudId)
                    intent.putExtra("uidPaseador", uidPaseador)
                    startActivity(intent)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SeguimientoPaseoActivity, "Error al obtener el estado de la solicitud: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        solicitudRef?.addValueEventListener(solicitudStateListener!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        solicitudRef?.let { ref ->
            solicitudStateListener?.let { listener ->
                ref.removeEventListener(listener)
            }
        }
    }
}