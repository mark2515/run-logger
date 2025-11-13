package moe.kunlonghe.myruns

import android.Manifest
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import moe.kunlonghe.myruns.database.MyRunsEntry

class TrackingService : Service() {

    private val binder = LocalBinder()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var notificationManager: NotificationManager
    private var stopServiceReceiver: BroadcastReceiver? = null
    
    private val _locationListLiveData = MutableLiveData<ArrayList<LatLng>>()
    val locationListLiveData: LiveData<ArrayList<LatLng>> = _locationListLiveData
    
    private val _distanceLiveData = MutableLiveData<Double>()
    val distanceLiveData: LiveData<Double> = _distanceLiveData
    
    private val _avgSpeedLiveData = MutableLiveData<Double>()
    val avgSpeedLiveData: LiveData<Double> = _avgSpeedLiveData
    
    private val _currentSpeedLiveData = MutableLiveData<Double>()
    val currentSpeedLiveData: LiveData<Double> = _currentSpeedLiveData
    
    private val _climbLiveData = MutableLiveData<Double>()
    val climbLiveData: LiveData<Double> = _climbLiveData
    
    private val _caloriesLiveData = MutableLiveData<Double>()
    val caloriesLiveData: LiveData<Double> = _caloriesLiveData
    
    private val _durationLiveData = MutableLiveData<Long>()
    val durationLiveData: LiveData<Long> = _durationLiveData
    
    private val _activityTypeLiveData = MutableLiveData<Int>()
    val activityTypeLiveData: LiveData<Int> = _activityTypeLiveData
    
    // Tracking data
    private var locationList = ArrayList<LatLng>()
    private var totalDistance = 0.0
    private var totalClimb = 0.0
    private var startTime = 0L
    private var inputType = MyRunsEntry.INPUT_TYPE_GPS
    private var activityType = MyRunsEntry.ACTIVITY_TYPE_RUNNING
    private var lastLocation: Location? = null
    
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "tracking_channel"
        private const val LOCATION_UPDATE_INTERVAL = 5000L // 5 seconds
        private const val LOCATION_UPDATE_FASTEST_INTERVAL = 2000L // 2 seconds
        const val ACTION_STOP_SERVICE = "moe.kunlonghe.myruns.STOP_SERVICE"
    }

    inner class LocalBinder : Binder() {
        fun getService(): TrackingService = this@TrackingService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        
        // Register broadcast receiver to stop service
        stopServiceReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == ACTION_STOP_SERVICE) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(stopServiceReceiver, IntentFilter(ACTION_STOP_SERVICE), RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(stopServiceReceiver, IntentFilter(ACTION_STOP_SERVICE))
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Get input and activity type from intent
        inputType = intent?.getIntExtra("INPUT_TYPE", MyRunsEntry.INPUT_TYPE_GPS) 
            ?: MyRunsEntry.INPUT_TYPE_GPS
        activityType = intent?.getIntExtra("ACTIVITY_TYPE", MyRunsEntry.ACTIVITY_TYPE_RUNNING) 
            ?: MyRunsEntry.ACTIVITY_TYPE_RUNNING
        
        _activityTypeLiveData.postValue(activityType)
        startTime = System.currentTimeMillis()
        
        // Start foreground service with notification
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        
        // Start location updates
        startLocationUpdates()
        
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Tracking Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "MyRuns is recording your path"
            }
            
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        // Intent to open MapActivity when notification is clicked
        val notificationIntent = Intent(this, MapActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            notificationIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MyRuns")
            .setContentText("MyRuns is recording your path")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            LOCATION_UPDATE_INTERVAL
        ).apply {
            setMinUpdateIntervalMillis(LOCATION_UPDATE_FASTEST_INTERVAL)
            setWaitForAccurateLocation(false)
        }.build()
        
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    onLocationChanged(location)
                }
            }
        }
        
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun onLocationChanged(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        locationList.add(latLng)
        _locationListLiveData.postValue(locationList)
        
        // Calculate distance if we have a previous location
        lastLocation?.let { prevLocation ->
            val results = FloatArray(1)
            Location.distanceBetween(
                prevLocation.latitude,
                prevLocation.longitude,
                location.latitude,
                location.longitude,
                results
            )
            
            val distanceInMeters = results[0]
            val distanceInMiles = distanceInMeters / 1609.34
            totalDistance += distanceInMiles
            _distanceLiveData.postValue(totalDistance)
            
            // Calculate climb
            if (location.hasAltitude() && prevLocation.hasAltitude()) {
                val elevationDiff = location.altitude - prevLocation.altitude
                if (elevationDiff > 0) {
                    totalClimb += elevationDiff
                    _climbLiveData.postValue(totalClimb)
                }
            }
        }
        
        // Calculate current speed
        if (location.hasSpeed()) {
            // Speed is in m/s, convert to mph
            val speedMph = location.speed * 2.23694
            _currentSpeedLiveData.postValue(speedMph)
        }
        
        // Calculate average speed
        val elapsedTime = System.currentTimeMillis() - startTime
        val elapsedHours = elapsedTime / (1000.0 * 60.0 * 60.0)
        if (elapsedHours > 0) {
            val avgSpeed = totalDistance / elapsedHours
            _avgSpeedLiveData.postValue(avgSpeed)
        }
        
        // Calculate calories
        val calories = totalDistance * 100
        _caloriesLiveData.postValue(calories)
        
        // Update duration
        _durationLiveData.postValue(elapsedTime)
        
        lastLocation = location
    }

    fun getMyRunsEntry(): MyRunsEntry {
        val duration = (System.currentTimeMillis() - startTime) / 1000.0 // in seconds
        
        return MyRunsEntry(
            inputType = inputType,
            activityType = activityType,
            dateTime = startTime,
            duration = duration,
            distance = totalDistance,
            calorie = totalDistance * 100,
            heartRate = 0.0,
            climb = totalClimb,
            comment = "",
            locationList = serializeLocationList(locationList)
        )
    }

    private fun serializeLocationList(locations: ArrayList<LatLng>): ByteArray? {
        if (locations.isEmpty()) return null
        
        return try {
            val stringBuilder = StringBuilder()
            for (latLng in locations) {
                stringBuilder.append("${latLng.latitude},${latLng.longitude};")
            }
            stringBuilder.toString().toByteArray()
        } catch (e: Exception) {
            null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        
        // Stop location updates
        fusedLocationClient.removeLocationUpdates(locationCallback)
        
        stopServiceReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (e: IllegalArgumentException) {
                // Already unregistered
            }
        }
    }
}