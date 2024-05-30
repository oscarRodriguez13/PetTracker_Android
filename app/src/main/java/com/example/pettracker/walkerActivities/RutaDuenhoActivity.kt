package com.example.pettracker.walkerActivities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
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
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

class RutaDuenhoActivity : AppCompatActivity(), SensorEventListener, LocationListener {

    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private var sensorManager: SensorManager? = null
    private lateinit var locationManager: LocationManager
    private var lightSensor: Sensor? = null
    private var marker: Marker? = null
    private var mGeocoder: Geocoder? = null
    private var geoPoint: GeoPoint? = null
    private val RADIUS_OF_EARTH_KM = 6371
    private lateinit var roadManager: RoadManager
    private lateinit var osmMap: MapView
    private var isFirstLocationUpdate = true
    private var paseadorMarker: Marker? = null
    private var solicitudId: String? = null
    private var uidDuenho: String? = null
    private lateinit var databaseReference: DatabaseReference
    private var userId: String? = null

    private lateinit var profileImage: CircleImageView
    private lateinit var nombreUsuario: TextView
    private lateinit var hora_inicial: TextView
    private lateinit var hora_final: TextView
    private lateinit var cant_mascotas: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var btnEmpezar:Button

    private var cantMascotas: String? = null
    private var currentRoadPolyline: Polyline? = null

    private lateinit var paseadorLocationListener: ValueEventListener

    private inner class FetchRouteTask(private val start: GeoPoint, private val finish: GeoPoint) : AsyncTask<Void, Void, Road>() {

        override fun doInBackground(vararg params: Void?): Road? {
            val routePoints = ArrayList<GeoPoint>()
            routePoints.add(start)
            routePoints.add(finish)
            return roadManager.getRoad(routePoints)
        }

        override fun onPostExecute(result: Road?) {
            super.onPostExecute(result)
            if (result != null) {
                drawRoad(result)
            } else {
                Toast.makeText(this@RutaDuenhoActivity, "Error al obtener la ruta", Toast.LENGTH_SHORT).show()
            }
        }
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ruta_duenho)

        btnEmpezar = findViewById<Button>(R.id.btn_Empezar)
        btnEmpezar.isEnabled = false

        profileImage = findViewById(R.id.profile_image)
        nombreUsuario = findViewById(R.id.nombre_duenho)
        hora_inicial = findViewById(R.id.hora_inicial)
        hora_final = findViewById(R.id.hora_final)
        cant_mascotas = findViewById(R.id.cantidad_mascotas)

        auth = FirebaseAuth.getInstance()


        solicitudId = intent.getStringExtra("solicitudId")
        uidDuenho = intent.getStringExtra("usuarioUid")
        cantMascotas = intent.getStringExtra("cantMascotas")

        // Obtén el userId del usuario autenticado
        val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        userId = user?.uid

        val intent = intent
        val uid = intent.getStringExtra("usuarioUid")
        val solicitudId2 = intent.getStringExtra("solicitudId")

        if (uid != null) {
            loadUserData(uid)
            loadProfileImage(uid)
            //loadUserPets(uid)
            cant_mascotas.text = cantMascotas

        }
        if (solicitudId2 != null) {
            loadHoras(solicitudId2)

        }

        databaseReference = FirebaseDatabase.getInstance().getReference("Usuarios")

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

        btnEmpezar.setOnClickListener {
            val intent = Intent(
                applicationContext,
                HomeWalkerActivity::class.java
            )
            updatePaseoRequest()
            startActivity(intent)
        }

        centerButton.setOnClickListener {
            centerCameraOnUser()
        }

        uidDuenho?.let { addPaseadorMarkerFromDatabase(it) }
        setupPaseadorLocationListener()

    }


    private fun loadUserData(uid: String) {
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("Usuarios").child(uid)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val nombre = snapshot.child("nombre").getValue(String::class.java)
                    //val direccion = snapshot.child("direccion").getValue(String::class.java)

                    nombreUsuario.text = nombre ?: "Nombre no disponible"

                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun loadProfileImage(uid: String) {
        val storageRef = FirebaseStorage.getInstance().reference
        val profileImageRef = storageRef.child("Usuarios/$uid/profile")

        profileImageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri)
                .into(profileImage)
        }.addOnFailureListener {
            // Handle error
        }
    }

    private fun loadHoras(uid: String) {
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("SolicitudesPaseo").child(uid)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val horaInicial = snapshot.child("horaInicio").getValue(String::class.java)
                    val horaFinal = snapshot.child("horaFin").getValue(String::class.java)


                    hora_inicial.text = "Hora inicial: $horaInicial"
                    hora_final.text = "Hora final: $horaFinal"

                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun loadUserPets(uid: String) {
        val database = FirebaseDatabase.getInstance()
        val petsRef = database.getReference("Mascotas").child(uid)
        var cont = 0

        petsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val petNames = ArrayList<String>()
                    cont = 0
                    for (petSnapshot in snapshot.children) {
                        cont++;
                    }

                }
                cant_mascotas.text = "Mascotas: $cont"

            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
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

    private fun calcularDistancia(
        lat1: Double,
        long1: Double,
        lat2: Double,
        long2: Double
    ): Double {
        val latDistance = Math.toRadians(lat1 - lat2)
        val lngDistance = Math.toRadians(long1 - long2)
        val a = (sin(latDistance / 2) * sin(latDistance / 2)
                + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2))
                * sin(lngDistance / 2) * sin(lngDistance / 2))
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val result = RADIUS_OF_EARTH_KM * c
        return (result * 100.0).roundToInt() / 100.0
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

    private fun updatePaseoRequest() {
        val database = FirebaseDatabase.getInstance()
        if (solicitudId != null && userId != null) {
            val solicitudRef = database.getReference("SolicitudesPaseo").child(solicitudId!!)
            solicitudRef.child("estado").setValue("en curso")
            Toast.makeText(this, "Solicitud actualizada con éxito", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error al actualizar la solicitud", Toast.LENGTH_SHORT).show()
        }
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
                    paseadorMarker?.title = "Paseador(a): ${nombreUsuario.text}"


                    // Obtener el drawable de la imagen personalizada
                    val customMarkerDrawable = ContextCompat.getDrawable(this@RutaDuenhoActivity, R.drawable.icn_marcador_paseador)

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

                    /*paseadorMarker?.position?.let { paseadorGeoPoint ->
                        drawRoute(geoPoint!!, paseadorGeoPoint)
                    }*/
                } else {
                    Toast.makeText(this@RutaDuenhoActivity, "No se pudo obtener la ubicación del paseador", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@RutaDuenhoActivity, "Error al obtener la ubicación del paseador: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkDistanceAndEnableButton() {
        if (marker != null && paseadorMarker != null) {
            val distance = calcularDistancia(marker!!.position.latitude, marker!!.position.longitude,paseadorMarker!!.position.latitude, paseadorMarker!!.position.longitude)
            println("Distancia: $distance")
            btnEmpezar.isEnabled = distance <= 10
        }
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
            addPaseadorMarkerFromDatabase(uidDuenho!!)
            isFirstLocationUpdate = false
        }

        // Actualiza la ubicación en Firebase
        updateLocationInFirebase(location)
        checkDistanceAndEnableButton()

        paseadorMarker?.position?.let { paseadorGeoPoint ->
            drawRoute(geoPoint!!, paseadorGeoPoint)
        }

    }

    private fun updateLocationInFirebase(location: Location) {
        userId?.let {
            val userLocation = mapOf(
                "latitud" to location.latitude.toString(),
                "longitud" to location.longitude.toString()
            )

            databaseReference.child(it).updateChildren(userLocation).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //Toast.makeText(this, "Ubicación actualizada en Firebase", Toast.LENGTH_SHORT).show()
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
    private fun setupPaseadorLocationListener() {
        paseadorLocationListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val latitudString = snapshot.child("latitud").getValue(String::class.java)
                val longitudString = snapshot.child("longitud").getValue(String::class.java)

                if (latitudString != null && longitudString != null) {
                    try {
                        val latitud = latitudString.toDouble()
                        val longitud = longitudString.toDouble()
                        val paseadorGeoPoint = GeoPoint(latitud, longitud)

                        // Elimina el marcador anterior si existe
                        paseadorMarker?.let { osmMap.overlays.remove(it) }

                        // Crea un nuevo marcador para el paseador
                        paseadorMarker = Marker(osmMap)
                        paseadorMarker?.position = paseadorGeoPoint
                        paseadorMarker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        paseadorMarker?.title = "Dueño(a): ${nombreUsuario.text}"

                        // Obtener el drawable de la imagen personalizada
                        val customMarkerDrawable = ContextCompat.getDrawable(this@RutaDuenhoActivity, R.drawable.icn_marcador_paseador)

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

                        paseadorMarker?.position?.let { paseadorGeoPoint ->
                            drawRoute(geoPoint!!, paseadorGeoPoint)
                        }

                        checkDistanceAndEnableButton()
                    } catch (e: NumberFormatException) {
                        Toast.makeText(this@RutaDuenhoActivity, "Error al convertir la ubicación del paseador", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@RutaDuenhoActivity, "No se pudo obtener la ubicación del paseador", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@RutaDuenhoActivity, "Error al obtener la ubicación del paseador: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }

        val database = FirebaseDatabase.getInstance().getReference("Usuarios/$uidDuenho")
        database.addValueEventListener(paseadorLocationListener)
    }



    override fun onDestroy() {
        super.onDestroy()
        removePaseadorLocationListener()
    }

    private fun removePaseadorLocationListener() {
        val database = FirebaseDatabase.getInstance().getReference("Usuarios/$uidDuenho")
        database.removeEventListener(paseadorLocationListener)
    }

    private fun drawRoad(road: Road) {
        Log.i("OSM_activity", "Route length: ${road.mLength} km")
        Log.i("OSM_activity", "Duration: ${road.mDuration / 60} min")

        // Crea una nueva Polyline a partir de la ruta
        val roadOverlay = RoadManager.buildRoadOverlay(road)
        roadOverlay.outlinePaint.color = Color.BLUE
        roadOverlay.outlinePaint.strokeWidth = 10f

        // Elimina la Polyline anterior si existe
        currentRoadPolyline?.let {
            osmMap.overlays.remove(it)
        }

        // Agrega la nueva Polyline al mapa y actualiza la referencia
        currentRoadPolyline = roadOverlay
        osmMap.overlays.add(currentRoadPolyline)

        // Invalida el mapa para forzar su redibujado
        osmMap.invalidate()
    }


    private fun drawRoute(start: GeoPoint, finish: GeoPoint) {
        FetchRouteTask(start, finish).execute()
    }

}