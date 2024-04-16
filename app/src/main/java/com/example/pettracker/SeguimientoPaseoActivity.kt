package com.example.pettracker

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
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import org.osmdroid.events.DelayedMapListener
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.TilesOverlay
import kotlin.math.cos
import kotlin.math.sin

class SeguimientoPaseoActivity : AppCompatActivity(), SensorEventListener, LocationListener {
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private var sensorManager: SensorManager? = null
    private lateinit var locationManager: LocationManager
    private var lightSensor: Sensor? = null
    private var marker: Marker? = null
    private var geoPoint: GeoPoint? = null
    private lateinit var roadManager: RoadManager
    private lateinit var osmMap: MapView
    private val routePolylineMap = mutableMapOf<Road, Polyline>()
    private val routeColors = mutableMapOf<Road, Int>()
    private var previousZoomLevel: Double = 0.0
    private var isFirstLocationUpdate = true
    private var isFirstMarkerUpdate = true
    private var randomMarker: Marker? = null
    private val ROUTE_COLOR = Color.BLUE

    private inner class FetchRouteTask(private val start: GeoPoint, private val finish: GeoPoint) :
        AsyncTask<Void, Void, Road>() {

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
                Toast.makeText(
                    this@SeguimientoPaseoActivity,
                    "Error al obtener la ruta",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seguimiento_paseo)
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
        previousZoomLevel = osmMap.zoomLevelDouble
        val centerButton = findViewById<ImageButton>(R.id.centerButton)

        centerButton.setOnClickListener {
            centerCameraOnUser()
        }
        mapZoomListener()

        val btnTerminar = findViewById<Button>(R.id.btn_terminar)
        btnTerminar.setOnClickListener {
            val intent = Intent(
                applicationContext,
                FinalizarPaseoActivity::class.java
            )
            startActivity(intent)
        }
    }

    private fun mapZoomListener() {
        osmMap.addMapListener(DelayedMapListener(object : MapListener {
            override fun onScroll(event: ScrollEvent?): Boolean {
                return false
            }

            override fun onZoom(event: ZoomEvent): Boolean {
                val currentZoomLevel = osmMap.zoomLevelDouble
                previousZoomLevel = currentZoomLevel
                return true
            }
        }, 100))
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
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
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
        val mapController: IMapController = osmMap.controller
        mapController.setZoom(20.0)
        previousZoomLevel = osmMap.zoomLevelDouble
    }

    override fun onPause() {
        super.onPause()
        osmMap.onPause()
        sensorManager?.unregisterListener(this)
        locationManager.removeUpdates(this)
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
            drawRandomRouteAroundUser()
            isFirstLocationUpdate = false
        }
    }


    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (event.sensor.type == Sensor.TYPE_LIGHT) {
                val lux = event.values[0]
                if (lux < 15000) {
                    osmMap.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
                } else {
                    osmMap.overlayManager.tilesOverlay.setColorFilter(null)
                }
            }
        }
    }

    private fun addRandomMarker(point: GeoPoint) {
        randomMarker = Marker(osmMap)
        randomMarker?.position = point
        randomMarker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        randomMarker?.title = "Marcador Aleatorio"

        // Obtener el drawable de la imagen personalizada
        val customMarkerDrawable = ContextCompat.getDrawable(this, R.drawable.icn_marcador_paseador)

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
        randomMarker?.icon = BitmapDrawable(resources, scaledDrawable)

        osmMap.overlays.add(randomMarker)
        osmMap.invalidate()
    }

    private fun drawRandomRouteAroundUser() {
        val numberOfRandomPoints = 5 // Define cuántos puntos aleatorios quieres generar
        val randomPoints = mutableListOf<GeoPoint>()

        // Genera puntos aleatorios alrededor de la ubicación actual
        repeat(numberOfRandomPoints) {
            val randomDistance = (Math.random() * 0.001) // 0.01 grados es aproximadamente 1.11 km
            val randomBearing = (Math.random() * 360).toFloat()

            val newLat =
                geoPoint!!.latitude + (randomDistance * cos(Math.toRadians(randomBearing.toDouble())))
            val newLon =
                geoPoint!!.longitude + (randomDistance * sin(Math.toRadians(randomBearing.toDouble())))

            randomPoints.add(GeoPoint(newLat, newLon))
        }

        // Añade el punto final que es la ubicación actual
        randomPoints.add(geoPoint!!)

        // Calcula y dibuja la ruta
        calculateAndDrawRoute(randomPoints)
    }

    // Método para calcular y dibujar la ruta entre una lista de puntos
    private fun calculateAndDrawRoute(points: List<GeoPoint>) {
        var start = geoPoint!!
        points.forEach { point ->
            drawRoute(start, point)
            start = point
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No implementation needed
    }

    private fun drawRoute(start: GeoPoint, finish: GeoPoint) {
        FetchRouteTask(start, finish).execute()
    }

    private fun drawRoad(road: Road) {
        // Verifica si ya existe una Polyline para esta ruta
        val existingPolyline = routePolylineMap[road]

        if (existingPolyline == null) {
            // Si no existe, crea una nueva Polyline
            val newPolyline = RoadManager.buildRoadOverlay(road)
            newPolyline.outlinePaint.color = ROUTE_COLOR
            newPolyline.outlinePaint.strokeWidth = 10f
            routePolylineMap[road] = newPolyline
            routeColors[road] = ROUTE_COLOR
            osmMap.overlays.add(newPolyline)
            osmMap.invalidate()

            // Calcula la posición intermedia entre los puntos de inicio y final de la ruta
            val intermediatePoint = calculateIntermediatePosition(road.mNodes[0].mLocation, road.mNodes[1].mLocation)

            if (isFirstMarkerUpdate) {
                addRandomMarker(intermediatePoint)
                isFirstMarkerUpdate = false
            }

        }
    }


    private fun calculateIntermediatePosition(start: GeoPoint, finish: GeoPoint): GeoPoint {
        val intermediateLatitude = (start.latitude + finish.latitude) / 2
        val intermediateLongitude = (start.longitude + finish.longitude) / 2
        return GeoPoint(intermediateLatitude, intermediateLongitude)
    }

    private fun centerCameraOnUser() {
        marker?.let {
            val mapController: IMapController = osmMap.controller
            mapController.setCenter(marker!!.position)
            mapController.setZoom(20.0)
            previousZoomLevel = osmMap.zoomLevelDouble

            } ?: run {
                Toast.makeText(this, "Ubicación no disponible", Toast.LENGTH_SHORT).show()
            }
    }

}
