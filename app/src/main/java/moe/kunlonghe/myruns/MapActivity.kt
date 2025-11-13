package moe.kunlonghe.myruns

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.view.View
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.kunlonghe.myruns.database.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    
    private lateinit var buttonSave: Button
    private lateinit var buttonCancel: Button
    private lateinit var tvActivityType: TextView
    private lateinit var tvAvgSpeed: TextView
    private lateinit var tvCurrentSpeed: TextView
    private lateinit var tvClimb: TextView
    private lateinit var tvCalories: TextView
    private lateinit var tvDistance: TextView
    
    private var googleMap: GoogleMap? = null
    private var inputTypeInt: Int = MyRunsEntry.INPUT_TYPE_GPS
    private var activityTypeInt: Int = MyRunsEntry.ACTIVITY_TYPE_RUNNING
    private var inputTypeStr: String? = null
    private var activityTypeStr: String? = null
    private var entryId: Long = -1L
    private var isHistoryMode = false
    private var trackingService: TrackingService? = null
    private var serviceBound = false
    private var startMarker: Marker? = null
    private var endMarker: Marker? = null
    private var polyline: Polyline? = null
    
    private lateinit var myRunsViewModel: MyRunsViewModel
    
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TrackingService.LocalBinder
            trackingService = binder.getService()
            serviceBound = true
            observeTrackingService()
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            trackingService = null
            serviceBound = false
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Map"
        
        // Initialize views
        initializeViews()
        
        // Setup database
        setupDatabase()
        
        // Check if this is history mode or tracking mode
        entryId = intent.getLongExtra("ENTRY_ID", -1L)
        isHistoryMode = entryId != -1L
        
        if (isHistoryMode) {
            // Load history entry
            loadHistoryEntry()
        } else {
            // Get data from intent for new tracking
            inputTypeStr = intent.getStringExtra("INPUT_TYPE")
            activityTypeStr = intent.getStringExtra("ACTIVITY_TYPE")
            
            inputTypeInt = UnitConverter.getInputTypeInt(inputTypeStr ?: "GPS")
            activityTypeInt = UnitConverter.getActivityTypeInt(activityTypeStr ?: "Running")
            
            tvActivityType.text = "Type: ${activityTypeStr ?: "Running"}"
            
            // Request location permissions and start tracking
            checkPermissionsAndStartTracking()
        }
        
        // Initialize map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        
        // Setup button listeners
        setupButtons()
    }
    
    private fun initializeViews() {
        buttonSave = findViewById(R.id.button_save)
        buttonCancel = findViewById(R.id.button_cancel)
        tvActivityType = findViewById(R.id.tv_activity_type)
        tvAvgSpeed = findViewById(R.id.tv_avg_speed)
        tvCurrentSpeed = findViewById(R.id.tv_current_speed)
        tvClimb = findViewById(R.id.tv_climb)
        tvCalories = findViewById(R.id.tv_calories)
        tvDistance = findViewById(R.id.tv_distance)
    }
    
    private fun setupDatabase() {
        val database = MyRunsDatabase.getInstance(applicationContext)
        val databaseDao = database.myRunsEntryDao
        val repository = MyRunsRepository(databaseDao)
        val viewModelFactory = MyRunsViewModelFactory(repository)
        myRunsViewModel = ViewModelProvider(this, viewModelFactory).get(MyRunsViewModel::class.java)
    }
    
    private fun checkPermissionsAndStartTracking() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            startTrackingService()
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startTrackingService()
            } else {
                Toast.makeText(this, "Location permission is required for tracking", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
    
    private fun startTrackingService() {
        val intent = Intent(this, TrackingService::class.java)
        intent.putExtra("INPUT_TYPE", inputTypeInt)
        intent.putExtra("ACTIVITY_TYPE", activityTypeInt)
        
        startService(intent)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }
    
    private fun observeTrackingService() {
        trackingService?.let { service ->
            service.locationListLiveData.observe(this) { locations ->
                updateMap(locations)
            }
            
            service.distanceLiveData.observe(this) { distanceMiles ->
                tvDistance.text = "Distance: ${UnitConverter.formatDistance(this, distanceMiles)}"
            }
            
            service.avgSpeedLiveData.observe(this) { avgSpeedMph ->
                if (UnitConverter.isMetric(this)) {
                    val avgKmh = UnitConverter.milesToKm(avgSpeedMph)
                    tvAvgSpeed.text = String.format("Avg speed: %.2f km/h", avgKmh)
                } else {
                    tvAvgSpeed.text = String.format("Avg speed: %.2f mph", avgSpeedMph)
                }
            }
            
            service.currentSpeedLiveData.observe(this) { currentSpeedMph ->
                if (UnitConverter.isMetric(this)) {
                    val curKmh = UnitConverter.milesToKm(currentSpeedMph)
                    tvCurrentSpeed.text = String.format("Cur speed: %.2f km/h", curKmh)
                } else {
                    tvCurrentSpeed.text = String.format("Cur speed: %.2f mph", currentSpeedMph)
                }
            }
            
            service.climbLiveData.observe(this) { climbMeters ->
                if (UnitConverter.isMetric(this)) {
                    val climbKm = climbMeters / 1000.0
                    tvClimb.text = String.format("Climb: %.2f Kilometers", climbKm)
                } else {
                    tvClimb.text = String.format("Climb: %.2f m", climbMeters)
                }
            }
            
            service.caloriesLiveData.observe(this) { calories ->
                tvCalories.text = String.format("Calories: %.0f", calories)
            }
        }
    }
    
    private fun loadHistoryEntry() {
        lifecycleScope.launch {
            val entry = withContext(Dispatchers.IO) {
                val database = MyRunsDatabase.getInstance(this@MapActivity.applicationContext)
                val dao = database.myRunsEntryDao
                dao.getMyRunsEntry(entryId)
            }
            
            if (entry != null) {
                displayHistoryEntry(entry)
            } else {
                Toast.makeText(
                    this@MapActivity,
                    "Error: Entry not found",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }
    
    private fun displayHistoryEntry(entry: MyRunsEntry) {
        tvActivityType.text = "Type: ${UnitConverter.getActivityTypeName(entry.activityType)}"
        tvDistance.text = "Distance: ${UnitConverter.formatDistance(this, entry.distance)}"
        tvCalories.text = String.format("Calories: %.0f", entry.calorie)
        
        val avgSpeed = if (entry.duration > 0) {
            entry.distance / (entry.duration / 3600.0)
        } else {
            0.0
        }
        if (UnitConverter.isMetric(this)) {
            val avgKmh = UnitConverter.milesToKm(avgSpeed)
            tvAvgSpeed.text = String.format("Avg speed: %.2f km/h", avgKmh)
        } else {
            tvAvgSpeed.text = String.format("Avg speed: %.2f mph", avgSpeed)
        }
        tvCurrentSpeed.text = "Cur speed: N/A"
        tvClimb.text = "Climb: N/A"
        
        // Deserialize and display locations
        entry.locationList?.let { locationData ->
            val locations = deserializeLocationList(locationData)
            updateMap(locations)
        }
        
        buttonSave.visibility = View.GONE
        buttonCancel.visibility = View.GONE
    }
    
    private fun deserializeLocationList(data: ByteArray): ArrayList<LatLng> {
        val locations = ArrayList<LatLng>()
        try {
            val dataStr = String(data)
            val points = dataStr.split(";")
            for (point in points) {
                if (point.isNotEmpty()) {
                    val coords = point.split(",")
                    if (coords.size == 2) {
                        val lat = coords[0].toDouble()
                        val lng = coords[1].toDouble()
                        locations.add(LatLng(lat, lng))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return locations
    }
    
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
        }
        
        map.mapType = GoogleMap.MAP_TYPE_NORMAL
        map.uiSettings.isZoomControlsEnabled = true
    }
    
    private fun updateMap(locations: ArrayList<LatLng>) {
        googleMap?.let { map ->
            if (locations.isEmpty()) return
            
            // Remove existing markers and polylines
            startMarker?.remove()
            endMarker?.remove()
            polyline?.remove()
            
            // Add start marker
            val startLatLng = locations.first()
            startMarker = map.addMarker(
                MarkerOptions()
                    .position(startLatLng)
                    .title("Start")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )
            
            // Add end marker
            val endLatLng = locations.last()
            endMarker = map.addMarker(
                MarkerOptions()
                    .position(endLatLng)
                    .title("Current")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )
            
            // Draw polyline
            val polylineOptions = PolylineOptions()
                .addAll(locations)
                .color(Color.BLUE)
                .width(10f)
            
            polyline = map.addPolyline(polylineOptions)
            
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(endLatLng, 15f))
        }
    }
    
    private fun setupButtons() {
        buttonSave.setOnClickListener {
            if (!isHistoryMode) {
                saveAndFinish()
            }
        }
        
        buttonCancel.setOnClickListener {
            if (serviceBound) {
                unbindService(serviceConnection)
                serviceBound = false
            }
            if (!isHistoryMode) {
                stopService(Intent(this, TrackingService::class.java))
            }
            finish()
        }
    }
    
    private fun saveAndFinish() {
        trackingService?.let { service ->
            val entry = service.getMyRunsEntry()
            myRunsViewModel.insert(entry)
            Toast.makeText(this, "Workout saved!", Toast.LENGTH_SHORT).show()
        }
        
        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }
        stopService(Intent(this, TrackingService::class.java))
        finish()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }
        if (!isHistoryMode) {
            stopService(Intent(this, TrackingService::class.java))
        }
        finish()
        return true
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return if (isHistoryMode) {
            menuInflater.inflate(R.menu.menu_display_entry, menu)
            true
        } else {
            super.onCreateOptionsMenu(menu)
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (serviceBound) {
                    unbindService(serviceConnection)
                    serviceBound = false
                }
                if (!isHistoryMode) {
                    stopService(Intent(this, TrackingService::class.java))
                }
                finish()
                true
            }
            R.id.action_delete -> {
                if (isHistoryMode && entryId != -1L) {
                    myRunsViewModel.delete(entryId)
                    Toast.makeText(this, "Entry deleted", Toast.LENGTH_SHORT).show()
                    finish()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
