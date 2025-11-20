package moe.kunlonghe.myruns

import android.Manifest
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.AsyncTask
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import moe.kunlonghe.myruns.database.MyRunsEntry
import java.util.concurrent.ArrayBlockingQueue
import kotlin.math.sqrt

class TrackingService : Service(), SensorEventListener {

    private val binder = LocalBinder()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var notificationManager: NotificationManager
    private var stopServiceReceiver: BroadcastReceiver? = null
    
    // Activity recognition components
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var accBuffer: ArrayBlockingQueue<Double>? = null
    private var classificationTask: OnSensorChangedTask? = null
    private val recentActivities = ArrayDeque<Int>()
    private val fft = FFT(Globals.ACCELEROMETER_BLOCK_CAPACITY)
    
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
        
        // Initialize sensor manager for activity recognition
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        
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
        
        // Start activity recognition if in Automatic mode
        if (inputType == MyRunsEntry.INPUT_TYPE_AUTOMATIC) {
            startActivityRecognition()
        }
        
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

    private fun startActivityRecognition() {
        accBuffer = ArrayBlockingQueue(Globals.ACCELEROMETER_BUFFER_CAPACITY)
        
        // Register accelerometer sensor listener
        accelerometer?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_FASTEST
            )
        }
        
        // Start the classification task
        classificationTask = OnSensorChangedTask()
        classificationTask?.execute()
    }
    
    private fun stopActivityRecognition() {
        // Unregister sensor listener
        sensorManager.unregisterListener(this)
        
        // Cancel the classification task
        classificationTask?.cancel(true)
        classificationTask = null
        
        accBuffer?.clear()
        accBuffer = null
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LINEAR_ACCELERATION) {
            // Calculate magnitude from x, y, z values
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val magnitude = sqrt((x * x + y * y + z * z).toDouble())
            
            // Add to buffer
            try {
                accBuffer?.add(magnitude)
            } catch (e: IllegalStateException) {
                // Buffer is full, double the capacity
                val newBuffer = ArrayBlockingQueue<Double>(accBuffer!!.size * 2)
                accBuffer!!.drainTo(newBuffer)
                accBuffer = newBuffer
                accBuffer?.add(magnitude)
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
    
    private inner class OnSensorChangedTask : AsyncTask<Void, Void, Void>() {
        
        override fun doInBackground(vararg params: Void?): Void? {
            var blockSize = 0
            val accBlock = DoubleArray(Globals.ACCELEROMETER_BLOCK_CAPACITY)
            val im = DoubleArray(Globals.ACCELEROMETER_BLOCK_CAPACITY)
            
            while (!isCancelled) {
                try {
                    // Get magnitude from buffer
                    val magnitude = accBuffer?.take() ?: continue
                    accBlock[blockSize++] = magnitude
                    
                    if (blockSize == Globals.ACCELEROMETER_BLOCK_CAPACITY) {
                        blockSize = 0
                        
                        // Find max magnitude
                        var max = accBlock[0]
                        for (value in accBlock) {
                            if (value > max) {
                                max = value
                            }
                        }
                        
                        // Perform FFT
                        fft.fft(accBlock, im)
                        
                        // Create feature array for classification
                        val features = Array<Any>(Globals.ACCELEROMETER_BLOCK_CAPACITY + 1) { 0.0 }
                        
                        for (i in accBlock.indices) {
                            val mag = sqrt(accBlock[i] * accBlock[i] + im[i] * im[i])
                            features[i] = mag
                            im[i] = 0.0 // Clear for next iteration
                        }
                        
                        // Add max as the last feature
                        features[Globals.ACCELEROMETER_BLOCK_CAPACITY] = max
                        
                        // Classify using Weka classifier
                        try {
                            val activityLabel = WekaClassifier.classify(features).toInt()
                            
                            // Map Weka classification to MyRunsEntry activity types
                            val recognizedActivity = when (activityLabel) {
                                Globals.ACTIVITY_ID_STANDING -> MyRunsEntry.ACTIVITY_TYPE_STANDING
                                Globals.ACTIVITY_ID_WALKING -> MyRunsEntry.ACTIVITY_TYPE_WALKING
                                Globals.ACTIVITY_ID_RUNNING -> MyRunsEntry.ACTIVITY_TYPE_RUNNING
                                else -> MyRunsEntry.ACTIVITY_TYPE_OTHER
                            }
                            
                            synchronized(recentActivities) {
                                recentActivities.addLast(recognizedActivity)
                                if (recentActivities.size > Globals.RECENT_ACTIVITY_WINDOW_SIZE) {
                                    recentActivities.removeFirst()
                                }
                            }

                            val mostCommonActivity = synchronized(recentActivities) {
                                if (recentActivities.isEmpty()) {
                                    null
                                } else {
                                    recentActivities
                                        .groupingBy { it }
                                        .eachCount()
                                        .maxByOrNull { it.value }
                                        ?.key
                                }
                            }

                            if (mostCommonActivity != null && mostCommonActivity != activityType) {
                                activityType = mostCommonActivity
                                _activityTypeLiveData.postValue(activityType)
                                Log.d(Globals.TAG, "Activity detected: ${getActivityName(activityType)}")
                            }
                            
                        } catch (e: Exception) {
                            Log.e(Globals.TAG, "Classification error: ${e.message}")
                        }
                    }
                    
                } catch (e: InterruptedException) {
                    break
                } catch (e: Exception) {
                    Log.e(Globals.TAG, "Error in classification task: ${e.message}")
                }
            }
            
            return null
        }
        
        private fun getActivityName(activityType: Int): String {
            return when (activityType) {
                MyRunsEntry.ACTIVITY_TYPE_STANDING -> "Standing"
                MyRunsEntry.ACTIVITY_TYPE_WALKING -> "Walking"
                MyRunsEntry.ACTIVITY_TYPE_RUNNING -> "Running"
                else -> "Other"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        
        // Stop activity recognition if it was started
        if (inputType == MyRunsEntry.INPUT_TYPE_AUTOMATIC) {
            stopActivityRecognition()
        }
        
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