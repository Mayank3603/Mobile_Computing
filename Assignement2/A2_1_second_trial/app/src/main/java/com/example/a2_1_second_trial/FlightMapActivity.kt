package com.example.a2_1_second_trial



import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class FlightMapActivity : AppCompatActivity() {

    private lateinit var map: MapView

    // Coordinates passed from FlightDetailsActivity
    private var departureLat: Double = 0.0
    private var departureLon: Double = 0.0
    private var arrivalLat: Double = 0.0
    private var arrivalLon: Double = 0.0
    private var liveLat: Double = 0.0
    private var liveLon: Double = 0.0

    private lateinit var markerDeparture: Marker
    private lateinit var markerArrival: Marker
    private lateinit var markerLive: Marker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize osmdroid configuration
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
        setContentView(R.layout.activity_flight_map)

        map = findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        // Retrieve coordinates from intent extras
        departureLat = intent.getDoubleExtra("departureLat", 0.0)
        departureLon = intent.getDoubleExtra("departureLon", 0.0)
        arrivalLat = intent.getDoubleExtra("arrivalLat", 0.0)
        arrivalLon = intent.getDoubleExtra("arrivalLon", 0.0)
        liveLat = intent.getDoubleExtra("liveLat", 0.0)
        liveLon = intent.getDoubleExtra("liveLon", 0.0)

        // Create departure marker
        val departurePoint = GeoPoint(departureLat, departureLon)
        markerDeparture = Marker(map)
        markerDeparture.position = departurePoint
        markerDeparture.title = "Departure"
        // Scale to 60x60 pixels and set anchor to bottom center
        markerDeparture.icon = getScaledDrawable(R.drawable.marker_departure, 60, 60)
        markerDeparture.setAnchor(0.5f, 0.95f)
        map.overlays.add(markerDeparture)

        // Create arrival marker
        val arrivalPoint = GeoPoint(arrivalLat, arrivalLon)
        markerArrival = Marker(map)
        markerArrival.position = arrivalPoint
        markerArrival.title = "Arrival"
        markerArrival.icon = getScaledDrawable(R.drawable.marker_arrival, 60, 60)
        markerArrival.setAnchor(0.5f, 0.95f)
        map.overlays.add(markerArrival)

        // Create live marker
        val livePoint = GeoPoint(liveLat, liveLon)
        markerLive = Marker(map)
        markerLive.position = livePoint
        markerLive.title = "Current Position"
        markerLive.icon = getScaledDrawable(R.drawable.marker_live, 60, 60)
        markerLive.setAnchor(0.5f, 0.95f)
        map.overlays.add(markerLive)

        // Center the camera on the live marker and set an appropriate zoom level
        map.controller.setZoom(7.0)
        map.controller.setCenter(livePoint)
    }

    /**
     * Helper function to scale a drawable resource to a desired width and height.
     */
    private fun getScaledDrawable(drawableId: Int, width: Int, height: Int): Drawable? {
        val drawable = ContextCompat.getDrawable(this, drawableId)
        if (drawable is BitmapDrawable) {
            val bitmap = drawable.bitmap
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
            return BitmapDrawable(resources, scaledBitmap)
        }
        return drawable
    }
}