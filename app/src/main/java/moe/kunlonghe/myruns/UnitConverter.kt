package moe.kunlonghe.myruns

import android.content.Context
import moe.kunlonghe.myruns.database.MyRunsEntry
import java.text.SimpleDateFormat
import java.util.*

object UnitConverter {
    // Conversion constants
    private const val MILES_TO_KM = 1.60934

    fun isMetric(context: Context): Boolean {
        val sharedPref = context.getSharedPreferences("MyRunsPrefs", Context.MODE_PRIVATE)
        return try {
            sharedPref.getBoolean("unit_preference", false)
        } catch (e: ClassCastException) {
            // Fallback for legacy string-based value
            sharedPref.getString("unit_preference", "Imperial") == "Metric"
        }
    }

    fun milesToKm(miles: Double): Double {
        return miles * MILES_TO_KM
    }

    fun kmToMiles(km: Double): Double {
        return km / MILES_TO_KM
    }

    fun formatDistance(context: Context, distanceInMiles: Double): String {
        return if (isMetric(context)) {
            String.format("%.2f Kilometers", milesToKm(distanceInMiles))
        } else {
            String.format("%.2f Miles", distanceInMiles)
        }
    }

    fun formatDurationInMinutes(durationInSeconds: Double): String {
        val minutes = (durationInSeconds / 60).toInt()
        return "$minutes mins"
    }

    fun formatCalories(calories: Double): String {
        return String.format("%.0f cals", calories)
    }

    fun formatHeartRate(heartRate: Double): String {
        return String.format("%.0f bpm", heartRate)
    }

    fun formatDateTime(timestamp: Long): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("HH:mm:ss MMM dd yyyy", Locale.ENGLISH)
        return format.format(date)
    }

    fun formatDate(timestamp: Long): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return format.format(date)
    }

    fun formatTime(timestamp: Long): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("h:mm a", Locale.getDefault())
        return format.format(date)
    }

    fun getActivityTypeName(activityType: Int): String {
        return when (activityType) {
            MyRunsEntry.ACTIVITY_TYPE_RUNNING -> "Running"
            MyRunsEntry.ACTIVITY_TYPE_WALKING -> "Walking"
            MyRunsEntry.ACTIVITY_TYPE_STANDING -> "Standing"
            MyRunsEntry.ACTIVITY_TYPE_CYCLING -> "Cycling"
            MyRunsEntry.ACTIVITY_TYPE_HIKING -> "Hiking"
            MyRunsEntry.ACTIVITY_TYPE_DOWNHILL_SKIING -> "Downhill Skiing"
            MyRunsEntry.ACTIVITY_TYPE_CROSS_COUNTRY_SKIING -> "Cross-Country Skiing"
            MyRunsEntry.ACTIVITY_TYPE_SNOWBOARDING -> "Snowboarding"
            MyRunsEntry.ACTIVITY_TYPE_SKATING -> "Skating"
            MyRunsEntry.ACTIVITY_TYPE_SWIMMING -> "Swimming"
            MyRunsEntry.ACTIVITY_TYPE_MOUNTAIN_BIKING -> "Mountain Biking"
            MyRunsEntry.ACTIVITY_TYPE_WHEELCHAIR -> "Wheelchair"
            MyRunsEntry.ACTIVITY_TYPE_ELLIPTICAL -> "Elliptical"
            MyRunsEntry.ACTIVITY_TYPE_OTHER -> "Other"
            else -> "Unknown"
        }
    }

    fun getInputTypeName(inputType: Int): String {
        return when (inputType) {
            MyRunsEntry.INPUT_TYPE_MANUAL -> "Manual Entry"
            MyRunsEntry.INPUT_TYPE_GPS -> "GPS"
            MyRunsEntry.INPUT_TYPE_AUTOMATIC -> "Automatic"
            else -> "Unknown"
        }
    }

    fun getActivityTypeInt(activityTypeName: String): Int {
        return when (activityTypeName) {
            "Running" -> MyRunsEntry.ACTIVITY_TYPE_RUNNING
            "Walking" -> MyRunsEntry.ACTIVITY_TYPE_WALKING
            "Standing" -> MyRunsEntry.ACTIVITY_TYPE_STANDING
            "Cycling" -> MyRunsEntry.ACTIVITY_TYPE_CYCLING
            "Hiking" -> MyRunsEntry.ACTIVITY_TYPE_HIKING
            "Downhill Skiing" -> MyRunsEntry.ACTIVITY_TYPE_DOWNHILL_SKIING
            "Cross-Country Skiing" -> MyRunsEntry.ACTIVITY_TYPE_CROSS_COUNTRY_SKIING
            "Snowboarding" -> MyRunsEntry.ACTIVITY_TYPE_SNOWBOARDING
            "Skating" -> MyRunsEntry.ACTIVITY_TYPE_SKATING
            "Swimming" -> MyRunsEntry.ACTIVITY_TYPE_SWIMMING
            "Mountain Biking" -> MyRunsEntry.ACTIVITY_TYPE_MOUNTAIN_BIKING
            "Wheelchair" -> MyRunsEntry.ACTIVITY_TYPE_WHEELCHAIR
            "Elliptical" -> MyRunsEntry.ACTIVITY_TYPE_ELLIPTICAL
            "Other" -> MyRunsEntry.ACTIVITY_TYPE_OTHER
            else -> MyRunsEntry.ACTIVITY_TYPE_RUNNING
        }
    }

    fun getInputTypeInt(inputTypeName: String): Int {
        return when (inputTypeName) {
            "Manual Entry" -> MyRunsEntry.INPUT_TYPE_MANUAL
            "GPS" -> MyRunsEntry.INPUT_TYPE_GPS
            "Automatic" -> MyRunsEntry.INPUT_TYPE_AUTOMATIC
            else -> MyRunsEntry.INPUT_TYPE_MANUAL
        }
    }
}