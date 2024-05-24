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
import android.location.Geocoder
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
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.TilesOverlay
import kotlin.math.sqrt

class PaginaPaseoWalkerActivity : AppCompatActivity(), SensorEventListener, LocationListener {

    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private var sensorManager: SensorManager? = null
    private lateinit var locationManager: LocationManager
    private var lightSensor: Sensor? = null
    private var marker: Marker? = null
    private var mGeocoder: Geocoder? = null
    private val routePolylineMap = mutableMapOf<Road, Polyline>()
    private val routeColors = mutableMapOf<Road, Int>()
    private var geoPoint: GeoPoint? = null
    private val RADIUS_OF_EARTH_KM = 6371
    private val ROUTE_COLOR = Color.BLUE
    private lateinit var roadManager: RoadManager
    private lateinit var osmMap: MapView
    private var isFirstLocationUpdate = true
    private var randomMarker: Marker? = null

    private inner class FetchRouteTask(private val start: GeoPoint, private val finish: GeoPoint) : AsyncTask<Void, Void, Road>() {

        override fun doInBackground(vararg params: Void?): Road? {
            val routePoints = ArrayList<GeoPoint>()
            routePoints.add(start)
            println("Start ${start.latitude} y ${start.longitude}")
            routePoints.add(finish)
            println("Finish ${finish.latitude} y ${finish.longitude}")

            return roadManager.getRoad(routePoints)
        }

        override fun onPostExecute(result: Road?) {
            super.onPostExecute(result)
            if (result != null) {
                drawRoad(result)
            } else {
                Toast.makeText(this@PaginaPaseoWalkerActivity, "Error al obtener la ruta", Toast.LENGTH_SHORT).show()
            }
        }
    }
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pagina_paseo_walker)

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
            marker?.position = geoPoint
            marker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker?.title = "Tú"
            val customMarkerDrawable = ContextCompat.getDrawable(this, R.drawable.icn_paseador_orange)
            val width = 48
            val height = 48
            val scaledDrawable = Bitmap.createScaledBitmap(
                (customMarkerDrawable as BitmapDrawable).bitmap,
                width,
                height,
                false
            )
            marker?.icon = BitmapDrawable(resources, scaledDrawable)
            osmMap.overlays.add(marker)

        } else {
            marker?.position = geoPoint
        }




        // Agregar el marcador aleatorio solo la primera vez que se obtiene la ubicación
        if (isFirstLocationUpdate) {
            println("Ubicacion actual: ${geoPoint!!.latitude} y ${geoPoint!!.longitude}")
            addRandomMarkerAroundUser(3.0)
            isFirstLocationUpdate = false
            println("Random point ${randomMarker!!.position.latitude} y ${randomMarker!!.position.longitude}")
            println("Marker point ${marker!!.position.latitude} y ${marker!!.position.longitude}")

        }

        osmMap.invalidate()

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

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No implementation needed
    }

    private fun addRandomMarkerAroundUser(radiusKm: Double) {
        val random = Math.random()
        val randomAngle = random * 2 * Math.PI
        val randomDistance = sqrt(random) * radiusKm / RADIUS_OF_EARTH_KM

        val latitude = geoPoint!!.latitude + randomDistance * Math.cos(randomAngle)
        val longitude = geoPoint!!.longitude + randomDistance * Math.sin(randomAngle)

        val randomGeoPoint = GeoPoint(latitude, longitude)

        randomMarker = Marker(osmMap)
        randomMarker?.position = randomGeoPoint
        randomMarker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        randomMarker?.title = "Casa"

        // Obtener el drawable de la imagen personalizada
        val customMarkerDrawable = ContextCompat.getDrawable(this, R.drawable.icn_home)

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

        drawRoute(geoPoint!!, randomGeoPoint)
        drawRoute(randomGeoPoint, geoPoint!!)
        osmMap.invalidate()
    }

    private fun centerCameraOnUser() {
        marker?.let {
            val mapController: IMapController = osmMap.controller
            mapController.setCenter(marker!!.position)
            mapController.setZoom(20.0)
        } ?: run {
            Toast.makeText(this, "Ubicación no disponible", Toast.LENGTH_SHORT).show()
        }
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
            newPolyline.outlinePaint.color = ROUTE_COLOR // Asigna el color constante a la polilínea
            newPolyline.outlinePaint.strokeWidth = 10f
            routePolylineMap[road] = newPolyline
            routeColors[road] = ROUTE_COLOR
            osmMap.overlays.add(newPolyline)
        }

        osmMap.invalidate()
    }


}
